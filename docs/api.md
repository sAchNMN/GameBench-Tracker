# API 契约



## 统一响应

统一响应基础结构已经创建。游戏管理 API 已实现；其余接口仍是规划。

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-07-11T12:00:00Z"
}
```

失败响应结构：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "资源不存在",
    "details": {}
  },
  "timestamp": "2026-07-11T12:00:00Z"
}
```



## 响应约束

- `success`、`data`、`error`、`timestamp` 分别对应 `ApiResponse` 的字段。
- `success=true` 时 `error` 必须为 `null`。
- `success=false` 时 `error` 必须非 `null`，`data` 必须为 `null`。
- `timestamp` 使用 UTC `Instant`，序列化后为 ISO 8601 UTC 时间。
- `ApiError.details` 为不可修改的防御性副本；空 details 统一为 `{}`。
- 当前不包含 HTTP 状态码字段。ErrorCodeHttpStatusMapper 与 GlobalExceptionHandler 已实现基础异常转换。



## 当前错误码

- `INVALID_REQUEST`：请求无效。
- `VALIDATION_FAILED`：参数校验失败。
- `RESOURCE_NOT_FOUND`：资源不存在。
- `GAME_NOT_FOUND`：游戏不存在。
- `SCENE_NOT_FOUND`：测试场景不存在。
- `TEMPLATE_NOT_FOUND`：配置模板不存在。
- `CONFLICT`：数据冲突。
- `INTERNAL_ERROR`：服务器内部错误。

当前已有游戏、场景和模板错误码；性能记录等业务模块错误码在对应模块开发时增加。错误码用于程序判断，不能依赖中文消息判断。



## HTTP 状态码

ErrorCodeHttpStatusMapper 已实现以下通用错误码映射：

- `INVALID_REQUEST`、`VALIDATION_FAILED` → 400 Bad Request。
- `RESOURCE_NOT_FOUND`、`GAME_NOT_FOUND`、`SCENE_NOT_FOUND`、`TEMPLATE_NOT_FOUND` → 404 Not Found。
- `CONFLICT` → 409 Conflict。
- `INTERNAL_ERROR` → 500 Internal Server Error。

GlobalExceptionHandler 已通过 MockMvc 验证以下处理：

- ApplicationException：使用异常携带的 ErrorCode、details 和映射状态码。
- MethodArgumentNotValidException：返回 `VALIDATION_FAILED` 与 400。
- ConstraintViolationException：返回 VALIDATION_FAILED 与 400。
- HandlerMethodValidationException：返回 VALIDATION_FAILED 与 400，使用参数注解名称或稳定回退值作为 path。
- MissingServletRequestParameterException：返回 INVALID_REQUEST 与 400，details 为 {}。
- MethodArgumentTypeMismatchException：返回 INVALID_REQUEST 与 400，details 为 {}。
- HttpMessageNotReadableException：返回 `INVALID_REQUEST` 与 400，details 为 `{}`。
- 未预期 Exception：返回 `INTERNAL_ERROR` 与 500，details 为 `{}`。

校验 details 固定结构：

```json
{
  "fieldErrors": [{"field": "字段名", "message": "校验消息"}],
  "violations": [{"path": "参数路径", "message": "校验消息"}]
}
```

字段错误按 field 后 message 排序，路径违例按 path 后 message 排序。响应不会返回 invalidValue、rejectedValue、cause、异常堆栈或底层解析器消息。MockMvc 已验证 HandlerMethodValidationException、缺参和类型错误的基础请求链。游戏 API 已通过 SQLite 集成测试验证。



## 已实现：游戏接口



### 游戏列表

- 方法：GET
- URL：`/api/games`
- Query：`keyword`、`page`、`size`
- 成功：200
- 页面：游戏列表



### 新增游戏

- 方法：POST
- URL：`/api/games`
- 请求体：`name` 必填，`platform` 可为空但后端保存为 `''`，`remark` 可选
- 成功：201
- 失败：400、409
- 页面：游戏列表、游戏详情



### 游戏详情

- 方法：GET
- URL：`/api/games/{id}`
- Path：`id`
- 成功：200
- 失败：404
- 页面：游戏详情



### 编辑游戏

- 方法：PUT
- URL：`/api/games/{id}`
- Path：`id`
- 请求体：`name` 必填，`platform` 可为空但后端保存为 `''`，`remark` 可选
- 成功：200
- 失败：400、404、409
- 页面：游戏详情



### 删除游戏

- 方法：DELETE
- URL：`/api/games/{id}`
- Path：`id`
- 成功：200
- 失败：404
- 页面：游戏列表、游戏详情

当前删除游戏会通过 SQLite 外键级联删除其场景；后续模板和记录按各自外键策略处理。前端已提供二次确认。



## 已实现：测试场景接口



### 场景列表

- 方法：GET
- URL：`/api/games/{gameId}/scenes`
- Path：`gameId`
- 成功：200
- 失败：404
- 页面：测试场景管理



### 新增场景

- 方法：POST
- URL：`/api/games/{gameId}/scenes`
- 请求体：`name` 必填，`method` 必填，`durationSeconds` 可选且大于 0
- 成功：201
- 失败：400、404、409
- 页面：测试场景管理



### 编辑场景

- 方法：PUT
- URL：`/api/scenes/{id}`
- 请求体：`name` 必填，`method` 必填，`durationSeconds` 可选且大于 0
- 成功：200
- 失败：400、404、409
- 页面：测试场景管理



### 场景详情

- 方法：GET
- URL：`/api/scenes/{id}`
- 成功：200
- 失败：404

### 删除场景

- 方法：DELETE
- URL：`/api/scenes/{id}`
- 成功：200
- 失败：404
- 页面：测试场景管理

删除场景后，未来历史记录的 `scene_id` 允许变为空，场景快照必须保留。

## 已实现：配置模板接口

### 模板列表

- 方法：GET
- URL：`/api/games/{gameId}/config-templates`
- Path：`gameId`
- 成功：200
- 失败：404
- 页面：配置模板管理

### 新增模板

- 方法：POST
- URL：`/api/games/{gameId}/config-templates`
- Path：`gameId`
- 请求体：名称必填；其余模板字段可选
- 校验：`gpu_power_limit_percent` 允许 -100 到 100；频率和电压只能为空或非负数
- 成功：201
- 失败：400、404、409
- 页面：配置模板管理

### 模板详情

- 方法：GET
- URL：`/api/config-templates/{id}`
- 成功：200
- 失败：404

### 编辑模板

- 方法：PUT
- URL：`/api/config-templates/{id}`
- 请求体：同新增模板
- 校验：`gpu_power_limit_percent` 允许 -100 到 100
- 成功：200
- 失败：400、404、409
- 页面：配置模板管理

### 删除模板

- 方法：DELETE
- URL：`/api/config-templates/{id}`
- 成功：200
- 失败：404
- 页面：配置模板管理

模板字段包括名称、分辨率、图形预设、缩放、VSync、帧生成、GPU 核心/显存频率、电压、功耗限制、驱动和说明。模板名称在同一游戏内唯一。删除游戏会级联删除模板；删除模板不改变未来历史记录，`config_template_id` 只用于记录来源。

## 已实现：性能记录接口

性能记录接口已实现（后端 `BenchmarkRecordController` + 前端测试记录页面）。双记录对比与 CSV 导出仍待实现（见下）。

## 测试记录接口

### 记录列表

- 方法：GET
- URL：`/api/games/{gameId}/records`
- Path：`gameId`
- 成功：200，返回该游戏下记录数组（按 id 倒序）
- 失败：404
- 页面：测试记录管理

### 新增记录

- 方法：POST
- URL：`/api/games/{gameId}/records`
- Path：`gameId`
- 请求体（BenchmarkRecordSaveRequest）：
  - `sceneId`：可选（前端必填），引用 `test_scene`。
  - `templateId`：可选，引用 `config_template`，仅记录来源。
  - `recordedAt`：可选，测试时间字符串。
  - `avgFps`：必填，大于 0。
  - `minFps`：必填，大于 0。
  - `frameTimeMs`：必填，大于 0。
  - `gpuTempCelsius`：可选，≥ -273.15。
  - `cpuTempCelsius`：可选，≥ -273.15。
  - `gpuPowerWatt`：可选，≥ 0。
  - `cpuUsagePercent`：可选，0–100。
  - `notes`：可选。
- 成功：201
- 失败：400、404（GAME_NOT_FOUND / SCENE_NOT_FOUND / TEMPLATE_NOT_FOUND）
- 页面：新增测试记录

说明：后端仅保存 `sceneId`/`templateId` 引用，不复制场景/模板字段快照；前端按 id 实时拉取名称展示。

### 记录详情

- 方法：GET
- URL：`/api/records/{id}`
- 成功：200
- 失败：404
- 页面：编辑测试记录

### 编辑记录

- 方法：PUT
- URL：`/api/records/{id}`
- 请求体：同新增记录
- 成功：200
- 失败：400、404
- 页面：编辑测试记录

编辑历史记录用于修正录入错误。编辑后更新 `updatedAt`，不改变所属游戏。前端必须提示正在修改历史测试数据。

### 删除记录

- 方法：DELETE
- URL：`/api/records/{id}`
- 成功：200
- 失败：404
- 页面：测试记录管理



## 已实现：双记录对比接口



### 对比

- 方法：POST
- URL：`/api/benchmark-records/compare`
- 请求体（RecordCompareRequest）：`baseRecordId`、`targetRecordId`，均必填且为正数。
- 成功：200，返回 `RecordCompareResponse`。
- 失败：400（参数校验）、404（`RECORD_NOT_FOUND`）、409（`CONFLICT`）。
- 页面：双记录对比

校验：

- 两条记录必须存在，否则 `RECORD_NOT_FOUND`。
- 两条记录必须属于同一游戏，否则 `CONFLICT`。
- 两条记录不能为同一条，否则 `CONFLICT`。
- base 值为空或 0 时，对应变化率返回 null。
- 功耗为空或 0 时，FPS/W 返回 null。

返回字段（RecordCompareResponse）：

- `base`、`target`：两条记录的完整快照（`BenchmarkRecordResponse`）。
- `avgFpsChangeRate`：平均 FPS 变化率（%）。
- `minFpsChangeRate`：最低帧率变化率（%）。
- `frameTimeMsChangeRate`：帧时间变化率（%）。
- `gpuPowerChangeRate`：GPU 功耗变化率（%）。
- `gpuPowerDropRate`：GPU 功耗下降率（%，等于功耗变化率取负）。
- `gpuTempDiff`：GPU 温度差异（℃）。
- `cpuTempDiff`：CPU 温度差异（℃）。
- `cpuUsageDiff`：CPU 占用差异（%）。
- `baseFpsPerWatt`、`targetFpsPerWatt`：基线/目标 FPS/W。
- `fpsPerWattChangeRate`：FPS/W 变化率（%）。

变化率 = (target - base) / base * 100，保留 2 位小数；差异 = target - base，保留 2 位小数。



## CSV 导出接口



### 测试记录导出

- 方法：GET
- URL：`/api/export/benchmark-records.csv`
- Query：`gameId`、`sceneId`
- 成功：200
- 页面：CSV 导出



### 双记录对比导出

- 方法：GET
- URL：`/api/export/compare.csv`
- Query：`baseRecordId`、`targetRecordId`
- 成功：200
- 页面：双记录对比

CSV 要求：

- UTF-8 with BOM。
- CRLF 换行。
- 空值输出空单元格。
- 数值不带单位。
- 小数默认保留 2 位。
