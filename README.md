# GameBench Tracker

![Last Commit](https://img.shields.io/github/last-commit/sAchNMN/GameBench-Tracker)
![Repo Size](https://img.shields.io/github/repo-size/sAchNMN/GameBench-Tracker)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)
![Phase](https://img.shields.io/badge/Phase-C%20Backend-informational)

GameBench Tracker 是本地单用户的游戏性能记录工具。用户手动录入和管理 Benchmark 数据；它不采集实时硬件数据。

## 当前状态

阶段 A、B 已完成；阶段 C 的配置模板后端已完成：

- SQLite 和 MyBatis-Plus 已接入。
- `game` 表已初始化。
- 游戏新增、列表、详情、编辑和删除 API 已完成。
- 游戏管理 Vue 页面已完成。
- `test_scene` 表、场景 CRUD API 和按游戏场景管理页面已完成。
- `config_template` 表、配置模板 CRUD API、游戏归属校验和功耗限制校验已完成。
- 游戏名称与平台组合唯一。
- 模板名称在同一游戏内唯一；删除游戏会级联删除其模板。
- 已验证保存 `CS2 / Steam` 后，重启后端仍可读取。

配置模板前端页面、性能记录、对比、图表和 CSV 导出尚未完成。

## 技术栈

后端：Java 21、Spring Boot 3.3.5、Spring Web、Jakarta Validation、MyBatis-Plus、SQLite、Maven。

前端：Vue 3、TypeScript、Vite、Element Plus。ECharts 留待图表阶段接入。

## 运行后端

```powershell
mvn "-Dmaven.repo.local=G:\Code\gamebench-tracker\MavenRepo" clean test
mvn "-Dmaven.repo.local=G:\Code\gamebench-tracker\MavenRepo" spring-boot:run
```

默认数据库文件是项目根目录的 `gamebench-tracker.db`。它保存本机数据，已被 Git 忽略。

默认 `%USERPROFILE%\.m2` 在当前开发机仍有写入问题，因此验证使用项目内 `MavenRepo`；该目录同样被 Git 忽略。

## 运行前端

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Vite 开发服务器把 `/api` 代理到 `http://localhost:8080`。生产构建使用：

```powershell
npm.cmd run build
```

## 当前 API

- `POST /api/games`
- `GET /api/games?keyword=&page=1&size=20`
- `GET /api/games/{id}`
- `PUT /api/games/{id}`
- `DELETE /api/games/{id}`
- `POST /api/games/{gameId}/scenes`
- `GET /api/games/{gameId}/scenes`
- `GET /api/scenes/{id}`
- `PUT /api/scenes/{id}`
- `DELETE /api/scenes/{id}`
- `POST /api/games/{gameId}/config-templates`
- `GET /api/games/{gameId}/config-templates`
- `GET /api/config-templates/{id}`
- `PUT /api/config-templates/{id}`
- `DELETE /api/config-templates/{id}`

所有接口返回统一 `ApiResponse`。删除成功返回 HTTP 200，不使用 HTTP 204。

## 明确不做

MVP 不包含账号、多用户、Redis、MySQL、Docker、云同步、AI 分析、实时 FPS 采集、硬件传感器读取、后台轮询或游戏进程注入。
