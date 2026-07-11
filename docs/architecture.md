# 架构设计

## 总体结构

```text
Vue 3 + TypeScript
  -> HTTP JSON
  -> Spring Boot 3
  -> MyBatis-Plus
  -> SQLite 本地数据库文件
```

最终用户只需要启动一个 Java 程序。

Vue 构建后的静态资源由 Spring Boot 提供。

## 后端分层

```text
controller
service
service.impl
mapper
entity
dto
vo
converter
exception
validation
config
util
```

职责：

- Controller 不写业务逻辑。
- Service 负责业务规则、快照复制、删除策略、对比计算。
- Mapper 只负责数据库访问。
- Entity 不直接作为全部接口请求和响应对象。
- DTO 用于请求入参。
- VO 用于响应出参。
- Converter 处理对象转换。
- Exception 处理业务异常和错误码。
- Validation 处理自定义参数校验。
- Config 处理 SQLite、时间、静态资源等配置。
- Util 处理 CSV、BigDecimal、时间格式等工具。

## 基础设施提前完成

以下内容在项目基础阶段完成，不推迟到后期：

- `ApiResponse`。
- `ErrorCode`。
- 全局异常处理。
- Jakarta Validation。
- 数据库异常转换。
- 日期时间格式统一。

P12 只做统一性审计和遗漏修复，不首次建立异常体系。

## 前端结构

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

页面规划：

- 首页。
- 游戏列表。
- 游戏详情。
- 测试场景管理。
- 配置模板管理。
- 测试记录列表。
- 新增测试记录。
- 编辑测试记录。
- 双记录对比。
- CSV 导出。

## 前端状态要求

每个页面至少处理：

- 加载状态。
- 空状态。
- 错误状态。
- 删除确认。
- 表单校验。
- 提交中按钮禁用。

## 关键数据流

新增测试记录：

```text
前端选择 game_id 和 scene_id
  -> Controller 接收 DTO
  -> Service 校验场景存在
  -> Service 校验场景属于游戏
  -> Service 复制场景快照
  -> Service 复制配置模板快照
  -> Mapper 保存 benchmark_record
```

双记录对比：

```text
前端选择两条记录
  -> Service 校验两条记录存在
  -> Service 校验同属一个游戏
  -> BigDecimal 计算变化率和 FPS/W
  -> 返回对比 VO
  -> 前端表格和 ECharts 展示
```

## 性能和资源约束

- 不做后台持续轮询。
- 不每秒刷新接口。
- 不后台扫描文件。
- 不自动读取硬件传感器。
- 不实时采集 FPS。
- 不定时写数据库。
- 不写高频日志。
- 用户关闭软件后，Java 进程必须退出。

## 程序退出模型

不设计公开的无保护 shutdown HTTP 接口。

规划采用显式启动器或启动窗口控制 Java 进程生命周期。用户关闭启动器或主窗口时，由启动器负责停止后端进程。
