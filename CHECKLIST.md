# CHECKLIST



## 范围检查

- 当前阶段是否只实现计划中的完整纵向切片。
- 是否没有把登录、复杂权限、Redis、MySQL、Docker、实时采集、硬件传感器读取放入 MVP。
- 是否没有把测试 Controller 写入 `src/main/java`。
- 是否没有提交运行数据库、日志、构建产物或本地依赖缓存。

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
- 场景是否必须属于现有游戏。
- 场景名是否只在同一游戏内唯一。
- 场景时长为空或大于 0。
- 删除游戏时是否级联删除场景。
- 配置模板名是否只在同一游戏内唯一。
- 删除游戏时是否级联删除配置模板。
- 模板的 `gpu_power_limit_percent` 是否允许 -100 到 100。
- 模板的频率与电压是否只能为空或非负数。



## API 检查

- 是否使用统一 `ApiResponse`。
- 成功响应是否不包含 `error`。
- 失败响应是否不包含 `data`。
- API 时间是否统一使用 UTC `Instant`。
- DELETE 是否返回 HTTP 200。
- 是否没有使用 HTTP 204。
- 业务错误是否有统一错误码。
- Validation 错误是否能转换成统一响应。
- 数据库异常是否能转换成统一响应。
- 错误码是否没有重复。
- 错误码是否没有依赖中文消息判断。
- ErrorCode 是否没有直接依赖 HttpStatus。
- 新增 ErrorCode 时是否同步审查 HTTP 映射。
- cause 是否没有暴露到 ApiError.details。
- ApplicationException.details 是否不可修改。
- 全局异常处理器是否没有直接返回堆栈信息。
- 500 响应是否没有暴露异常消息或堆栈。
- 校验响应是否没有返回 rejectedValue。
- JSON 解析错误是否没有暴露底层解析器消息。
- 未预期异常是否写入内部 error 日志。
- 客户端参数错误是否没有污染 error 日志。
- 新增 Controller 参数约束时是否验证实际异常类型。
- Controller 类是否没有随意添加 @Validated。
- 方法参数校验是否返回统一 ApiResponse。
- 缺参和类型错误是否没有返回 500。
- MockMvc 测试是否同时断言响应结构而非只断言状态码。
- 测试专用 Controller 是否没有进入 main 源码。



## 测试检查

- 后端修改后是否运行 `mvn test` 或等价 Maven 测试命令。
- Maven 测试是否全部通过。
- `mvn clean test` 是否可以正常删除并重建 `target`。
- 每个后端功能是否同步完成 Service、Controller 或集成测试。
- 删除功能是否同步测试关联数据策略。
- 对比功能是否同步完成固定数据和边界测试。
- P14 是否只做全量回归、补漏和覆盖审计。



## Maven 环境检查

- 项目路径是否为纯英文短路径。
- 项目路径是否不包含中文、空格和特殊字符。
- Maven 命令是否没有使用管理员权限。
- 开发前是否确认 Maven 本地仓库可写。
- 当前稳定 Maven 本地仓库是否为项目内 `G:\Code\gamebench-tracker\MavenRepo`。
- 默认 Maven 本地仓库 `%USERPROFILE%\.m2\repository` 是否可写。
- 如果默认仓库不可写，是否明确记录为环境问题。
- 是否避免把临时仓库当作长期解决方案。
- `clean` 是否可以正常删除 `target`。



## 依赖检查

- `pom.xml` 是否只加入当前任务需要的依赖。
- 是否没有无需求依赖。
- 新依赖是否有当前任务的直接用途。
- 是否没有重复声明传递依赖已经提供的 Starter。
- 引入 Web 后是否验证应用可启动并正常停止。
- Validation 依赖存在是否未被误写为参数校验逻辑已经实现。
- MyBatis-Plus、SQLite JDBC 是否只用于当前本地持久化；是否没有加入 Lombok、MapStruct、Actuator、DevTools。



## Git 检查

- 修改前是否运行 `git status --short`。
- 如果不是 Git 仓库，是否只报告事实且没有执行 `git init`。
- 修改后是否运行 `git diff --check`。
- 提交前是否运行 `git status --short`。
- 提交前是否检查暂存文件范围。
- 暂存区是否只包含当前任务允许提交的文件。
- 是否没有提交临时文件、日志、数据库文件、构建产物。
- 是否禁止提交数据库、日志、构建产物和 IDE 文件。
- `target/` 是否没有进入 Git 暂存区。
- 是否没有提交 IDE 配置文件。
- 是否没有提交 SQLite 运行数据库文件。
- 是否没有在用户未要求时 commit。



## 文档同步检查

- README 是否描述当前阶段。
- HANDOFF 是否记录下一步。
- PROJECT_MAP 是否和实际目录一致。
- ERROR_LOG 是否只记录真实错误。
- docs/database.md 是否区分已执行 schema 与后续 SQL 规划。
- docs/api.md 是否只保存接口契约。
