const Database = require('better-sqlite3');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const DB_PATH = process.env.DB_PATH || path.join(__dirname, 'teamsync.db');
const db = new Database(DB_PATH);
db.pragma('journal_mode = WAL');
db.exec(fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8'));

const hash = (pwd) => {
  const salt = crypto.randomBytes(16).toString('hex');
  const h = crypto.scryptSync(pwd, salt, 64).toString('hex');
  return `${salt}:${h}`;
};

// ─── wipe ────────────────────────────────────────────────────────────────────
db.exec(`
  DELETE FROM carpool_passengers;
  DELETE FROM carpools;
  DELETE FROM rsvps;
  DELETE FROM matches;
  DELETE FROM team_members;
  DELETE FROM rotations;
  DELETE FROM teams;
  DELETE FROM drills;
  DELETE FROM users;
  DELETE FROM sqlite_sequence;
`);

// ─── users (password: teamsync) ──────────────────────────────────────────────
// Venue (gym): 41.3851, 2.1734 (Barcelona-ish centroid for demo distances)
const PWD = hash('teamsync');
// Self-evaluation per user — 0..5 floats keyed by skill
const lv = (spiking, setting, digging, serving, blocking, passing) =>
  JSON.stringify({ spiking, setting, digging, serving, blocking, passing });

const users = [
  { email: 'coach@teamsync.dev',  name: 'Marta Reyes',  role: 'coach',  sport: 'volleyball', position: 'Head Coach', gaps: '["serve","passing"]',    levels: lv(3.0, 4.5, 3.5, 4.0, 3.0, 4.0), car: 1, seats: 4, lat: 41.3950, lng: 2.1611 },
  { email: 'alex@teamsync.dev',   name: 'Alex Romero',  role: 'player', sport: 'volleyball', position: 'S',   gaps: '["setting","blocking"]', levels: lv(2.5, 3.0, 3.0, 3.5, 2.0, 3.5), car: 1, seats: 3, lat: 41.3781, lng: 2.1898 },
  { email: 'jordi@teamsync.dev',  name: 'Jordi Vives',  role: 'player', sport: 'volleyball', position: 'OH',  gaps: '["attack","passing"]',   levels: lv(3.5, 2.0, 2.5, 3.0, 2.5, 2.5), car: 0, seats: 0, lat: 41.4012, lng: 2.1530 },
  { email: 'noa@teamsync.dev',    name: 'Noa Ferrer',   role: 'player', sport: 'volleyball', position: 'MB',  gaps: '["blocking","serve"]',   levels: lv(3.0, 1.5, 2.0, 2.0, 2.5, 3.0), car: 0, seats: 0, lat: 41.3895, lng: 2.1655 },
  { email: 'lucia@teamsync.dev',  name: 'Lucia Mendez', role: 'player', sport: 'volleyball', position: 'OPP', gaps: '["attack","defense"]',   levels: lv(4.0, 2.5, 2.5, 3.5, 3.5, 3.0), car: 1, seats: 4, lat: 41.3700, lng: 2.1450 },
  { email: 'iker@teamsync.dev',   name: 'Iker Castaño', role: 'player', sport: 'volleyball', position: 'L',   gaps: '["passing","defense"]',  levels: lv(1.5, 2.5, 4.5, 3.0, 1.0, 3.0), car: 0, seats: 0, lat: 41.3830, lng: 2.1822 },
  { email: 'mireia@teamsync.dev', name: 'Mireia Pons',  role: 'player', sport: 'volleyball', position: 'OH',  gaps: '["serve","attack"]',     levels: lv(3.0, 2.0, 3.0, 2.0, 2.5, 3.5), car: 0, seats: 0, lat: 41.4080, lng: 2.1780 },
  { email: 'pol@teamsync.dev',    name: 'Pol Garrido',  role: 'player', sport: 'volleyball', position: 'S',   gaps: '["setting"]',            levels: lv(2.0, 3.0, 2.5, 3.0, 1.5, 3.0), car: 0, seats: 0, lat: 41.3760, lng: 2.1720 },
  { email: 'aina@teamsync.dev',   name: 'Aina Coll',    role: 'player', sport: 'volleyball', position: 'MB',  gaps: '["blocking"]',           levels: lv(3.5, 1.5, 2.0, 2.5, 3.0, 2.5), car: 0, seats: 0, lat: 41.3990, lng: 2.1690 },
];

const insUser = db.prepare(`
  INSERT INTO users (email, password_hash, name, role, sport, position, skill_gaps, skill_levels, has_car, car_seats, home_lat, home_lng)
  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
`);
const userIds = {};
db.transaction(() => {
  for (const u of users) {
    const r = insUser.run(
      u.email, PWD, u.name, u.role, u.sport, u.position, u.gaps, u.levels,
      u.car, u.seats, u.lat, u.lng,
    );
    userIds[u.email] = r.lastInsertRowid;
  }
})();

// ─── team ────────────────────────────────────────────────────────────────────
const teamId = db.prepare(`
  INSERT INTO teams (name, sport, coach_id, color_hex) VALUES (?, ?, ?, ?)
`).run('Lime Smashers', 'volleyball', userIds['coach@teamsync.dev'], '#39FF14').lastInsertRowid;

const insMember = db.prepare('INSERT INTO team_members (team_id, user_id, jersey) VALUES (?, ?, ?)');
const roster = [
  ['alex@teamsync.dev',   1],
  ['jordi@teamsync.dev',  7],
  ['noa@teamsync.dev',   11],
  ['lucia@teamsync.dev',  4],
  ['iker@teamsync.dev',   6],
  ['mireia@teamsync.dev', 9],
  ['pol@teamsync.dev',    3],
  ['aina@teamsync.dev',  12],
];
db.transaction(() => {
  for (const [email, jersey] of roster) insMember.run(teamId, userIds[email], jersey);
})();

// ─── matches ─────────────────────────────────────────────────────────────────
// Times relative to "now" so the dashboard always shows something live.
const now = new Date();
const inDays = (d, h = 19, m = 30) => {
  const x = new Date(now);
  x.setDate(x.getDate() + d);
  x.setHours(h, m, 0, 0);
  return x.toISOString().slice(0, 19).replace('T', ' ');
};

const insMatch = db.prepare(`
  INSERT INTO matches (team_id, opponent, venue, address, venue_lat, venue_lng, kickoff_at, is_home, status, score_us, score_them)
  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
`);
const matches = [
  // past
  { opp: 'Coastal Spikers',  venue: 'Sant Adrià Sports Hall',  addr: 'Av. del Litoral 240, 08930 Sant Adrià de Besòs',     lat: 41.4280, lng: 2.2210, when: inDays(-7),  home: 1, status: 'done', us: 3, them: 1 },
  { opp: 'Net Ninjas',       venue: 'Pabellón Vall d\'Hebron', addr: 'Pg. de la Vall d\'Hebron 171, 08035 Barcelona',       lat: 41.4290, lng: 2.1400, when: inDays(-3),  home: 0, status: 'done', us: 2, them: 3 },
  // upcoming
  { opp: 'Riverside Aces',   venue: 'Polideportivo La Mar',    addr: 'Carrer de la Marina 19, 08005 Barcelona',            lat: 41.3851, lng: 2.1734, when: inDays(2),   home: 1, status: 'scheduled' },
  { opp: 'Mountain Setters', venue: 'CEM Olímpia',             addr: 'Carrer de Bori i Fontestà 2, 08021 Barcelona',       lat: 41.3760, lng: 2.1500, when: inDays(6, 18, 0),  home: 0, status: 'scheduled' },
  { opp: 'Harbor Hawks',     venue: 'Polideportivo La Mar',    addr: 'Carrer de la Marina 19, 08005 Barcelona',            lat: 41.3851, lng: 2.1734, when: inDays(10),  home: 1, status: 'scheduled' },
  { opp: 'Volcano VBC',      venue: 'Pavelló Marítim',         addr: 'Pg. Marítim de la Barceloneta 33, 08003 Barcelona',  lat: 41.3700, lng: 2.1950, when: inDays(15, 11, 0), home: 0, status: 'scheduled' },
];
const matchIds = [];
db.transaction(() => {
  for (const m of matches) {
    matchIds.push(insMatch.run(
      teamId, m.opp, m.venue, m.addr, m.lat, m.lng, m.when,
      m.home, m.status, m.us ?? null, m.them ?? null,
    ).lastInsertRowid);
  }
})();

// ─── rsvps for the next upcoming match ───────────────────────────────────────
const nextMatchId = matchIds[2]; // Riverside Aces, +2 days
const insRsvp = db.prepare(`
  INSERT INTO rsvps (match_id, user_id, status, needs_ride) VALUES (?, ?, ?, ?)
`);
const rsvps = [
  ['alex@teamsync.dev',   'in',    0],
  ['jordi@teamsync.dev',  'in',    1],
  ['noa@teamsync.dev',    'in',    1],
  ['lucia@teamsync.dev',  'in',    0],
  ['iker@teamsync.dev',   'maybe', 0],
  ['mireia@teamsync.dev', 'in',    1],
  ['pol@teamsync.dev',    'out',   0],
  ['aina@teamsync.dev',   'in',    1],
];
db.transaction(() => {
  for (const [email, s, ride] of rsvps) insRsvp.run(nextMatchId, userIds[email], s, ride);
})();

// ─── carpools for the next match ─────────────────────────────────────────────
const insCarpool = db.prepare(`
  INSERT INTO carpools (match_id, driver_id, seats, depart_at, origin_lat, origin_lng) VALUES (?, ?, ?, ?, ?, ?)
`);
const insPax = db.prepare(`
  INSERT INTO carpool_passengers (carpool_id, user_id, pickup_lat, pickup_lng, stop_order) VALUES (?, ?, ?, ?, ?)
`);
// depart 45 min before kickoff
const departAt = (() => {
  const k = new Date(matches[2].when.replace(' ', 'T') + 'Z');
  k.setMinutes(k.getMinutes() - 45);
  return k.toISOString().slice(0, 19).replace('T', ' ');
})();

db.transaction(() => {
  // Coach's van
  const c1 = insCarpool.run(
    nextMatchId, userIds['coach@teamsync.dev'], 4, departAt,
    users[0].lat, users[0].lng,
  ).lastInsertRowid;
  insPax.run(c1, userIds['jordi@teamsync.dev'],  users[2].lat, users[2].lng, 0);
  insPax.run(c1, userIds['noa@teamsync.dev'],    users[3].lat, users[3].lng, 0);
  insPax.run(c1, userIds['mireia@teamsync.dev'], users[6].lat, users[6].lng, 0);

  // Lucia's car
  const c2 = insCarpool.run(
    nextMatchId, userIds['lucia@teamsync.dev'], 3, departAt,
    users[4].lat, users[4].lng,
  ).lastInsertRowid;
  insPax.run(c2, userIds['aina@teamsync.dev'], users[8].lat, users[8].lng, 0);

  // Alex's car (still has seats, no passengers yet)
  insCarpool.run(
    nextMatchId, userIds['alex@teamsync.dev'], 3, departAt,
    users[1].lat, users[1].lng,
  );
})();

// ─── drills ──────────────────────────────────────────────────────────────────
const insDrill = db.prepare(`
  INSERT INTO drills (sport, skill_tag, title, duration_s, video_url, thumbnail, difficulty) VALUES (?, ?, ?, ?, ?, ?, ?)
`);
const drills = [
  ['volleyball', 'serve',    'Jump-Serve Toss Consistency',     300, 'https://example.com/v/serve1.mp4',    '', 2],
  ['volleyball', 'serve',    'Float Serve Contact Drill',       240, 'https://example.com/v/serve2.mp4',    '', 1],
  ['volleyball', 'passing',  'Platform Angle Pepper',           360, 'https://example.com/v/pass1.mp4',     '', 1],
  ['volleyball', 'passing',  'Three-Line Reception Reps',       420, 'https://example.com/v/pass2.mp4',     '', 2],
  ['volleyball', 'setting',  'Back-Set Footwork Ladder',        300, 'https://example.com/v/set1.mp4',      '', 2],
  ['volleyball', 'attack',   'Slide Approach + Wash Drill',     480, 'https://example.com/v/attack1.mp4',   '', 3],
  ['volleyball', 'blocking', 'Block Footwork & Press',          300, 'https://example.com/v/block1.mp4',    '', 2],
  ['volleyball', 'defense',  'Read-Step Digging Reaction',      300, 'https://example.com/v/def1.mp4',      '', 2],
  ['basketball', 'shooting', '100 Free-Throw Routine',          600, 'https://example.com/b/shoot1.mp4',    '', 1],
  ['soccer',     'passing',  'One-Touch Wall Combinations',     300, 'https://example.com/s/pass1.mp4',     '', 1],
];
db.transaction(() => { for (const d of drills) insDrill.run(...d); })();

// ─── starting rotation ───────────────────────────────────────────────────────
db.prepare('INSERT INTO rotations (team_id, name, positions) VALUES (?, ?, ?)').run(
  teamId, 'Rotation 1 — S in P1',
  JSON.stringify([
    { pos: 1, role: 'S',   jersey: 1,  rx: 0.78, ry: 0.72 },
    { pos: 2, role: 'OH',  jersey: 7,  rx: 0.78, ry: 0.28 },
    { pos: 3, role: 'MB',  jersey: 11, rx: 0.50, ry: 0.22 },
    { pos: 4, role: 'OPP', jersey: 4,  rx: 0.22, ry: 0.28 },
    { pos: 5, role: 'L',   jersey: 6,  rx: 0.22, ry: 0.72 },
    { pos: 6, role: 'OH',  jersey: 9,  rx: 0.50, ry: 0.78 },
  ]),
);

// ─── report ──────────────────────────────────────────────────────────────────
const n = (t) => db.prepare(`SELECT COUNT(*) AS c FROM ${t}`).get().c;
console.log(`\n✓ Seeded TeamSync demo data:
  users:    ${n('users')}
  teams:    ${n('teams')}
  matches:  ${n('matches')}
  rsvps:    ${n('rsvps')}
  carpools: ${n('carpools')}
  passngrs: ${n('carpool_passengers')}
  drills:   ${n('drills')}
  rotations:${n('rotations')}

Demo login (any user, password = "teamsync"):
  coach@teamsync.dev   — Marta Reyes (coach, driver)
  alex@teamsync.dev    — Alex Romero (Setter, driver)
  jordi@teamsync.dev   — Jordi Vives (Outside Hitter)
  lucia@teamsync.dev   — Lucia Mendez (Opposite, driver)
  iker@teamsync.dev    — Iker Castaño (Libero)
  noa@teamsync.dev     — Noa Ferrer (Middle Blocker)
\n`);
