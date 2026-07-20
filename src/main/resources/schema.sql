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

CREATE TABLE IF NOT EXISTS benchmark_record (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  game_id INTEGER NOT NULL,
  scene_id INTEGER,
  template_id INTEGER,
  recorded_at TEXT,
  avg_fps NUMERIC,
  min_fps NUMERIC,
  gpu_temp_celsius NUMERIC,
  cpu_temp_celsius NUMERIC,
  gpu_power_watt NUMERIC,
  cpu_usage_percent NUMERIC,
  frame_time_ms NUMERIC,
  notes TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  CHECK (avg_fps IS NULL OR avg_fps > 0),
  CHECK (min_fps IS NULL OR min_fps > 0),
  CHECK (gpu_temp_celsius IS NULL OR gpu_temp_celsius >= -273.15),
  CHECK (cpu_temp_celsius IS NULL OR cpu_temp_celsius >= -273.15),
  CHECK (gpu_power_watt IS NULL OR gpu_power_watt >= 0),
  CHECK (cpu_usage_percent IS NULL OR (cpu_usage_percent >= 0 AND cpu_usage_percent <= 100)),
  CHECK (frame_time_ms IS NULL OR frame_time_ms > 0),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
  FOREIGN KEY (scene_id) REFERENCES test_scene(id) ON DELETE SET NULL,
  FOREIGN KEY (template_id) REFERENCES config_template(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_record_game_id ON benchmark_record(game_id);
CREATE INDEX IF NOT EXISTS idx_record_scene_id ON benchmark_record(scene_id);
CREATE INDEX IF NOT EXISTS idx_record_template_id ON benchmark_record(template_id);