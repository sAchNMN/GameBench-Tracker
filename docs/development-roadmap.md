# 开发路线

## 已完成

- P0：需求、架构、数据库、API、测试和路线文档。
- P1：Spring Boot、统一响应、错误码、Web 校验和全局异常处理。
- 阶段 A：SQLite、MyBatis-Plus、`game` 表、游戏 CRUD API、游戏管理 Vue 页面、持久化与构建验证。
- 阶段 B：`test_scene` 表、场景 CRUD、游戏归属校验、游戏删除级联策略、场景管理前端与持久化验证。
- 阶段 C 后端：`config_template` 表、模板 CRUD、游戏归属校验、功耗限制范围校验与 SQLite 集成测试。
- 阶段 C 前端：配置模板管理页面（从游戏进入、新增/编辑/删除确认、空/加载/错误状态、`gpu_power_limit_percent` 前端 -100~100 校验、前端构建验证）。
- 阶段 D 后端：`benchmark_record` 表、记录 CRUD、游戏归属与可选场景/模板存在性校验、字段 CHECK 约束与 SQLite 集成测试。
- 阶段 D 前端：性能测试记录页面（从游戏进入、新增/编辑（历史编辑提示）/删除确认、空/加载/错误状态、必填 `avgFps`/`minFps`/`frameTimeMs`>0 与温度/功耗/CPU 占用范围前端校验、前端构建验证）。

## 当前下一阶段

阶段 E：双记录对比。

1. 新增 `POST /api/benchmark-records/compare`，校验两条记录存在且同属一游戏。
2. 返回平均/1% Low/0.1% Low FPS 变化率、GPU 功耗变化率与下降率、GPU/热点温度差异、FPS/W。
3. 旧值或功耗为空/0 时不计算对应变化率。
4. 提供双记录对比前端页面。
5. 更新文档并建立独立提交。

## 后续阶段

- 阶段 F：ECharts 图表与 CSV 导出。
- 阶段 G：前后端联调、全量回归、运行说明和交付审查。
