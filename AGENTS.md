# GameBench Tracker AI 开发规则

## 当前阶段

当前仓库只完成 Phase 0 文档。

尚未初始化后端项目，尚未初始化前端项目，尚未写业务代码。

## 工作原则

- 每次修改前先确认任务阶段。
- 只修改当前任务允许的文件。
- 不把后续版本功能提前塞进 MVP。
- 不把一个模块一次性做完。
- 后端功能必须同步补 Service、Controller 或集成测试。
- 删除功能必须同步测试关联数据策略。
- 文档必须随阶段同步更新。

## MVP 禁止事项

以下内容不得进入 MVP：

- 登录、注册、多用户、复杂权限。
- Redis、MySQL、PostgreSQL、Docker、微服务。
- 云同步、AI 分析、AI 推荐配置。
- 实时采集、硬件传感器读取、后台常驻、定时任务。
- 显卡控制、驱动控制、游戏进程注入。
- PDF 导出、移动端、多语言、主题系统。

## 数据规则

- `game` 表不保存游戏版本。
- 游戏版本只保存在 `benchmark_record.game_version`。
- `game` 唯一约束为 `UNIQUE(name, platform)`。
- `platform` 必须为 `NOT NULL DEFAULT ''`。
- 显卡功耗限制字段统一为 `gpu_power_limit_percent`。
- `gpu_power_limit_percent` 表示功耗限制调整百分比，允许 -100 到 100。
- 实际平均功耗字段为 `gpu_avg_power_w`，单位 W。
- `average_fps` 必填，且必须大于 0。
- 其他 FPS 指标可以为空，也可以为 0，但不能为负数。

## 快照规则

新增测试记录时，`scene_id` 必填。

Service 必须校验：

- 场景存在。
- 场景属于所选游戏。
- 场景名称、方法、测试时长复制到记录快照字段。

配置模板规则：

- 选择模板时，将模板字段复制到 `benchmark_record`。
- 保存后，历史记录不依赖模板当前值。
- 编辑或删除模板不得改变历史记录。
- `config_template_id` 只用于记录来源。

## 历史记录编辑

允许编辑历史测试记录，用于修正录入错误。

编辑后必须更新 `updated_at`。界面必须提示正在修改历史测试数据。

MVP 不做修改历史审计。

## API 规则

- 使用 REST 风格。
- 使用统一 `ApiResponse`。
- DELETE 接口返回 HTTP 200 和统一响应体。
- 不使用 HTTP 204。
- 全局异常、Jakarta Validation、数据库异常转换在基础阶段完成。

## 重复提交规则

MVP 不引入 Redis、锁、Token 或复杂幂等机制。

只采用：

- 前端提交期间禁用按钮。
- 数据库唯一约束。
- 标准错误处理。

## Git 规则

- 修改前检查 `git status --short`。
- 如果当前目录不是 Git 仓库，只报告事实，不执行 `git init`。
- 修改后运行 `git diff --check`。
- 不提交临时文件、构建产物、运行数据库、日志文件。
- 不 commit，除非用户明确要求。
