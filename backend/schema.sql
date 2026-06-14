PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

CREATE TABLE IF NOT EXISTS users (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  email         TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  name          TEXT NOT NULL,
  role          TEXT NOT NULL DEFAULT 'player' CHECK(role IN ('player','coach','admin')),
  sport         TEXT NOT NULL DEFAULT 'volleyball' CHECK(sport IN ('volleyball','basketball','soccer')),
  position      TEXT,
  skill_gaps    TEXT NOT NULL DEFAULT '[]',
  skill_levels  TEXT NOT NULL DEFAULT '{}',
  avatar_seed   TEXT,
  has_car       INTEGER NOT NULL DEFAULT 0,
  car_seats     INTEGER NOT NULL DEFAULT 0,
  home_lat      REAL,
  home_lng      REAL,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS teams (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  name       TEXT NOT NULL,
  sport      TEXT NOT NULL,
  coach_id   INTEGER REFERENCES users(id) ON DELETE SET NULL,
  color_hex  TEXT DEFAULT '#22d3ee',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS team_members (
  team_id INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  jersey  INTEGER,
  PRIMARY KEY (team_id, user_id)
);

CREATE TABLE IF NOT EXISTS matches (
  id           INTEGER PRIMARY KEY AUTOINCREMENT,
  team_id      INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
  opponent     TEXT NOT NULL,
  venue        TEXT NOT NULL,
  address      TEXT,
  venue_lat    REAL,
  venue_lng    REAL,
  kickoff_at   DATETIME NOT NULL,
  is_home      INTEGER NOT NULL DEFAULT 1,
  status       TEXT NOT NULL DEFAULT 'scheduled' CHECK(status IN ('scheduled','live','done','cancelled')),
  score_us     INTEGER,
  score_them   INTEGER,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rsvps (
  id        INTEGER PRIMARY KEY AUTOINCREMENT,
  match_id  INTEGER NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
  user_id   INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status    TEXT NOT NULL CHECK(status IN ('in','out','maybe')),
  needs_ride INTEGER NOT NULL DEFAULT 0,
  responded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(match_id, user_id)
);

CREATE TABLE IF NOT EXISTS carpools (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  match_id   INTEGER NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
  driver_id  INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  seats      INTEGER NOT NULL,
  depart_at  DATETIME NOT NULL,
  origin_lat REAL,
  origin_lng REAL,
  route_json TEXT
);

CREATE TABLE IF NOT EXISTS carpool_passengers (
  carpool_id INTEGER NOT NULL REFERENCES carpools(id) ON DELETE CASCADE,
  user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  pickup_lat REAL,
  pickup_lng REAL,
  stop_order INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (carpool_id, user_id)
);

CREATE TABLE IF NOT EXISTS drills (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  sport       TEXT NOT NULL,
  skill_tag   TEXT NOT NULL,
  title       TEXT NOT NULL,
  duration_s  INTEGER NOT NULL,
  video_url   TEXT NOT NULL,
  thumbnail   TEXT,
  difficulty  INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS rotations (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  team_id    INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
  name       TEXT NOT NULL,
  positions  TEXT NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_matches_team_kickoff ON matches(team_id, kickoff_at);
CREATE INDEX IF NOT EXISTS idx_rsvps_match ON rsvps(match_id);
CREATE INDEX IF NOT EXISTS idx_drills_sport_skill ON drills(sport, skill_tag);
