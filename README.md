# GameBench Tracker

![Last Commit](https://img.shields.io/github/last-commit/sAchNMN/GameBench-Tracker)
![Repo Size](https://img.shields.io/github/repo-size/sAchNMN/GameBench-Tracker)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)
![Phase](https://img.shields.io/badge/Phase-F%20Complete-success)

GameBench Tracker 是本地单用户的游戏性能记录工具。用户手动录入和管理 Benchmark 数据；它不采集实时硬件数据。

## 当前状态

全部功能已完成并验证：

- SQLite 和 MyBatis-Plus 已接入，外键级联已启用。
- 游戏、测试场景、配置模板的增删改查与按游戏归属校验。
- 性能记录（FPS / 温度 / 功耗 / CPU 占用）的增删改查与历史编辑。
- 双记录对比：FPS / 帧时间变化率、GPU 功耗变化率与下降率、温度 / 占用差异、FPS/W。
- 记录趋势图表（ECharts 折线，按测试时间升序，可切换指标）。
- CSV 导出（UTF-8 BOM、RFC 4180 转义）。
- 前后端联调、全量回归与质量门禁（Checkstyle / SpotBugs / JaCoCo / ArchUnit）均通过。

## 技术栈

后端：Java 21、Spring Boot 3.3.5、Spring Web、Jakarta Validation、MyBatis-Plus、SQLite、Maven。

前端：Vue 3、TypeScript、Vite、Element Plus、ECharts。

## 功能一览

- **游戏**：搜索、新增、编辑、删除；名称 + 平台组合唯一；删除级联删除其场景、模板与记录。
- **测试场景**：归属游戏；名称、方法、时长；删除级联置空历史记录的场景引用。
- **配置模板**：名称、分辨率、图形预设、超分、VSync、帧生成、GPU 频率 / 电压、功耗限制、驱动等；功耗限制范围 -100~100；删除不影响历史记录。
- **性能记录**：场景（必填）、模板（可选）、测试时间、平均 / 最低 FPS、帧时间、GPU/CPU 温度、GPU 功耗、CPU 占用、备注；必填 FPS / 帧时间 > 0，温度 / 功耗 / CPU 占用范围校验；编辑历史记录有提示。
- **双记录对比**：同游戏校验、非同条校验；变化率与差异计算，空值 / 零值返回 null。
- **趋势图表**：ECharts 折线，平均 / 最低 FPS、GPU 功耗 / 温度按测试时间升序切换。
- **CSV 导出**：按游戏导出全部记录，UTF-8 BOM + CRLF + RFC 4180 转义。

## 运行后端

需要 Java 21 与 Maven。

```powershell
mvn spring-boot:run
```

默认数据库文件是项目根目录的 `gamebench-tracker.db`，保存本机数据，已被 Git 忽略。删除该文件即清空所有本地数据。

> 端口说明：后端默认 `8080`，前端 Vite 代理也指向 `8080`。若本机设置了 `SERVER__PORT` 等环境变量，Spring Boot 会将其宽松绑定为 `server.port`，导致后端绑定到该变量指定的端口，与前端代理不一致。此时显式指定端口即可对齐：
>
> ```powershell
> mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
> # 或
> java -jar target/gamebench-tracker-0.0.1-SNAPSHOT.jar --server.port=8080
> ```

构建可执行包：

```powershell
mvn clean package
java -jar target/gamebench-tracker-0.0.1-SNAPSHOT.jar
```

## 运行前端

需要 Node.js 20+。

```powershell
cd frontend
npm install
npm run dev
```

Vite 开发服务器默认 `http://localhost:5173`，把 `/api` 代理到 `http://localhost:8080`。生产构建：

```powershell
npm run build
```

产物在 `frontend/dist`，可部署为静态站点或对接后端。

## API 概览

所有接口返回统一 `ApiResponse`（`{ success, data, error, timestamp }`）。删除成功返回 HTTP 200，不使用 HTTP 204。

游戏

- `POST /api/games`
- `GET /api/games?keyword=&page=1&size=20`
- `GET /api/games/{id}`
- `PUT /api/games/{id}`
- `DELETE /api/games/{id}`

测试场景

- `POST /api/games/{gameId}/scenes`
- `GET /api/games/{gameId}/scenes`
- `GET /api/scenes/{id}`
- `PUT /api/scenes/{id}`
- `DELETE /api/scenes/{id}`

配置模板

- `POST /api/games/{gameId}/config-templates`
- `GET /api/games/{gameId}/config-templates`
- `GET /api/config-templates/{id}`
- `PUT /api/config-templates/{id}`
- `DELETE /api/config-templates/{id}`

性能记录

- `POST /api/games/{gameId}/records`
- `GET /api/games/{gameId}/records`
- `GET /api/records/{id}`
- `PUT /api/records/{id}`
- `DELETE /api/records/{id}`
- `GET /api/games/{gameId}/records/export`（CSV 下载，UTF-8 BOM）

双记录对比

- `POST /api/benchmark-records/compare`（`{ baseRecordId, targetRecordId }`）

详细字段、校验与错误码见 `docs/api.md`。

## 质量门禁

`mvn verify` 强制运行 Checkstyle、SpotBugs、JaCoCo（行覆盖门槛 60%）与 ArchUnit 模块边界检查。提交前请确保 `mvn verify` 全绿。

## 明确不做

MVP 不包含账号、多用户、Redis、MySQL、Docker、云同步、AI 分析、实时 FPS 采集、硬件传感器读取、后台轮询或游戏进程注入。
