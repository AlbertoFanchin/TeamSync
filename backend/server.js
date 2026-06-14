const express = require('express');
const cors = require('cors');
const Database = require('better-sqlite3');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 4000;
const DB_PATH = process.env.DB_PATH || path.join(__dirname, 'teamsync.db');
const SECRET = process.env.JWT_SECRET || 'dev-secret-change-me';

const db = new Database(DB_PATH);
db.pragma('journal_mode = WAL');
db.exec(fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8'));

// Lightweight forward-only migrations for existing DBs (CREATE TABLE IF NOT EXISTS
// won't add columns to an existing table). Each ALTER is wrapped because SQLite
// errors out if the column already exists.
const migrate = (sql) => { try { db.exec(sql); } catch { /* already applied */ } };
migrate(`ALTER TABLE users   ADD COLUMN skill_levels TEXT NOT NULL DEFAULT '{}'`);
migrate(`ALTER TABLE matches ADD COLUMN address TEXT`);

const hash = (pwd, salt = crypto.randomBytes(16).toString('hex')) => {
  const h = crypto.scryptSync(pwd, salt, 64).toString('hex');
  return `${salt}:${h}`;
};
const verify = (pwd, stored) => {
  const [salt, h] = stored.split(':');
  const test = crypto.scryptSync(pwd, salt, 64).toString('hex');
  return crypto.timingSafeEqual(Buffer.from(h, 'hex'), Buffer.from(test, 'hex'));
};

const b64u = (b) => Buffer.from(b).toString('base64url');
const signToken = (payload) => {
  const header = b64u(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const body = b64u(JSON.stringify({ ...payload, iat: Date.now() }));
  const sig = crypto.createHmac('sha256', SECRET).update(`${header}.${body}`).digest('base64url');
  return `${header}.${body}.${sig}`;
};
const verifyToken = (token) => {
  try {
    const [h, b, s] = token.split('.');
    const expected = crypto.createHmac('sha256', SECRET).update(`${h}.${b}`).digest('base64url');
    if (expected !== s) return null;
    return JSON.parse(Buffer.from(b, 'base64url').toString());
  } catch { return null; }
};

const app = express();
app.use(cors());
app.use(express.json({ limit: '1mb' }));

const auth = (req, res, next) => {
  const token = (req.headers.authorization || '').replace('Bearer ', '');
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'unauthorized' });
  req.user = payload;
  next();
};

app.post('/api/auth/register', (req, res) => {
  const { email, password, name, sport = 'volleyball', position = null } = req.body || {};
  if (!email || !password || !name) return res.status(400).json({ error: 'missing_fields' });
  try {
    const stmt = db.prepare(`
      INSERT INTO users (email, password_hash, name, sport, position, skill_gaps, avatar_seed)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `);
    const seed = crypto.randomBytes(4).toString('hex');
    const info = stmt.run(email.toLowerCase(), hash(password), name, sport, position, '["serve","passing"]', seed);
    const token = signToken({ uid: info.lastInsertRowid, email });
    res.json({ token, user: { id: info.lastInsertRowid, email, name, sport, position } });
  } catch (e) {
    if (String(e).includes('UNIQUE')) return res.status(409).json({ error: 'email_taken' });
    res.status(500).json({ error: 'server_error' });
  }
});

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body || {};
  const u = db.prepare('SELECT * FROM users WHERE email = ?').get((email || '').toLowerCase());
  if (!u || !verify(password, u.password_hash)) return res.status(401).json({ error: 'invalid_credentials' });
  const token = signToken({ uid: u.id, email: u.email });
  res.json({
    token,
    user: {
      id: u.id, email: u.email, name: u.name, sport: u.sport,
      position: u.position, skill_gaps: JSON.parse(u.skill_gaps || '[]'),
    },
  });
});

app.get('/api/me', auth, (req, res) => {
  const u = db.prepare('SELECT id,email,name,sport,position,skill_gaps,has_car,car_seats FROM users WHERE id = ?').get(req.user.uid);
  if (!u) return res.status(404).json({ error: 'not_found' });
  u.skill_gaps = JSON.parse(u.skill_gaps || '[]');
  res.json(u);
});

// IMPORTANT: declare `/upcoming` BEFORE `/:id`, otherwise Express matches the
// param route first and routes `/upcoming` into `id="upcoming"` → 404.
app.get('/api/matches/upcoming', auth, (req, res) => {
  const rows = db.prepare(`
    SELECT m.*, r.status AS my_rsvp, r.needs_ride
    FROM matches m
    LEFT JOIN rsvps r ON r.match_id = m.id AND r.user_id = ?
    WHERE m.kickoff_at >= datetime('now')
    ORDER BY m.kickoff_at ASC
    LIMIT 20
  `).all(req.user.uid);
  res.json(rows);
});

app.get('/api/matches/:id', auth, (req, res) => {
  const m = db.prepare(`
    SELECT m.*, r.status AS my_rsvp, r.needs_ride
    FROM matches m
    LEFT JOIN rsvps r ON r.match_id = m.id AND r.user_id = ?
    WHERE m.id = ?
  `).get(req.user.uid, req.params.id);
  if (!m) return res.status(404).json({ error: 'not_found' });

  // Full attendance list: every team member's RSVP (or "pending" if missing)
  const attendance = db.prepare(`
    SELECT u.id AS user_id, u.name, u.position,
           COALESCE(r.status, 'pending') AS status,
           COALESCE(r.needs_ride, 0)     AS needs_ride
    FROM team_members tm
    JOIN users u ON u.id = tm.user_id
    LEFT JOIN rsvps r ON r.user_id = u.id AND r.match_id = ?
    WHERE tm.team_id = ?
    ORDER BY
      CASE COALESCE(r.status,'pending')
        WHEN 'in' THEN 0 WHEN 'maybe' THEN 1 WHEN 'pending' THEN 2 WHEN 'out' THEN 3
      END,
      u.name
  `).all(req.params.id, m.team_id);

  res.json({ match: m, attendance });
});

app.post('/api/matches/:id/rsvp', auth, (req, res) => {
  const { status, needs_ride = 0 } = req.body || {};
  if (!['in', 'out', 'maybe'].includes(status)) return res.status(400).json({ error: 'bad_status' });
  db.prepare(`
    INSERT INTO rsvps (match_id, user_id, status, needs_ride)
    VALUES (?, ?, ?, ?)
    ON CONFLICT(match_id, user_id) DO UPDATE SET status=excluded.status, needs_ride=excluded.needs_ride, responded_at=CURRENT_TIMESTAMP
  `).run(req.params.id, req.user.uid, status, needs_ride ? 1 : 0);
  res.json({ ok: true });
});

app.get('/api/drills/today', auth, (req, res) => {
  const u = db.prepare('SELECT sport, skill_gaps FROM users WHERE id = ?').get(req.user.uid);
  const gaps = JSON.parse(u.skill_gaps || '[]');
  const placeholders = gaps.length ? gaps.map(() => '?').join(',') : "''";
  const row = db.prepare(`
    SELECT * FROM drills WHERE sport = ? AND skill_tag IN (${placeholders})
    ORDER BY RANDOM() LIMIT 1
  `).get(u.sport, ...gaps);
  res.json(row || {
    id: 0, title: 'Pepper Drill — 5 min', skill_tag: 'passing',
    duration_s: 300, video_url: '', thumbnail: '', difficulty: 1,
  });
});

app.get('/api/carpools/:matchId', auth, (req, res) => {
  const cars = db.prepare(`
    SELECT c.*, u.name AS driver_name
    FROM carpools c JOIN users u ON u.id = c.driver_id
    WHERE c.match_id = ?
  `).all(req.params.matchId);
  for (const c of cars) {
    c.passengers = db.prepare(`
      SELECT p.user_id, u.name, p.pickup_lat, p.pickup_lng, p.stop_order
      FROM carpool_passengers p JOIN users u ON u.id = p.user_id
      WHERE p.carpool_id = ? ORDER BY p.stop_order
    `).all(c.id);
    c.route_json = c.route_json ? JSON.parse(c.route_json) : null;
  }
  res.json(cars);
});

app.post('/api/carpools', auth, (req, res) => {
  const { match_id, seats, depart_at, origin_lat = null, origin_lng = null } = req.body || {};
  if (!match_id || !seats || !depart_at) return res.status(400).json({ error: 'missing_fields' });
  const seatsN = Number(seats);
  if (!Number.isFinite(seatsN) || seatsN < 1 || seatsN > 8) return res.status(400).json({ error: 'bad_seats' });
  const m = db.prepare('SELECT id FROM matches WHERE id = ?').get(match_id);
  if (!m) return res.status(404).json({ error: 'match_not_found' });

  // Fall back to driver's home coords if origin not supplied
  let oLat = origin_lat, oLng = origin_lng;
  if (oLat == null || oLng == null) {
    const h = db.prepare('SELECT home_lat, home_lng FROM users WHERE id = ?').get(req.user.uid);
    oLat = oLat ?? h?.home_lat ?? null;
    oLng = oLng ?? h?.home_lng ?? null;
  }
  const info = db.prepare(`
    INSERT INTO carpools (match_id, driver_id, seats, depart_at, origin_lat, origin_lng)
    VALUES (?, ?, ?, ?, ?, ?)
  `).run(match_id, req.user.uid, seatsN, depart_at, oLat, oLng);
  res.json({ id: info.lastInsertRowid });
});

app.post('/api/carpools/:matchId/optimize', auth, (req, res) => {
  const matchId = req.params.matchId;
  const m = db.prepare('SELECT * FROM matches WHERE id = ?').get(matchId);
  if (!m || m.venue_lat == null) return res.status(400).json({ error: 'no_venue_geo' });
  const cars = db.prepare('SELECT * FROM carpools WHERE match_id = ?').all(matchId);
  const results = [];
  for (const c of cars) {
    const pax = db.prepare(`
      SELECT p.user_id, p.pickup_lat AS lat, p.pickup_lng AS lng, u.name
      FROM carpool_passengers p JOIN users u ON u.id = p.user_id
      WHERE p.carpool_id = ? AND p.pickup_lat IS NOT NULL
    `).all(c.id);

    const route = [];
    const remaining = [...pax];
    let cur = { lat: c.origin_lat, lng: c.origin_lng };
    const d = (a, b) => Math.hypot(a.lat - b.lat, a.lng - b.lng);
    while (remaining.length) {
      let best = 0;
      for (let i = 1; i < remaining.length; i++) {
        if (d(cur, remaining[i]) < d(cur, remaining[best])) best = i;
      }
      const next = remaining.splice(best, 1)[0];
      route.push(next);
      cur = next;
    }
    route.push({ lat: m.venue_lat, lng: m.venue_lng, name: m.venue });

    const tx = db.transaction(() => {
      db.prepare('UPDATE carpools SET route_json = ? WHERE id = ?').run(JSON.stringify(route), c.id);
      route.slice(0, -1).forEach((r, idx) => {
        db.prepare('UPDATE carpool_passengers SET stop_order = ? WHERE carpool_id = ? AND user_id = ?').run(idx, c.id, r.user_id);
      });
    });
    tx();
    results.push({ carpool_id: c.id, route });
  }
  res.json({ optimized: results });
});

app.get('/api/me/skills', auth, (req, res) => {
  const u = db.prepare('SELECT skill_levels FROM users WHERE id = ?').get(req.user.uid);
  res.json(JSON.parse(u?.skill_levels || '{}'));
});

app.post('/api/me/skills', auth, (req, res) => {
  const allowed = ['spiking', 'setting', 'digging', 'serving', 'blocking', 'passing'];
  const incoming = req.body || {};
  const clean = {};
  for (const k of allowed) {
    if (k in incoming) {
      const v = Number(incoming[k]);
      if (!Number.isFinite(v) || v < 0 || v > 5) return res.status(400).json({ error: `bad_${k}` });
      clean[k] = Math.round(v * 10) / 10;
    }
  }
  db.prepare('UPDATE users SET skill_levels = ? WHERE id = ?').run(JSON.stringify(clean), req.user.uid);
  res.json(clean);
});

app.get('/api/rotations/:teamId', auth, (req, res) => {
  const rows = db.prepare('SELECT * FROM rotations WHERE team_id = ? ORDER BY updated_at DESC').all(req.params.teamId);
  res.json(rows.map((r) => ({ ...r, positions: JSON.parse(r.positions) })));
});

app.post('/api/rotations/:teamId', auth, (req, res) => {
  const { name, positions } = req.body || {};
  const info = db.prepare('INSERT INTO rotations (team_id, name, positions) VALUES (?, ?, ?)')
    .run(req.params.teamId, name, JSON.stringify(positions));
  res.json({ id: info.lastInsertRowid });
});

app.listen(PORT, () => console.log(`TeamSync API → http://localhost:${PORT}`));
