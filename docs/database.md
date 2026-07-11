# SQLite SQL 草案

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE game (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  platform TEXT NOT NULL DEFAULT '',
  remark TEXT,
  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  UNIQUE (name, platform)
);

CREATE INDEX idx_game_name ON game(name);

CREATE TABLE test_scene (
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

CREATE INDEX idx_scene_game_id ON test_scene(game_id);

CREATE TABLE config_template (
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

CREATE INDEX idx_template_game_id ON config_template(game_id);

CREATE TABLE benchmark_record (
  id INTEGER PRIMARY KEY AUTOINCREMENT,

  game_id INTEGER NOT NULL,
  scene_id INTEGER,
  config_template_id INTEGER,

  scene_name_snapshot TEXT NOT NULL,
  scene_method_snapshot TEXT NOT NULL,
  scene_duration_seconds_snapshot INTEGER,

  test_date TEXT NOT NULL,
  game_version TEXT,

  resolution TEXT NOT NULL,
  graphics_preset TEXT NOT NULL,
  upscaling_tech TEXT,
  upscaling_quality TEXT,
  vsync_enabled INTEGER,
  frame_generation_enabled INTEGER,

  cpu_name TEXT,
  gpu_name TEXT,
  memory_gb NUMERIC,
  driver_version TEXT,

  gpu_core_clock_mhz NUMERIC,
  gpu_voltage_mv NUMERIC,
  gpu_memory_clock_mhz NUMERIC,
  gpu_power_limit_percent NUMERIC,

  average_fps NUMERIC NOT NULL,
  min_fps NUMERIC,
  one_percent_low_fps NUMERIC,
  zero_one_percent_low_fps NUMERIC,

  gpu_avg_usage_percent NUMERIC,
  cpu_avg_usage_percent NUMERIC,
  vram_usage_mb NUMERIC,
  ram_usage_gb NUMERIC,

  gpu_temp_c NUMERIC,
  gpu_hotspot_temp_c NUMERIC,
  cpu_temp_c NUMERIC,
  gpu_avg_power_w NUMERIC,

  remark TEXT,

  created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),

  CHECK (scene_duration_seconds_snapshot IS NULL OR scene_duration_seconds_snapshot > 0),
  CHECK (vsync_enabled IS NULL OR vsync_enabled IN (0, 1)),
  CHECK (frame_generation_enabled IS NULL OR frame_generation_enabled IN (0, 1)),

  CHECK (gpu_core_clock_mhz IS NULL OR gpu_core_clock_mhz >= 0),
  CHECK (gpu_voltage_mv IS NULL OR gpu_voltage_mv >= 0),
  CHECK (gpu_memory_clock_mhz IS NULL OR gpu_memory_clock_mhz >= 0),
  CHECK (gpu_power_limit_percent IS NULL OR gpu_power_limit_percent BETWEEN -100 AND 100),

  CHECK (average_fps > 0),
  CHECK (min_fps IS NULL OR min_fps >= 0),
  CHECK (one_percent_low_fps IS NULL OR one_percent_low_fps >= 0),
  CHECK (zero_one_percent_low_fps IS NULL OR zero_one_percent_low_fps >= 0),

  CHECK (gpu_avg_usage_percent IS NULL OR gpu_avg_usage_percent BETWEEN 0 AND 100),
  CHECK (cpu_avg_usage_percent IS NULL OR cpu_avg_usage_percent BETWEEN 0 AND 100),

  CHECK (vram_usage_mb IS NULL OR vram_usage_mb >= 0),
  CHECK (ram_usage_gb IS NULL OR ram_usage_gb >= 0),

  CHECK (gpu_temp_c IS NULL OR gpu_temp_c >= 0),
  CHECK (gpu_hotspot_temp_c IS NULL OR gpu_hotspot_temp_c >= 0),
  CHECK (cpu_temp_c IS NULL OR cpu_temp_c >= 0),
  CHECK (gpu_avg_power_w IS NULL OR gpu_avg_power_w >= 0),

  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
  FOREIGN KEY (scene_id) REFERENCES test_scene(id) ON DELETE SET NULL,
  FOREIGN KEY (config_template_id) REFERENCES config_template(id) ON DELETE SET NULL
);

CREATE INDEX idx_record_game_id ON benchmark_record(game_id);
CREATE INDEX idx_record_scene_id ON benchmark_record(scene_id);
CREATE INDEX idx_record_template_id ON benchmark_record(config_template_id);
CREATE INDEX idx_record_test_date ON benchmark_record(test_date);
CREATE INDEX idx_record_game_scene_date ON benchmark_record(game_id, scene_id, test_date);
```
