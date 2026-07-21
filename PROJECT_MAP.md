# PROJECT_MAP

## 当前实际目录

```text
.
├── pom.xml
├── src
│   ├── main
│   │   ├── java/com/gamebench/tracker
│   │   │   ├── GameBenchTrackerApplication.java
│   │   │   ├── common
│   │   │   │   ├── api
│   │   │   │   ├── error
│   │   │   │   ├── exception
│   │   │   │   └── web
│   │   │   └── game
│   │   │       ├── controller
│   │   │       │   ├── GameController.java
│   │   │       │   └── TestSceneController.java
│   │   │       ├── dto
│   │   │       │   ├── GameSaveRequest.java
│   │   │       │   └── TestSceneSaveRequest.java
│   │   │       ├── entity
│   │   │       │   ├── Game.java
│   │   │       │   └── TestScene.java
│   │   │       ├── mapper
│   │   │       │   ├── GameMapper.java
│   │   │       │   └── TestSceneMapper.java
│   │   │       ├── service
│   │   │       │   ├── GameService.java
│   │   │       │   └── TestSceneService.java
│   │   │       └── vo
│   │   ├── resources
│   │   │   ├── application.yml
│   │   │   └── schema.sql
│   │   └── test
│   │       ├── java/com/gamebench/tracker
│   │       │   ├── common
│   │       │   └── game
│   │       │       ├── GameControllerIntegrationTest.java
│   │       │       └── TestSceneControllerIntegrationTest.java
│   │       └── resources/application-test.yml
├── frontend/src
│   ├── App.vue
│   ├── api.ts
│   ├── main.ts
│   └── styles.css
└── docs
```

## 已实现模块

- `common/api`：统一成功和失败响应。
- `common/error`：通用错误码、`GAME_NOT_FOUND`、`SCENE_NOT_FOUND`、`TEMPLATE_NOT_FOUND`、`RECORD_NOT_FOUND`。
- `common/exception`、`common/web`：应用异常、HTTP 映射和全局异常处理。
- `game`：游戏、测试场景、配置模板与性能记录的 Controller、Service、Mapper、Entity、DTO、VO。
- `schema.sql`：`game`、`test_scene` 与 `config_template` 表、索引和关联约束。
- `frontend`：游戏搜索、新增、编辑、删除和按游戏管理测试场景、配置模板、性能记录。

## 当前数据流

```text
游戏页面
  -> /api/games
  -> GameController -> GameService -> GameMapper
  -> SQLite game

场景页面
  -> /api/games/{gameId}/scenes
  -> TestSceneController -> TestSceneService -> TestSceneMapper
  -> SQLite test_scene

模板页面
  -> /api/games/{gameId}/config-templates
  -> ConfigTemplateController -> ConfigTemplateService -> ConfigTemplateMapper
  -> SQLite config_template

记录页面
  -> /api/games/{gameId}/records
  -> BenchmarkRecordController -> BenchmarkRecordService -> BenchmarkRecordMapper
  -> SQLite benchmark_record
```

性能记录前端页面已实现。对比、图表和 CSV 仍未实现。

## 配置模板后端文件

- game/controller/ConfigTemplateController.java：模板 CRUD HTTP 入口。
- game/service/ConfigTemplateService.java：游戏归属、模板存在性和字段规范化。
- game/mapper/ConfigTemplateMapper.java、entity/ConfigTemplate.java：SQLite 表映射。
- game/dto/ConfigTemplateSaveRequest.java、vo/ConfigTemplateResponse.java：API 输入输出。
- ConfigTemplateControllerIntegrationTest.java：模板持久化、校验、冲突、CRUD 和级联删除验证。
