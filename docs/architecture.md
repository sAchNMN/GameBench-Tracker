# 架构设计

## 当前结构

```text
Vue 3 + TypeScript + Element Plus
  -> HTTP JSON
  -> Spring Boot 3
  -> Controller -> Service -> Mapper
  -> MyBatis-Plus
  -> SQLite 本地文件
```

阶段 A、B 已实现游戏与测试场景管理链路；阶段 C 已实现配置模板后端链路。前端开发服务器将 `/api` 代理到 Spring Boot。

## 分层职责

- `GameController`：绑定 HTTP 参数、返回 HTTP 状态和 `ApiResponse`。
- `GameService`：规范化输入、检查存在性、创建、分页查询、更新和删除。
- `GameMapper`：通过 MyBatis-Plus 访问 `game`。
- `Game`：映射数据库行；`GameSaveRequest` 与 `GameResponse` 分离 HTTP 输入输出。
- `GlobalExceptionHandler`：统一处理校验、应用异常和 SQLite 唯一约束错误，避免泄漏内部信息。
- `TestSceneController`、`TestSceneService`、`TestSceneMapper`：场景必须隶属现有游戏；场景名在同一游戏内唯一。
- `ConfigTemplateController`、`ConfigTemplateService`、`ConfigTemplateMapper`：模板必须隶属现有游戏；模板名在同一游戏内唯一；功耗限制为 -100 到 100。

## 已实现数据流

```text
游戏表单
  -> POST /api/games
  -> GameController
  -> GameService
  -> GameMapper
  -> SQLite
  -> ApiResponse<GameResponse>
```

游戏、场景和配置模板的列表、详情、编辑和删除均走同一分层。删除游戏时，SQLite 外键级联删除场景和模板；未来记录表删除场景或模板时会使用 `SET NULL` 保留快照。

## 时间与数据

SQLite 默认时间使用 UTC；更新时由应用写入 UTC `Instant`。API 序列化为 ISO 8601。

生产数据库为项目根目录 `gamebench-tracker.db`。测试配置使用独立的共享内存 SQLite 数据库。

## 未实现

配置模板前端页面、性能记录、对比、图表、CSV、静态前端资源托管和应用启动器仍未实现。

不设计公共 shutdown HTTP 接口。
