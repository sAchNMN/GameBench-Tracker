# GameBench Tracker

GameBench Tracker 是一个本地单用户的游戏性能记录工具。

它用于记录、管理、对比和导出游戏 Benchmark 结果。它不是实时性能监控工具，也不负责采集游戏运行时数据。

## 当前状态

- 已完成 Phase 0 文档。
- 已初始化 Spring Boot 空后端项目。
- 尚未初始化 Vue 项目。
- 尚未写业务代码。
- 尚未创建 SQLite 数据库。
- 尚未执行 SQL。
- 尚未接入数据库。
- 尚未创建 API。

## MVP 目标

MVP 只包含以下能力：

- 游戏管理。
- 测试场景管理。
- 配置模板管理。
- 测试记录管理。
- 同一游戏下双记录对比。
- 基础柱状图。
- CSV 导出。
- 输入校验和统一错误处理。

## 明确不做

以下内容不得进入 MVP：

- 用户注册、用户登录、多用户、复杂权限。
- JWT、Redis、MySQL、PostgreSQL、Docker、微服务。
- 云同步、在线账号、社区、排行榜。
- AI 分析、AI 推荐配置。
- 自动读取硬件、硬件传感器读取、实时采集 FPS。
- 自动运行游戏、显卡超频控制、驱动控制、游戏进程注入。
- 开机自启、后台常驻、定时任务。
- 多语言、主题系统、移动端、PDF 导出。

## 技术栈规划

后端：

- Java 21
- Spring Boot 3
- MyBatis-Plus
- SQLite

前端：

- Vue 3
- TypeScript
- Vite
- Element Plus
- ECharts

构建：

- Maven
- npm
- Git

## 环境要求

- JDK 21
- Maven 3.9+

## 后端命令

运行测试：

```powershell
mvn test
```

启动空后端：

```powershell
mvn spring-boot:run
```

当前后端只是最小 Spring Boot ApplicationContext。它不包含 Controller、Service、Mapper、Entity、DTO、VO，也未接入 SQLite、MyBatis-Plus 或前端。

路径要求：

- 推荐把项目放在纯英文短路径下，例如 `C:\dev\gamebench-tracker`。
- 不建议项目路径包含中文、特殊字符或过长目录。
- 当前原路径包含中文，已确认 `mvn spring-boot:run` 会因 Spring Boot Maven Plugin fork classpath 乱码而找不到主类。
- 英文路径副本中，`mvn clean test` 和 `mvn spring-boot:run` 已验证通过。

Maven 本地仓库要求：

- 默认仓库为 `%USERPROFILE%\.m2\repository`。
- 当前环境下默认 `.m2` ACL 显示当前用户有 FullControl，但普通写入测试仍失败。
- 在权限修复前，可临时使用 `-Dmaven.repo.local=%TEMP%\gamebench-m2`。

## 数据来源

MVP 数据来源为手动录入。

用户可以先使用游戏自带 Benchmark、CapFrameX、PresentMon、MSI Afterburner 等工具完成测试，再把结果填入本软件。

MVP 不做 CSV 导入。CSV 导入规划到 V1.1。

## 运行模型

- 软件按需启动。
- 用户没有操作时不轮询接口。
- 用户没有操作时不扫描文件。
- 用户没有操作时不写数据库。
- 不读取硬件传感器。
- 不实时采集 FPS。
- 关闭软件后 Java 进程必须退出。

MVP 不设计公开的无保护 shutdown HTTP 接口。后续实现时采用显式启动器或启动窗口控制 Java 进程生命周期。

## 时间规范

- 数据库存储 UTC。
- API 使用 ISO 8601。
- 前端按用户本地时区显示。

## 下一步

下一步只建议执行 T007：建立统一响应和错误码基础结构。

不要自动执行。
