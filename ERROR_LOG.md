# ERROR_LOG

本文件只记录真实发生过的项目错误。

不要把尚未发生的风险、推测问题或规划注意事项写入错误日志。

## 2026-07-11 中文路径导致 Spring Boot Maven Plugin fork classpath 乱码

现象：

- 原路径 `G:\桌面\CODE\GameBench Tracker` 下，`mvn test` 可以通过。
- `mvn spring-boot:run` 失败，报 `ClassNotFoundException: com.gamebench.tracker.GameBenchTrackerApplication`。
- 主类通过 `java -cp` 直接启动可以成功。

原因或最强证据：

- `mvn -X spring-boot:run` 显示 Spring Boot Maven Plugin fork 子进程 classpath 中的中文路径被写成乱码。
- 乱码路径指向 `target/classes`，导致子进程找不到已编译主类。
- 纯英文路径副本中，`mvn clean test` 和 `mvn spring-boot:run` 均成功。

解决方法：

- 将正式项目迁移到纯英文短路径 `G:\Code\gamebench-tracker`。
- Maven 验证命令统一使用稳定仓库 `G:\MavenRepo`。

防止再次发生的规则：

- 项目路径不得包含中文、空格、特殊字符或过长目录。
- 标准 Maven 启动验证必须在纯英文短路径下完成。

相关文件：

- `README.md`
- `HANDOFF.md`
- `CHECKLIST.md`

## 2026-07-11 原路径 target 构建产物权限异常

现象：

- 原路径 `G:\桌面\CODE\GameBench Tracker` 下，`mvn clean compile` 在 clean 阶段失败。
- `target` 下 `.class` 文件普通权限无法删除。

原因或最强证据：

- Maven clean 删除 `target` 失败，错误集中在已生成 class 文件。
- 同一项目迁移到 `G:\Code\gamebench-tracker` 后，已验证可以创建和删除普通文件，也可以创建和删除 `target`。
- 该问题与原路径下异常构建产物或目录权限状态相关。

解决方法：

- 新正式路径不复制 `target`。
- 在新路径重新运行 `mvn "-Dmaven.repo.local=G:\MavenRepo" clean test`。
- 在新路径验证 `target` 可正常创建和删除。

防止再次发生的规则：

- 不提交构建产物。
- `target/` 必须保持在 `.gitignore` 中。
- 开发前确认普通用户可以创建、修改、删除项目文件和 `target`。
- Maven 命令不得使用管理员权限掩盖目录权限问题。

相关文件：

- `.gitignore`
- `CHECKLIST.md`