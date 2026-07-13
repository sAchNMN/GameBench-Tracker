CREATE TABLE IF NOT EXISTS game (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  platform TEXT NOT NULL DEFAULT '',
  remark TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  UNIQUE (name, platform)
);

CREATE INDEX IF NOT EXISTS idx_game_name ON game(name);

CREATE TABLE IF NOT EXISTS test_scene (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  game_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  method TEXT NOT NULL,
  duration_seconds INTEGER,
  remark TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  UNIQUE (game_id, name),
  CHECK (duration_seconds IS NULL OR duration_seconds > 0),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_scene_game_id ON test_scene(game_id);

CREATE TABLE IF NOT EXISTS config_template (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  game_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  resolution TEXT,
  graphics_preset TEXT,
  upscaling_tech TEXT,
  upscaling_quality TEXT,
  vsync_enabled INTEGER,
  frame_generation_enabled INTEGER,
  gpu_core_clock_mhz NUMERIC,
  gpu_voltage_mv NUMERIC,
  gpu_memory_clock_mhz NUMERIC,
  gpu_power_limit_percent NUMERIC,
  driver_version TEXT,
  custom_description TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  UNIQUE (game_id, name),
  CHECK (vsync_enabled IS NULL OR vsync_enabled IN (0, 1)),
  CHECK (frame_generation_enabled IS NULL OR frame_generation_enabled IN (0, 1)),
  CHECK (gpu_core_clock_mhz IS NULL OR gpu_core_clock_mhz >= 0),
  CHECK (gpu_voltage_mv IS NULL OR gpu_voltage_mv >= 0),
  CHECK (gpu_memory_clock_mhz IS NULL OR gpu_memory_clock_mhz >= 0),
  CHECK (gpu_power_limit_percent IS NULL OR gpu_power_limit_percent BETWEEN -100 AND 100),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_template_game_id ON config_template(game_id);