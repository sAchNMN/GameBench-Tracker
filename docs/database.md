# SQLite 数据库设计

## 当前实施状态

`src/main/resources/schema.sql` 当前实际创建 `game`、`test_scene`、`config_template` 及其索引。`game` 使用 `UNIQUE(name, platform)`，`platform` 为 `NOT NULL DEFAULT ''`；`test_scene` 使用 `UNIQUE(game_id, name)`、可选正数 `duration_seconds` 和 `ON DELETE CASCADE` 外键；`config_template` 使用 `UNIQUE(game_id, name)`、布尔开关约束、非负频率/电压约束、`gpu_power_limit_percent` 的 -100 到 100 约束和 `ON DELETE CASCADE` 外键。创建和更新时间由 SQLite 以 UTC ISO 8601 文本生成。

运行库位于项目根目录 `gamebench-tracker.db`，已被 Git 忽略。测试使用独立共享内存 SQLite。

下面保留完整结构规划。`config_template` 已写入运行 schema；`benchmark_record` 尚未写入，必须在对应纵向切片完成时创建。

## 全量数据库规划（后续表尚未创建）

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
