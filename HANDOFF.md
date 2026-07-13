# HANDOFF

## 当前状态

稳定基线为 `2b7bcef feat(scene): add test scene management`。

阶段 A、B 已完成并提交。阶段 C 的配置模板后端已完成，待本次提交。

- SQLite、MyBatis-Plus 已接入；SQLite 外键已启用。
- `schema.sql` 已创建 `game`、`test_scene`、`config_template` 及索引。
- 已完成游戏、测试场景与配置模板的 CRUD API。
- 模板必须归属现有游戏；模板名称在同一游戏内唯一。
- `gpu_power_limit_percent` 允许 -100 到 100；频率和电压只能为空或非负数。
- 删除游戏会级联删除场景和配置模板。
- 后端 `mvn "-Dmaven.repo.local=G:\Code\gamebench-tracker\MavenRepo" clean test`：62 个测试通过。
- 前端游戏和场景页面构建已通过；配置模板前端页面尚未创建。

## 本次提交

- `config_template`：Entity、Mapper、DTO、VO、Service、Controller 与 SQLite schema。
- 模板错误码 `TEMPLATE_NOT_FOUND` 及 HTTP 404 映射。
- `ConfigTemplateControllerIntegrationTest`：完整字段持久化、不存在游戏、同游戏重名、功耗边界、按游戏列表、编辑、删除和游戏级联删除。
- README、架构、API、数据库、路线和测试文档同步到实际状态。

## 运行与数据

运行数据库为项目根目录 `gamebench-tracker.db`，不提交。

当前开发机使用 `G:\Code\gamebench-tracker\MavenRepo` 作为 Maven 本地仓库。默认 `%USERPROFILE%\.m2` 写入问题未处理，不影响项目验证。

## 下一步

阶段 C 前端部分：从游戏页面进入配置模板管理，完成模板新增、编辑、删除确认、空/加载/错误状态和功耗范围前端校验。
