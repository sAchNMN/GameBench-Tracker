# 测试计划

## 当前已完成阶段结果

后端共有 62 个 JUnit 5 测试，`mvn "-Dmaven.repo.local=G:\Code\gamebench-tracker\MavenRepo" clean test` 通过。

`GameControllerIntegrationTest` 覆盖游戏 CRUD；`TestSceneControllerIntegrationTest` 覆盖场景创建、游戏归属、同游戏重名冲突、按游戏列表、时长校验、编辑、删除和删除游戏级联场景。真实运行库已验证重启后仍能读取 `CS2 / Steam` 与其场景。

`ConfigTemplateControllerIntegrationTest` 覆盖完整模板持久化、不存在游戏、同游戏模板重名冲突、功耗限制边界、按游戏列表、编辑、删除和删除游戏级联模板。

前端未配置测试脚本；`npm.cmd run build` 已通过类型检查和 Vite 生产构建。

## 后续测试规范



## 测试原则

自动化测试必须随功能阶段同步完成。

不得把所有测试推迟到 P14。

P14 只做全量回归、补漏和覆盖审计。



## 后端测试

每个后端功能至少包含对应测试：

- Service 单元测试。
- Controller 接口测试。
- SQLite 集成测试。
- Jakarta Validation 参数校验测试。
- 全局异常处理测试。
- 数据库异常转换测试。

删除功能必须同步测试关联数据策略。



## 前端测试

前端功能至少覆盖：

- 表单校验。
- API 错误提示。
- 空列表。
- 加载状态。
- 删除确认。
- 提交期间按钮禁用。
- 对比选择限制。
- 图表无数据状态。



## 测试记录校验

新增和编辑测试记录必须测试：

- `gameId` 必填。
- `sceneId` 必填。
- 场景必须存在。
- 场景必须属于所选游戏。
- 场景快照被复制。
- 模板字段被复制。
- `averageFps` 必填且大于 0。
- 其他 FPS 指标可以为空。
- 其他 FPS 指标可以为 0。
- 其他 FPS 指标不能为负数。
- `gpu_power_limit_percent` 允许负数、0 和正数。
- `gpu_power_limit_percent` 小于 -100 或大于 100 时失败。



## 对比计算固定数据

测试 A：

- 平均 FPS：50
- 1% Low：40
- 0.1% Low：30
- GPU 平均功耗：100W

测试 B：

- 平均 FPS：60
- 1% Low：44
- 0.1% Low：33
- GPU 平均功耗：80W

必须验证：

- 平均 FPS 提升 20%。
- 1% Low 提升 10%。
- 0.1% Low 提升 10%。
- 功耗下降 20%。
- A 的 FPS/W 为 0.5。
- B 的 FPS/W 为 0.75。
- B 的能效相对 A 提升 50%。



## 对比边界测试

必须覆盖：

- 旧值为 0。
- 新值为空。
- 旧值为空。
- 功耗为空。
- 功耗为 0。
- 负数输入。
- 超大数值。
- 小数精度。
- 两条不同游戏记录不能对比。



## CSV 导出测试

必须覆盖：

- UTF-8 with BOM。
- 中文 Excel 打开不乱码。
- CRLF 换行。
- 空值为空单元格。
- 数值不带单位。
- 小数保留 2 位。
- 文件名包含业务范围和时间。
- 导出失败返回统一错误。



## 全局异常处理测试

T008.2B 使用普通 JUnit 5 单元测试，直接调用 GlobalExceptionHandler，覆盖：

- ApplicationException 的 409、404 响应与 details 保留。
- JSON 解析错误的 400 响应和解析器消息隔离。
- 未预期异常的 500 响应和异常消息隔离。
- 所有失败响应的 success、data、error 与 timestamp 约束。

T008.3 使用 WebMvcTest、MockMvc 与 test 源码中的 ValidationProbeController 验证：

- POST /test/validation/body：合法请求、MethodArgumentNotValidException、JSON 解析错误。
- GET /test/validation/query：HandlerMethodValidationException、缺参和类型错误。
- GET /test/validation/path/{id}：HandlerMethodValidationException。
- GET /test/errors/application、/test/errors/unexpected：应用异常与兜底异常。
- resolvedException 实际类型、统一 JSON 响应和 ISO 8601 timestamp。

游戏 API 已有生产 Controller 和 SQLite 集成测试；ValidationProbeController 仍只位于 test 源码。

## 回归检查

P14 执行：

- 全量后端测试。
- 全量前端测试。
- 关键用户流程手测。
- 文档和实现一致性检查。
- 覆盖缺口审计。
