# CHECKLIST

## 范围检查

- 当前任务是否只修改允许文件。
- 是否没有初始化 Spring Boot。
- 是否没有初始化 Vue。
- 是否没有写业务代码。
- 是否没有执行 SQL。
- 是否没有创建 SQLite 数据库。
- 是否没有创建构建产物。
- 是否没有把登录、复杂权限、Redis、MySQL、Docker、实时采集、硬件传感器读取放入 MVP。

## 数据设计检查

- `game` 是否没有 `version` 字段。
- `game` 是否使用 `UNIQUE(name, platform)`。
- `platform` 是否为 `NOT NULL DEFAULT ''`。
- `gpu_power_limit_percent` 是否允许 -100 到 100。
- `gpu_avg_power_w` 是否继续表示实际平均功耗。
- `average_fps` 是否必填且大于 0。
- 其他 FPS 指标是否允许为空或 0，但不允许负数。
- 新增测试记录时 `scene_id` 是否必填。
- 场景快照是否包含名称、方法、测试时长。
- 模板字段是否在保存记录时复制到历史记录。

## API 检查

- 是否使用统一 `ApiResponse`。
- DELETE 是否返回 HTTP 200。
- 是否没有使用 HTTP 204。
- 业务错误是否有统一错误码。
- Validation 错误是否能转换成统一响应。
- 数据库异常是否能转换成统一响应。

## 测试检查

- 后端修改后是否运行 `mvn test`。
- Maven 测试是否全部通过。
- 每个后端功能是否同步完成 Service、Controller 或集成测试。
- 删除功能是否同步测试关联数据策略。
- 对比功能是否同步完成固定数据和边界测试。
- P14 是否只做全量回归、补漏和覆盖审计。

## Maven 环境检查

- 项目路径是否为纯英文短路径。
- 默认 Maven 本地仓库 `%USERPROFILE%\.m2\repository` 是否可写。
- 如果默认仓库不可写，是否明确记录为环境问题。
- 是否避免把临时仓库当作长期解决方案。

## 依赖检查

- `pom.xml` 是否只加入当前任务需要的依赖。
- 是否没有无需求依赖。
- 空后端阶段是否没有加入 Web、Validation、MyBatis-Plus、SQLite JDBC、Lombok、MapStruct、Actuator、DevTools。

## Git 检查

- 修改前是否运行 `git status --short`。
- 如果不是 Git 仓库，是否只报告事实且没有执行 `git init`。
- 修改后是否运行 `git diff --check`。
- 提交前是否运行 `git status --short`。
- 提交前是否检查暂存文件范围。
- 暂存区是否只包含当前任务允许提交的文件。
- 是否没有提交临时文件、日志、数据库文件、构建产物。
- `target/` 是否没有进入 Git 暂存区。
- 是否没有提交 IDE 配置文件。
- 是否没有提交 SQLite 运行数据库文件。
- 是否没有在用户未要求时 commit。

## 文档同步检查

- README 是否描述当前阶段。
- HANDOFF 是否记录下一步。
- PROJECT_MAP 是否和实际目录一致。
- ERROR_LOG 是否只记录真实错误。
- docs/database.md 是否只保存 SQL 草案。
- docs/api.md 是否只保存接口契约。
