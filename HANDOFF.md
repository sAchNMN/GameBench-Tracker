# HANDOFF

## 当前状态

已完成 Phase 0 文档。

Git 仓库已初始化。
已建立纯文档基线提交。
T006 已完成。
T006.1 诊断已完成。
Spring Boot 空项目已初始化。
尚未初始化 Vue 项目。
尚未写业务代码。
当前无业务代码。
尚未执行 SQL。
尚未创建 SQLite 数据库。
当前无数据库。
当前无前端。

## 本阶段已完成

- 创建项目 README。
- 创建 AI 开发规则。
- 创建项目结构规划。
- 创建需求与边界文档。
- 创建架构文档。
- 创建数据库 SQL 草案。
- 创建 API 契约文档。
- 创建开发路线。
- 创建测试计划。
- 创建检查清单。
- 创建错误日志模板。
- 创建 `.gitignore`。
- 建立 Phase 0 纯文档基线提交。
- 创建最小 Spring Boot 后端空项目。
- 创建 Spring Boot ApplicationContext 加载测试。

## 关键设计结论

- `game` 表不保存 `version`。
- `game` 唯一约束为 `UNIQUE(name, platform)`。
- `platform` 为 `NOT NULL DEFAULT ''`。
- 游戏版本只保存在 `benchmark_record.game_version`。
- 显卡功耗限制统一使用 `gpu_power_limit_percent`。
- `gpu_power_limit_percent` 允许 -100 到 100。
- 实际平均功耗使用 `gpu_avg_power_w`。
- `average_fps` 必填，且必须大于 0。
- 新增测试记录时 `scene_id` 必填。
- 场景和模板都要复制快照到历史记录。
- DELETE 接口返回 HTTP 200 和统一 `ApiResponse`。
- 统一响应、错误码、全局异常、Jakarta Validation、数据库异常转换在基础阶段完成。
- P12 只做统一性审计和遗漏修复。
- 自动化测试随功能阶段同步完成。

## 环境问题

已定位：原项目路径包含中文，Spring Boot Maven Plugin 在 `spring-boot:run` fork classpath 中把路径写成乱码，导致子进程找不到 `com.gamebench.tracker.GameBenchTrackerApplication`。

英文路径副本中，`mvn clean test` 和 `mvn spring-boot:run` 均成功。

未解决：默认 Maven 本地仓库 `%USERPROFILE%\.m2` ACL 显示当前用户 FullControl，但普通文件写入测试失败。当前仍需临时使用 `%TEMP%\gamebench-m2` 或由用户手动修复 `.m2` 写入权限。

## 尚未完成

- T007 建立统一响应结构。
- T008 建立全局异常和校验骨架。
- T009 接入 SQLite 基础。
- T010 以后所有后端和前端功能任务。

## 下一步

下一步只能建议执行 T007：建立统一响应和错误码基础结构。

不要自动执行 T007。

## 接手提示

接手前先读：

1. `docs/requirements.md`
2. `docs/database.md`
3. `docs/api.md`
4. `docs/development-roadmap.md`
5. `docs/test-plan.md`

下一步任务是 T007。执行 T007 前先运行 `git status --short`，确认工作区状态。
