# API 契约

## 统一响应

所有接口返回统一结构。

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-07-11T12:00:00Z"
}
```

错误结构：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "GAME_NOT_FOUND",
    "message": "游戏不存在",
    "details": {}
  },
  "timestamp": "2026-07-11T12:00:00Z"
}
```

## 状态码

- 查询成功：200。
- 新增成功：201。
- 编辑成功：200。
- 删除成功：200。
- 参数错误：400。
- 不存在：404。
- 业务冲突：409。
- 服务器错误：500。

DELETE 不使用 204，避免和统一 JSON 响应体冲突。

## 错误码

- `GAME_NOT_FOUND`
- `SCENE_NOT_FOUND`
- `SCENE_GAME_MISMATCH`
- `TEMPLATE_NOT_FOUND`
- `RECORD_NOT_FOUND`
- `RECORD_GAME_MISMATCH`
- `INVALID_NUMBER`
- `VALIDATION_ERROR`
- `DUPLICATE_DATA`
- `EXPORT_FAILED`
- `DB_ERROR`

MVP 不引入复杂幂等错误码。重复提交依靠前端禁用按钮、数据库约束和标准错误处理。

## 游戏接口

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

删除游戏会级联删除测试场景、配置模板和测试记录。前端必须二次确认。

## 测试场景接口

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

### 删除场景

- 方法：DELETE
- URL：`/api/scenes/{id}`
- 成功：200
- 失败：404
- 页面：测试场景管理

删除场景后，历史记录的 `scene_id` 允许变为空，场景快照必须保留。

## 配置模板接口

### 模板列表

- 方法：GET
- URL：`/api/games/{gameId}/config-templates`
- 成功：200
- 页面：配置模板管理

### 新增模板

- 方法：POST
- URL：`/api/games/{gameId}/config-templates`
- 校验：`gpu_power_limit_percent` 允许 -100 到 100
- 成功：201
- 页面：配置模板管理

### 编辑模板

- 方法：PUT
- URL：`/api/config-templates/{id}`
- 校验：`gpu_power_limit_percent` 允许 -100 到 100
- 成功：200
- 页面：配置模板管理

### 删除模板

- 方法：DELETE
- URL：`/api/config-templates/{id}`
- 成功：200
- 页面：配置模板管理

删除模板不改变历史记录。`config_template_id` 只用于记录来源。

## 测试记录接口

### 记录列表

- 方法：GET
- URL：`/api/benchmark-records`
- Query：`gameId`、`sceneId`、`page`、`size`、`sort`
- 成功：200
- 页面：测试记录列表

### 新增记录

- 方法：POST
- URL：`/api/benchmark-records`
- 必填：`gameId`、`sceneId`、`testDate`、`resolution`、`graphicsPreset`、`averageFps`
- 校验：`averageFps > 0`
- 校验：其他 FPS 指标为空或大于等于 0
- 校验：`gpu_power_limit_percent` 允许 -100 到 100
- 成功：201
- 页面：新增测试记录

新增记录时，Service 必须校验场景存在且属于所选游戏，并复制场景快照。

### 记录详情

- 方法：GET
- URL：`/api/benchmark-records/{id}`
- 成功：200
- 失败：404
- 页面：测试记录详情、编辑测试记录

### 编辑记录

- 方法：PUT
- URL：`/api/benchmark-records/{id}`
- 校验：同新增记录
- 成功：200
- 页面：编辑测试记录

编辑历史记录用于修正录入错误。编辑后更新 `updated_at`。前端必须提示正在修改历史测试数据。

### 删除记录

- 方法：DELETE
- URL：`/api/benchmark-records/{id}`
- 成功：200
- 页面：测试记录列表

## 双记录对比接口

### 对比

- 方法：POST
- URL：`/api/benchmark-records/compare`
- 请求体：`baseRecordId`、`targetRecordId`
- 成功：200
- 失败：400、404、409
- 页面：双记录对比

校验：

- 两条记录必须存在。
- 两条记录必须属于同一个游戏。
- 旧值为空或 0 时，不计算变化率。
- 功耗为空或 0 时，不计算 FPS/W。

必须返回：

- 平均 FPS 变化率。
- 1% Low 变化率。
- 0.1% Low 变化率。
- GPU 功耗变化率。
- GPU 功耗下降率。
- GPU 温度差异。
- GPU 热点温度差异。
- FPS/W。

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
