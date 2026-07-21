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

---

## 质量门禁（2026-07-20 追加）

在稳定基线之上接入自动化质量门禁，`mvn verify` 强制生效，不改动业务逻辑：

- **Checkstyle 9.3**（`config/checkstyle/checkstyle.xml`）：文件末尾换行、禁 Tab、导入规范（UnusedImports/RedundantImport/AvoidStarImport）、行宽 ≤200。0 违规。
  - 注意：`NewlineAtEndOfFile`/`FileTabCharacter`/`LineLength` 属 Checker 级；导入类检查属 TreeWalker 级，放错层级会报 "not allowed as a child/parent"。
- **SpotBugs 4.8.6.4**（High 阈值）：0 bug。
- **JaCoCo 0.8.12**：行覆盖门槛 60%，实际 95.3%。
  - destfile 指向 `${java.io.tmpdir}/gamebench-jacoco.exec`，规避项目路径含空格（"GameBench Tracker"）导致 agent 写不出 exec 的假绿问题。
  - surefire `argLine` 使用 `@{argLine}` 承接 JaCoCo agent。
- **ArchUnit**（`architecture/ModuleBoundaryTest.java`）：声明式模块边界护栏——Controller 不直连 Mapper、common 不反向依赖 game、game 内不向上依赖 controller。

测试合计 **74 通过 / 0 失败 / 0 错误**（含新增 benchmark_record 模块 9 个集成测试）。

### 门禁构建命令（项目路径含中文/空格，用临时本地仓库）
```
"C:/Program Files/Java/jdk-21.0.11/bin/java" -cp "C:/tools/maven-fixed/apache-maven-3.9.16/boot/plexus-classworlds-2.11.0.jar" -Dmaven.home=C:/tools/maven-fixed/apache-maven-3.9.16 -Dclassworlds.conf=C:/tools/maven-fixed/apache-maven-3.9.16/bin/m2.conf "-Dmaven.multiModuleProjectDirectory=G:/桌面/CODE/GameBench Tracker" -Dmaven.repo.local=C:/Users/34759/AppData/Local/Temp/gamebench-m2 org.codehaus.plexus.classworlds.launcher.Launcher -B verify

---

## 性能记录模块 benchmark_record（2026-07-20 追加）

GameBench Tracker 的核心数据模块——记录游戏实测性能（FPS / 温度 / 功耗 / CPU 占用）。复用现有统一响应、jakarta 校验、统一异常与 ArchUnit 护栏范式。

- `schema.sql`：`benchmark_record` 表。`game_id` 必填 FK→`game` ON DELETE CASCADE；`scene_id`/`template_id` 可选 FK（→`test_scene`/`config_template`）ON DELETE SET NULL；数值 CHECK 约束（`avg_fps`/`min_fps`>0、`gpu_power_watt`≥0、`cpu_usage_percent` 0–100、温度/帧时间≥0）+ 索引。
- 文件：`BenchmarkRecord`(Entity) / `BenchmarkRecordMapper` / `BenchmarkRecordService`(CRUD + requireGame/requireRecord + 可选 scene/template 存在性校验) / `BenchmarkRecordController`(`POST /api/games/{gameId}/records`、`GET /api/games/{gameId}/records`、`GET|PUT|DELETE /api/records/{id}`) / `BenchmarkRecordSaveRequest`(DTO，jakarta 校验) / `BenchmarkRecordResponse`(VO)。
- `ErrorCode.RECORD_NOT_FOUND` 已登记并映射到 HTTP 404。
- `BenchmarkRecordControllerIntegrationTest`：9 个用例覆盖创建持久化、未知游戏、记录不存在、跨游戏隔离、字段校验边界、可选关联存在性、更新删除。
- `mvn verify` 全绿：74 测试通过，Checkstyle/SpotBugs/JaCoCo 三道门禁通过。

---

## 配置模板前端（2026-07-20 追加）

阶段 C 前端已完成，补齐 `config_template` 后端与前端闭环。

- `frontend/src/App.vue`：新增 `templates` 视图，从游戏列表「模板」入口进入。
- 实现模板列表、新增、编辑、删除确认，以及空/加载/错误状态。
- 表单字段覆盖名称、分辨率、图形预设、超分技术与质量、垂直同步、帧生成、GPU 核心/显存频率、电压、功耗限制、驱动版本与说明。
- 前端校验：`gpu_power_limit_percent` 限定 -100~100（`el-input-number` 范围 + `saveTemplate` 二次拦截）。
- 复刻游戏/场景页的统一 `request` 调用、`ElMessage`/`ElMessageBox` 范式与 `formatEmpty` 空值展示。
- 前端 `vue-tsc -b && vite build` 构建通过。
- 文档同步：api.md 配置模板接口页面标记已实现、PROJECT_MAP 数据流与已实现模块、development-roadmap 阶段 C 前端完成、HANDOFF 本段。
- 尚未建立独立 git 提交（用户未要求 commit）。

## 性能记录前端（2026-07-21 追加）

阶段 D 前端已完成，补齐 `benchmark_record` 后端与前端闭环。

- `frontend/src/App.vue`：新增 `records` 视图，从游戏列表「记录」入口进入；进入时并行拉取记录列表、该游戏场景与模板（用于下拉与名称映射）。
- 实现记录列表、新增、编辑、删除确认，以及空/加载/错误状态。
- 表单字段：`sceneId`（必填下拉）、`templateId`（可选下拉）、`recordedAt`（日期时间）、`avgFps`/`minFps`/`frameTimeMs`（必填 >0）、`gpuTempCelsius`/`cpuTempCelsius`（≥ -273.15）、`gpuPowerWatt`（≥0）、`cpuUsagePercent`（0–100）、`notes`。
- 前端校验：必填三项 >0；温度/功耗/CPU 占用范围拦截；`sceneId` 必选。复用统一 `request` 与 `ElMessage`/`ElMessageBox` 范式。
- 编辑历史记录时显示 `el-alert` 警告「正在修改历史测试数据」，保存后 `updatedAt` 由后端刷新。
- 记录表展示测试时间、场景名、模板名、平均/最低 FPS、GPU 温度/功耗、CPU 占用、备注（场景/模板名按 id 实时映射，后端不存快照）。
- 前端 `vue-tsc -b && vite build` 构建通过（类型检查 0 错误）。
- 文档同步：api.md 记录接口按实际契约重写（URL 与字段）、PROJECT_MAP 模块/数据流/已实现清单、development-roadmap 阶段 D 完成并设阶段 E 为下一阶段、HANDOFF 本段。
- 尚未建立独立 git 提交（用户未要求 commit）。

## 双记录对比（2026-07-21 追加）

阶段 E 已完成，补齐双记录对比后端与前端闭环。

- `RecordCompareRequest`（DTO）：`baseRecordId`/`targetRecordId`，均 `@NotNull @Min(1)`。
- `RecordCompareResponse`（VO）：`base`/`target` 完整快照 + `avgFpsChangeRate`/`minFpsChangeRate`/`frameTimeMsChangeRate`/`gpuPowerChangeRate`/`gpuPowerDropRate`/`gpuTempDiff`/`cpuTempDiff`/`cpuUsageDiff`/`baseFpsPerWatt`/`targetFpsPerWatt`/`fpsPerWattChangeRate`。
- `BenchmarkRecordService.compare`：校验非同一条、同属一游戏（否则 `CONFLICT` 409）、记录存在（否则 `RECORD_NOT_FOUND` 404）；变化率=(target-base)/base*100、差异=target-base、FPS/W=avgFps/gpuPowerWatt，均保留 2 位；base 值空/0 或功耗空/0 时对应项返回 null。
- `BenchmarkRecordController` 新增 `POST /api/benchmark-records/compare`。
- `RecordCompareControllerIntegrationTest`：5 用例覆盖正常对比、base 功耗缺失返回 null 指标、未知记录 404、跨游戏 409、同 id 409。
- `mvn verify` 全绿：79 测试通过，Checkstyle/SpotBugs/JaCoCo/ArchUnit 门禁通过。
- 前端 `App.vue` 新增 `compare` 视图：游戏列表「对比」入口 -> 双下拉选记录 -> 结果表按性能向好着色（`delta-good`/`delta-bad`）；`styles.css` 补对比样式；`vue-tsc -b && vite build` 构建通过。
- 文档同步：api.md 对比接口按实际契约重写（修正 1% Low/0.1% Low/热点温度等文档漂移为实际 minFps/frameTimeMs/cpuTemp）、development-roadmap 阶段 E 完成并设阶段 F 为下一阶段、PROJECT_MAP 模块/数据流/已实现清单、HANDOFF 本段。

## 图表与 CSV 导出（2026-07-21 追加）

阶段 F 已完成，补齐记录趋势可视化与数据导出闭环。

- `BenchmarkRecordService.exportRecordsCsv`：校验游戏存在（否则 `GAME_NOT_FOUND` 404），按 `recordedAt` 升序查询，组装 UTF-8 BOM + CRLF 的 12 列 CSV；字段含逗号/引号/换行时按 RFC 4180 用双引号包裹、内部引号转义为两个双引号；无记录时仅表头。
- `BenchmarkRecordController` 新增 `GET /api/games/{gameId}/records/export`，`produces=text/csv;charset=utf-8`，响应头 `Content-Disposition: attachment; filename="benchmark-records-game-{gameId}.csv"`，响应体为 `\uFEFF` + CSV 文本。
- `RecordExportControllerIntegrationTest`：4 用例覆盖正常导出（表头+数据行）、未知游戏 404、无记录仅表头、特殊字符 RFC4180 转义。
- `mvn verify` 全绿：83 测试通过，Checkstyle/SpotBugs/JaCoCo/ArchUnit 门禁通过。
- 前端 `App.vue` 新增 `echarts` 依赖与趋势图表对话框（记录视图「趋势图表」入口 -> ECharts 折线，按测试时间升序，可切换平均/最低 FPS、GPU 功耗/温度；`opened` 渲染、`closed` 销毁、窗口 resize 重绘）；新增「导出 CSV」按钮（fetch + Blob 触发浏览器下载，失败用 `ElMessage` 提示）；`styles.css` 补图表容器与空态样式；`vue-tsc -b && vite build` 构建通过（仅 chunk 体积告警）。
- 文档同步：api.md CSV 导出接口按实际契约重写（修正旧规划 `/api/export/...` 与不存在的"双记录对比导出"）、development-roadmap 阶段 F 完成并设阶段 G 为下一阶段、PROJECT_MAP 模块/数据流/已实现清单、HANDOFF 本段。

## 下一步

阶段 G：前后端联调、全量回归、运行说明和交付审查。

## 阶段 G：联调 / 回归 / 交付（2026-07-21 追加）

阶段 G 收尾已完成。

- 端到端联调：启动前端 Vite dev（5173，proxy `/api`→`localhost:8080`）与后端 jar（8080），经代理跑通完整链路——建游戏→场景→模板→记录→列表→双记录对比→CSV 导出→删除清理，全部 200。
- 联调发现真实坑：本机环境变量 `SERVER__PORT=14835` 被 Spring Boot 宽松绑定为 `server.port`，导致后端默认绑 14835 而非 8080，与前端 proxy（8080）不匹配；另有旧后端进程占用 14835（PID 16804）与 8080（PID 4584）。用最新 jar + 独立临时 db 在 8090 复跑同一链路全部通过，证明**最新代码无 bug**，8080/14835 上是旧后端实例，需重启到最新 jar（`mvn spring-boot:run` 或 `java -jar target/*.jar`）。
- 全量回归：`mvn verify` 复跑 **83 测试通过**，Checkstyle/SpotBugs/JaCoCo/ArchUnit 四道门禁全绿；前端 `npm run build`（`vue-tsc -b && vite build`）0 类型错误通过（仅 chunk 体积告警）。
- README 重写到当前状态：技术栈含 ECharts；功能一览与 API 概览补全记录/对比/CSV 导出；运行状态更新到阶段 F 完成；修正旧 Maven 仓库路径；新增 `SERVER__PORT` 端口坑说明与显式 `--server.port=8080` 对齐方案；补充质量门禁段落。
- 交付审查：api.md / roadmap / PROJECT_MAP / HANDOFF / README 现已一致，已修正历史文档漂移（旧 CSV 规划、对比文档漂移、过时状态）。
- 交付状态：MVP 功能与质量门禁全部完成，可交付。

## 交付状态

MVP 全功能完成（游戏/场景/模板/记录/对比/图表/CSV），四道质量门禁通过，前后端联调验证通过。后续若需可加：登录鉴权、图表按场景分组、移动端适配（均超出 MVP 范围）。
