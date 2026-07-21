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
- 阶段 E：双记录对比后端（`POST /api/benchmark-records/compare`，同游戏校验、同记录拒绝、FPS/帧时间/功耗变化率与下降率、温度/占用差异、FPS/W 及其变化率，空值/零值返回 null，集成测试）与前端对比页面（从游戏进入、双下拉选记录、结果表按性能向好着色、空/加载/错误状态、前端构建验证）。
- 阶段 F：CSV 导出后端（`GET /api/games/{gameId}/records/export`，UTF-8 BOM + CRLF + RFC4180 转义、按 recordedAt 升序、游戏不存在 404、无记录仅表头，集成测试）与前端趋势图表（ECharts 折线，平均/最低 FPS、GPU 功耗/温度按时间升序切换）及 CSV 导出入口（fetch + Blob 下载）。

## 当前下一阶段

阶段 G：前后端联调、全量回归、运行说明和交付审查。

1. 端到端联调：前端 dev server 连接后端，验证游戏/场景/模板/记录/对比/图表/导出全链路。
2. 全量回归：后端 `mvn verify` 与前端 `npm run build` 门禁复跑。
3. 编写运行说明（启动后端、前端 dev/build、数据库位置）。
4. 交付审查：检查文档一致性与契约漂移。

## 后续阶段

- 阶段 F 已完成；阶段 G 为收尾阶段。
