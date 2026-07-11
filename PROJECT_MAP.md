# PROJECT_MAP

## 当前实际目录

当前只允许存在文档和工具目录。

已规划但尚未创建：

- `src/`
- `frontend/`
- `pom.xml`
- `package.json`
- `vite.config.ts`

这些内容属于后续任务，不在 Phase 0 执行。

## 规划后的后端结构

```text
src/main/java/com/gamebench/tracker
├── controller
├── service
├── service/impl
├── mapper
├── entity
├── dto
├── vo
├── converter
├── exception
├── validation
├── config
└── util
```

职责：

- `controller`：接收请求，返回统一响应，不写业务规则。
- `service`：处理业务规则、快照复制、删除策略、对比计算。
- `mapper`：只负责数据库访问。
- `entity`：映射数据库表，不直接作为全部接口请求和响应对象。
- `dto`：接口请求对象。
- `vo`：接口响应对象。
- `converter`：DTO、Entity、VO 转换。
- `exception`：业务异常和错误码。
- `validation`：自定义校验。
- `config`：SQLite、时间格式、静态资源等配置。
- `util`：CSV、BigDecimal、时间工具。

## 规划后的前端结构

```text
frontend/src
├── api
├── views
├── components
├── router
├── stores
├── types
├── utils
└── composables
```

职责：

- `api`：HTTP 请求封装。
- `views`：页面。
- `components`：公共组件。
- `router`：路由。
- `stores`：必要的页面状态。
- `types`：接口类型。
- `utils`：数值、时间、CSV 文件名等工具。
- `composables`：复用页面逻辑。

## 数据流

```text
用户表单
  -> 前端校验
  -> DTO
  -> Controller
  -> Service
  -> Mapper
  -> SQLite
```

对比数据流：

```text
选择同一游戏下两条记录
  -> Service 校验记录和游戏归属
  -> BigDecimal 计算
  -> VO 返回
  -> 前端表格和图表展示
```

新增测试记录数据流：

```text
选择 game_id 和 scene_id
  -> 校验场景存在
  -> 校验场景属于游戏
  -> 复制场景快照
  -> 复制配置模板快照
  -> 保存 benchmark_record
```
