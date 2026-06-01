# 后台管理子系统 API 接口文档 (v1.0.9)

## 文档版本修订

| 版本   | 日期       | 修订说明 |
| :----- | :--------- | :------- |
| v1.0.9 | 2026-06-01 | **§5.2** 与 `admin-server` 实现对齐：`popularity`/`imageUrls` 出现场景、多图组装顺序与主图解析、`sort` 大小写与错误文案、`pageSize` 截断策略、相关推荐卡片字段与空 `period` 跳档、错误码表；修正 CSV 小节对 **§5.2.7** 的交叉引用。 |
| v1.0.8 | 2026-06-01 | **M2 文物浏览增强**：**§5.2.3** 列表新增 Query `period`、`type`、`material`、`sort` 及响应字段 `popularity`；新增 **§5.2.4** `GET /api/v1/data/relics/filters`；**§5.2.5** 详情补充 `imageUrls`；新增 **§5.2.6** `GET /api/v1/data/relics/{objectId}/related`；原 **§5.2.4～§5.2.10** 顺延为 **§5.2.7～§5.2.13**；**§5.2.1** 补充 filters、related 的 GET 鉴权说明。 |
| v1.0.7 | 2026-05-27 | 重写 **§7 日志管理** 为与 **§5.3 博物馆数据**一致的结构化风格（权限、对象模型、分页/详情/导出等）；新增日志下载接口 **GET** `/api/v1/logs/download`，约定导出返回的 `downloadUrl` 指向该 API。 |
| v1.0.6 | 2026-05-27 | **§7 日志管理**与代码实现对齐：补充分页与参数校验规则、默认值、返回字段模型、错误码；明确日志导出仅支持 `CSV`、`type` 取值为 `OPERATION/SYSTEM/SECURITY`，并补充当前实现返回本地 `file://` 下载地址。 |
| v1.0.5 | 2026-05-21 | 按照模块重新组织文档结构，分为：讲解审核、数据管理、备份与恢复、用户管理四大模块；保留 v1.0.4 所有功能内容，补充各模块功能说明。 |
| v1.0.4 | 2026-05-13 | **§6.1** 与实现对齐：**POST** 创建文物请求体不再包含 **`imageUrl`/`imagePath`**（由服务端按 **`app.relics`** 与 **`objectId`** 写入）；**`museumId`** 创建时**必填**；**§6.1.6** 上传图片成功响应补充 **`imageUrl`**，并注明落盘后**同步更新库表**；新增 **§6.1.10 CSV 批量创建**（**POST** `/api/v1/data/relics/import-csv`）；**§6.1.1** 补充 **import-csv** 仅 **ADMIN**；**§1.5** 补充业务码 **413**；**§6.1.9** 补充 CSV/413 相关场景。 |
| v1.0.3 | 2026-05-13 | 在 **§6.1 文物数据** 下新增 **§6.1.6 上传文物图片**：**POST** `/api/v1/data/relics/{objectId}/image`（`multipart/form-data`，字段 **`file`**）；白名单 **JPEG/PNG/GIF/WebP**、单文件 **10 MB** 上限、落盘 **`src/main/resources/relics-images/`**、文件名为 **`{objectId}.<扩展名>`**；**§6.1.1** 补充该接口仅 **ADMIN**；原 **§6.1.6～§6.1.8** 顺延为 **§6.1.7～§6.1.9**。 |
| v1.0.2 | 2026-05-12 | 新增 **§6.2 博物馆数据 CRUD**：资源路径 **`/api/v1/data/museums`**，补充 **`MuseumObject`** 字段模型、分页/详情/创建/更新/删除及 JSON 示例；权限规则与 **§6.1** 中文物接口一致（GET 任意有效 JWT，写操作仅 **`user.user_type=ADMIN`**）；删除为**物理删除**，响应体 **`{ objectId, deleted }`**。原 **§6.2～§6.4** 顺延为 **§6.3～§6.5**。 |
| v1.0.1 | 2026-05-12 | 扩写 **§6.1 文物数据 CRUD**：补充 Query 参数、`RelicObject` 字段模型、基于 `user.user_type` 的读写权限、请求/响应 JSON 示例及软删除语义。 |
| v1.0.0 | —          | 初版。   |

## 1. 通用规范

### 1.1 接口基础路径

```text
开发环境: http://localhost:8080/api/v1
生产环境: https://api.your-domain.com/api/v1
```

### 1.2 认证方式

- 采用 **JWT Bearer Token** 认证。

- 登录成功后返回 `accessToken`，后续请求在 Header 中携带：

  ```text
  Authorization: Bearer {accessToken}
  ```

- Token 过期后调用刷新接口获取新 Token。

### 1.3 全局唯一 ID

- 所有资源标识统一使用 `objectId`（UUID v4 格式字符串，如 `"550e8400-e29b-41d4-a716-446655440000"`）。
- 路径参数、请求体、响应体中的资源 ID 字段一律命名为 `objectId`。

### 1.4 通用响应格式

**成功响应**

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**分页响应**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ ... ],
    "total": 150,
    "page": 1,
    "pageSize": 10
  }
}
```

**错误响应**

```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": null
}
```

### 1.5 通用错误码

| 错误码 | 说明                |
| :----- | :------------------ |
| 200    | 成功                |
| 400    | 请求参数错误        |
| 401    | 未认证 / Token 失效 |
| 403    | 无操作权限          |
| 404    | 资源不存在          |
| 413    | 请求体过大（如单文件超过 **10 MB**） |
| 500    | 服务器内部错误      |
| 1001   | 用户名或密码错误    |
| 1002   | 账号已被禁用        |
| 1003   | 验证码错误（如需）  |
| 2001   | 角色名称已存在      |
| 2002   | 角色不可删除        |
| 3001   | 待审核内容不存在    |
| 3002   | 审核规则已存在      |
| 4001   | 备份文件不存在      |
| 4002   | 恢复失败            |
| 5001   | 日志导出失败        |

### 1.6 命名约定

- 字段名统一使用 **camelCase**（驼峰命名）。
- 枚举值使用大写字母 + 下划线（如 `STATUS_ENABLED`）。
- 日期时间格式统一为 **ISO 8601** 字符串（`yyyy-MM-ddTHH:mm:ss`）。

---

## 2. 认证接口

### 2.1 登录

**POST** `/api/v1/auth/login`

**Request Body**

```json
{
  "username": "admin",
  "password": "123456"
}
```

**Response (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJ...",
    "refreshToken": "dGhpcyBpcyBy...",
    "expiresIn": 7200
  }
}
```

### 2.2 登出

**POST** `/api/v1/auth/logout`
Header 携带 Token。

**Response (200)**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 2.3 获取当前用户信息

**GET** `/api/v1/auth/current-user`

**Response (200)**

```json
{
  "code": 200,
  "data": {
    "objectId": "u-123e4567...",
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "https://xxx/avatar.jpg",
    "roles": ["SUPER_ADMIN"],
    "permissions": ["user:read", "user:write", "role:read"]
  }
}
```

### 2.4 刷新 Token

**POST** `/api/v1/auth/refresh-token`

**Request Body**

```json
{
  "refreshToken": "dGhpcyBpcyBy..."
}
```

**Response (200)** 同登录成功。

---

## 3. 用户管理模块

### 3.1 模块概述

用户管理模块负责系统用户的全生命周期管理，包括用户信息维护、角色权限分配、状态管理等功能。该模块支持管理员对系统用户进行精细化管理，确保系统安全稳定运行。

### 3.2 角色管理

#### 3.2.1 获取角色列表（分页）

**GET** `/api/v1/roles?page=1&pageSize=10&keyword=超级`

**Response (200)**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "objectId": "r-550e8400...",
        "roleName": "超级管理员",
        "roleCode": "SUPER_ADMIN",
        "description": "拥有所有权限",
        "isSystem": true,
        "createTime": "2026-05-01T10:00:00",
        "updateTime": "2026-05-01T10:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 10
  }
}
```

#### 3.2.2 创建角色

**POST** `/api/v1/roles`

**Request Body**

```json
{
  "roleName": "内容审核员",
  "roleCode": "CONTENT_AUDITOR",
  "description": "负责内容审核",
  "permissionIds": ["p-001", "p-002"]
}
```

#### 3.2.3 更新角色

**PUT** `/api/v1/roles/{objectId}`

**Request Body** 同创建（不含 `roleCode` 即可）。

#### 3.2.4 删除角色

**DELETE** `/api/v1/roles/{objectId}`
*注：系统内置角色不可删除，返回错误码 2002。*

#### 3.2.5 获取角色权限树

**GET** `/api/v1/roles/{objectId}/permissions`

**Response**

```json
{
  "code": 200,
  "data": [
    {
      "objectId": "p-001",
      "name": "用户管理",
      "code": "user:read",
      "checked": true,
      "children": []
    }
  ]
}
```

#### 3.2.6 设置角色权限

**PUT** `/api/v1/roles/{objectId}/permissions`

**Request Body**

```json
{
  "permissionIds": ["p-001", "p-003"]
}
```

#### 3.2.7 获取所有权限树

**GET** `/api/v1/permissions`

### 3.3 用户管理

#### 3.3.1 获取用户列表

**GET** `/api/v1/users?page=1&pageSize=10&keyword=&status=ENABLED&role=SUPER_ADMIN`

**Response (200)**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "objectId": "u-123e4567...",
        "username": "wanghaixin",
        "nickname": "王海鑫",
        "email": "wang@example.com",
        "phone": "13800138000",
        "status": "ENABLED",
        "roles": ["SUPER_ADMIN"],
        "lastLoginTime": "2026-05-02T08:30:00",
        "createTime": "2025-12-01T09:00:00"
      }
    ],
    "total": 42,
    "page": 1,
    "pageSize": 10
  }
}
```

#### 3.3.2 创建用户

**POST** `/api/v1/users`

**Request Body**

```json
{
  "username": "sheyoutian",
  "password": "123456",
  "nickname": "申由田",
  "email": "shen@example.com",
  "phone": "13912345678",
  "roleIds": ["r-auditor"]
}
```

#### 3.3.3 更新用户

**PUT** `/api/v1/users/{objectId}`

**Request Body**

```json
{
  "nickname": "申由田",
  "email": "newemail@example.com",
  "phone": "13900001111",
  "roleIds": ["r-auditor", "r-data"]
}
```

#### 3.3.4 删除用户

**DELETE** `/api/v1/users/{objectId}`
*逻辑删除，用户状态变为 `DISABLED`。*

#### 3.3.5 更新用户状态

**PUT** `/api/v1/users/{objectId}/status`

**Request Body**

```json
{
  "status": "DISABLED"
}
```

`status` 枚举：`ENABLED`、`DISABLED`。

#### 3.3.6 查看用户行为记录

**GET** `/api/v1/users/{objectId}/logs?page=1&pageSize=20`

#### 3.3.7 用户违规处理（选做）

**POST** `/api/v1/users/{objectId}/violations`

**Request Body**

```json
{
  "type": "WARNING",
  "reason": "多次发布违规评论",
  "punishment": "MUTE_3D"
}
```

---

## 4. 讲解审核模块

### 4.1 模块概述

讲解审核模块负责对平台用户生成内容（UGC）进行审核管理，包括审核规则配置、待审核队列管理、人工审核操作及审核统计等功能，确保平台内容合规性。

### 4.2 审核规则配置

#### 4.2.1 获取审核规则配置

**GET** `/api/v1/audit/rules`

#### 4.2.2 更新审核规则

**PUT** `/api/v1/audit/rules`

**Request Body**

```json
{
  "textAutoAuditEnabled": true,
  "imageAutoAuditEnabled": true,
  "sensitiveWordFilterEnabled": true,
  "textAuditAction": "REJECT",
  "imageAuditAction": "MANUAL"
}
```

### 4.3 审核队列管理

#### 4.3.1 待审核队列（分页）

**GET** `/api/v1/audit/queue?page=1&pageSize=10&type=ALL&status=PENDING`

**Response**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "objectId": "ugc-abc123...",
        "type": "COMMENT",
        "content": "这是一条评论...",
        "author": {
          "objectId": "u-456",
          "nickname": "用户A"
        },
        "autoAuditResult": "PASS",
        "status": "PENDING",
        "submitTime": "2026-05-02T12:00:00"
      }
    ],
    "total": 23,
    "page": 1,
    "pageSize": 10
  }
}
```

#### 4.3.2 获取审核详情

**GET** `/api/v1/audit/queue/{objectId}`

### 4.4 审核操作

#### 4.4.1 人工审核通过

**POST** `/api/v1/audit/queue/{objectId}/approve`

**Request Body (可选)**

```json
{
  "remark": "内容合规"
}
```

#### 4.4.2 人工审核拒绝

**POST** `/api/v1/audit/queue/{objectId}/reject`

**Request Body**

```json
{
  "reason": "包含违规内容",
  "remark": "已屏蔽"
}
```

#### 4.4.3 批量通过

**POST** `/api/v1/audit/queue/batch-approve`

**Request Body**

```json
{
  "objectIds": ["ugc-001", "ugc-002"],
  "remark": "批量操作"
}
```

#### 4.4.4 批量拒绝

**POST** `/api/v1/audit/queue/batch-reject`

**Request Body**

```json
{
  "objectIds": ["ugc-003"],
  "reason": "广告骚扰"
}
```

### 4.5 审核统计

**GET** `/api/v1/audit/statistics?startDate=2026-05-01&endDate=2026-05-06`

**Response**

```json
{
  "code": 200,
  "data": {
    "totalSubmitted": 500,
    "autoApproved": 300,
    "autoRejected": 50,
    "manualApproved": 120,
    "manualRejected": 30,
    "pendingCount": 0
  }
}
```

---

## 5. 数据管理模块

### 5.1 模块概述

数据管理模块负责平台核心数据的管理与维护，包括文物数据、博物馆数据、知识图谱数据及用户生成内容（UGC）的增删改查操作，是系统数据治理的核心模块。

### 5.2 文物数据 CRUD

关系型存储对应数据库表 **`artifact`**（字段定义见《后台管理子系统 数据库设计文档》**§3.4**）；REST 资源集合路径为 **`/api/v1/data/relics`**，JSON 字段一律 **camelCase**，主键字段 **`objectId`**（UUID v4 字符串）。

#### 5.2.1 权限

- 所有接口须在 Header 携带 **`Authorization: Bearer {accessToken}`**；未携带、格式错误、过期或签名校验失败返回 **401**（见 **§1.5**）。  
- **GET** `/api/v1/data/relics`、**GET** `/api/v1/data/relics/filters`、**GET** `/api/v1/data/relics/{objectId}`、**GET** `/api/v1/data/relics/{objectId}/related`：任意 **有效** Token 即可访问（不区分 `user_type`）。  
- **POST** `/api/v1/data/relics`、**POST** `/api/v1/data/relics/import-csv`、**POST** `/api/v1/data/relics/{objectId}/image`、**PUT** `/api/v1/data/relics/{objectId}`、**DELETE** `/api/v1/data/relics/{objectId}`：仅当当前登录用户在 **`user`** 表中 **`user_type` 取值为 `ADMIN`** 时允许；否则返回 **403**，`code` 为 **403**，`message` 建议为 **「无操作权限」**。文物写权限**不**依据 RBAC 的 `roles` / `roleCode` 判定。

#### 5.2.2 文物对象 `RelicObject`（与 `artifact` 对齐）

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| objectId | string | 响应必填；创建请求勿传 | UUID v4 |
| title | string | 是 | 最大长度 **500** 字符 |
| period | string | 否 | 最大 **200** 字符 |
| type | string | 否 | 最大 **100** 字符 |
| material | string | 否 | 最大 **200** 字符 |
| description | string | 否 | 文本 |
| dimensions | string | 否 | 最大 **300** 字符 |
| museumId | string | 创建请求**必填**；响应必填 | 关联 `museum.object_id`，最大 **36** 字符（与 **`CreateRelicRequest`** 一致） |
| detailUrl | string | 是 | 最大 **1000** 字符 |
| imageUrl | string | 响应必填；**创建 JSON 勿传** | 最大 **1000** 字符；由服务端按配置 **`app.relics.image-public-base-url`** 与 **`objectId`** 生成并落库 |
| imagePath | string | 响应必填；**创建 JSON 勿传** | 最大 **500** 字符；由服务端生成，形如 **`relics-images/{objectId}.{ext}`**（**`ext`** 默认 **`jpg`**，见 **`app.relics.default-image-extension`**） |
| creditLine | string | 否 | 最大 **500** 字符 |
| accessionNumber | string | 否 | 最大 **100** 字符 |
| crawlDate | string | 是 | 日期 **`yyyy-MM-dd`** |
| createTime | string | 响应必填；创建请求勿传 | **ISO 8601**：`yyyy-MM-ddTHH:mm:ss` |
| updateTime | string | 否 | **ISO 8601** |
| isDeleted | number | 否 | **0** 正常，**1** 已软删；创建时默认 **0**；**列表接口不返回** `isDeleted=1` 的记录 |
| popularity | number | 凡返回完整或卡片 **`RelicObject`** 时均有 | 非负整数 **0～999999**；服务端按 `createTime` 的日期与当前日期的整天数差计算：`max(0, 1000 - daysSinceCreate)`（`createTime` 为 `null` 时为 **0**）。列表 **`sort=hot`** 与相关推荐排序在 SQL 层使用等价表达式 `GREATEST(0, 1000 - DATEDIFF(CURDATE(), DATE(create_time)))` |
| imageUrls | string[] | 仅 **详情** `GET .../{objectId}` | 多图轮播完整 HTTP(S) URL；**`imageUrls[0]`** 与 **`imageUrl`** 一致；长度 **≥ 1**。列表、创建、更新响应**不填充**该字段（JSON 中通常省略） |

#### 5.2.3 分页列表

**GET** `/api/v1/data/relics`

**Query 参数**

| 参数      | 类型   | 必填 | 说明 |
| :-------- | :----- | :--- | :--- |
| page      | number | 否   | 默认 **1**，最小 **1** |
| pageSize  | number | 否   | 默认 **10**，最大 **100**；**当前实现**：大于 **100** 时**静默截断为 100**（不返回 **400**） |
| keyword   | string | 否   | 模糊匹配 **`title`** 或 **`accessionNumber`** |
| museumId  | string | 否   | 精确匹配 **`museum_id`** |
| period    | string | 否   | 精确匹配 **`period`**；空字符串视为未传 |
| type      | string | 否   | 精确匹配 **`type`** |
| material  | string | 否   | 精确匹配 **`material`** |
| sort      | string | 否   | 排序：`hot`（默认）、`name`、`period`；**大小写不敏感**；未传或仅空白视为 `hot`；非法值 **400**，`message`：**「sort 取值须为 hot、name 或 period」** |

**排序规则**

| sort | 规则 |
| :--- | :--- |
| `hot`（默认） | `GREATEST(0, 1000 - DATEDIFF(CURDATE(), DATE(create_time)))` 降序，相同则 `create_time` 降序 |
| `name` | `title` 升序（UTF-8 字典序） |
| `period` | `period IS NULL` 排末尾，其次 `period` 升序，再 `title` 升序 |

**Response (200)** — 分页结构见 **§1.4**；`records[]` 每项含 **`popularity`**。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "objectId": "550e8400-e29b-41d4-a716-446655440001",
        "title": "青铜鼎",
        "period": "商代晚期",
        "type": "青铜器",
        "material": "青铜",
        "description": "示例介绍文本。",
        "dimensions": "高 50cm",
        "museumId": "m-550e8400-e29b-41d4-a716-446655440000",
        "detailUrl": "https://museum.example.org/object/12345",
        "imageUrl": "https://cdn.example.org/relics/12345.jpg",
        "imagePath": "/data/images/12345.jpg",
        "creditLine": "Courtesy of Example Museum",
        "accessionNumber": "1924.123",
        "crawlDate": "2026-05-01",
        "createTime": "2026-05-02T10:00:00",
        "updateTime": "2026-05-10T08:30:00",
        "isDeleted": 0,
        "popularity": 980
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

#### 5.2.4 筛选元数据

**GET** `/api/v1/data/relics/filters`

- 返回当前库内未删除文物实际存在的年代、类型、材质，以及至少关联 1 件文物的博物馆列表。  
- 各维度 DISTINCT 后升序，单维度最多 **500** 条（`period`/`type`/`material` 排除 `NULL` 与空字符串）。  
- 某维度无数据时对应数组为 **`[]`**，仍返回 **200**。
- **路由**：须在服务端注册于 **`GET .../{objectId}`** 之前（当前 `RelicController` 已按此顺序声明），避免路径 `filters` 被当作 `objectId`。

**Response (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "periods": ["商代晚期", "唐代"],
    "types": ["青铜器", "陶瓷"],
    "materials": ["青铜", "瓷"],
    "museums": [
      {
        "objectId": "m-550e8400-e29b-41d4-a716-446655440000",
        "name": "Example Museum",
        "nameCn": "示例博物馆"
      }
    ]
  }
}
```

#### 5.2.5 详情

**GET** `/api/v1/data/relics/{objectId}`

- 路径参数 **`objectId`** 为文物主键。  
- 资源不存在，或 **`isDeleted=1`**，或 **`RelicImageUrlsResolver` 解析后无任何可访问图片 URL**：返回 **404**，`code` **404**，`message` 建议 **「资源不存在」**，`data` 为 **null**。  
- 成功时 `data` 为单个 **`RelicObject`**；在列表字段基础上增加 **`imageUrls`**（**≥ 1** 个完整 HTTP(S) URL）及 **`popularity`**。  
- **主图 `imageUrl` 解析**（`RelicPublicUrlBuilder.resolvePrimary`）：若库表 **`image_url`** 已是 **`http://` 或 `https://` 开头**，则原样使用（超过 **1000** 字符时截断至 **1000**）；否则按 **`image_path`** 与 **`app.relics.image-public-base-url`** 拼接。未配置基址且需拼接时，写操作类接口返回 **500**（见创建逻辑）。  
- **多图 `imageUrls` 组装**（`RelicImageUrlsResolver`）：  
  1. 将主图 URL 置于首位（见上）；  
  2. 扫描图片落盘根目录（**`RelicImageStorage`**，默认 classpath **`relics-images/`**）下文件名匹配 **`{objectId}.{ext}`** 或 **`{objectId}-{n}.{ext}`**（**`n` 仅 2～9**；**`ext`** 为 **jpg/jpeg/png/gif/webp**，扩展名大小写不敏感）；  
  3. 磁盘文件按附加序号升序、同序号按文件名字典序排序后追加；  
  4. **`LinkedHashSet` 去重**后返回；**`imageUrl`** 与 **`imageUrls[0]`** 一致。

**Response (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "objectId": "550e8400-e29b-41d4-a716-446655440001",
    "title": "青铜鼎",
    "period": "商代晚期",
    "type": "青铜器",
    "material": "青铜",
    "description": "示例介绍文本。",
    "dimensions": "高 50cm",
    "museumId": "m-550e8400-e29b-41d4-a716-446655440000",
    "detailUrl": "https://museum.example.org/object/12345",
    "imageUrl": "https://cdn.example.org/relics-images/550e8400-e29b-41d4-a716-446655440001.jpg",
    "imageUrls": [
      "https://cdn.example.org/relics-images/550e8400-e29b-41d4-a716-446655440001.jpg",
      "https://cdn.example.org/relics-images/550e8400-e29b-41d4-a716-446655440001-2.jpg"
    ],
    "imagePath": "relics-images/550e8400-e29b-41d4-a716-446655440001.jpg",
    "creditLine": "Courtesy of Example Museum",
    "accessionNumber": "1924.123",
    "crawlDate": "2026-05-01",
    "createTime": "2026-05-02T10:00:00",
    "updateTime": "2026-05-10T08:30:00",
    "isDeleted": 0,
    "popularity": 980
  }
}
```

#### 5.2.6 相关推荐

**GET** `/api/v1/data/relics/{objectId}/related`

- 当前文物不存在或已软删：**404**。  
- 成功返回 **`periodTag`**（当前文物 `period` 非空时原样返回，否则 **`null`**）与 **`related`**（最多 **10** 条，不含当前文物）。  
- 推荐优先级（按档补足至 **10** 条，每档内按 SQL 热度降序，单档最多取当前剩余名额）：  
  1. 当前文物 **`period` 非空**时：同 **`period`**；  
  2. 仍不足且 **`type` 非空**：同 **`type`**；  
  3. 仍不足且 **`museumId` 非空**：同 **`museumId`**。  
- **`related[]` 卡片字段**（`RelicAssembler.toBrowseCard`，非完整 `RelicObject`）：**`objectId`**、**`title`**、**`period`**、**`type`**、**`imageUrl`**、**`popularity`**。主图解析规则同详情 **`resolvePrimary`**；无法解析时 **`imageUrl`** 可能为 **`null`**。

**Response (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "periodTag": "商代晚期",
    "related": [
      {
        "objectId": "550e8400-e29b-41d4-a716-446655440002",
        "title": "青铜爵",
        "period": "商代晚期",
        "type": "青铜器",
        "imageUrl": "https://cdn.example.org/relics-images/550e8400-e29b-41d4-a716-446655440002.jpg",
        "popularity": 975
      }
    ]
  }
}
```

#### 5.2.7 创建

**POST** `/api/v1/data/relics`

**Request Body**

- 客户端**勿传** `objectId`、`createTime`、`updateTime`、`imageUrl`、`imagePath`（由服务端生成并写入 **`artifact`**）；`isDeleted` 可不传，默认 **0**。  
- **必填**：`title`、`museumId`、`detailUrl`、`crawlDate`；其余字段按 **§5.2.2** 选填。  
- **`imageUrl` / `imagePath`**：请求 DTO 中**不包含**该二字段；若客户端仍发送同名 JSON 键（例如经网关透传），由 **Jackson** 默认行为**忽略**未知属性，**不得**采用客户端值落库。  
- **`imageUrl` / `imagePath`** 生成规则：须配置 **`app.relics.image-public-base-url`**（非空）；未配置时创建失败（**`code`** **500**，`message` 含配置提示）。**`imagePath`** 形如 **`relics-images/{objectId}.{defaultExt}`**，**`defaultExt`** 来自 **`app.relics.default-image-extension`**（默认 **`jpg`**）。  
- 任一字符串超出最大长度或 Bean 校验失败：**400**。

```json
{
  "title": "新文物",
  "period": "唐代",
  "type": "陶瓷",
  "material": "瓷",
  "description": "简介",
  "dimensions": null,
  "museumId": "m-550e8400-e29b-41d4-a716-446655440000",
  "detailUrl": "https://museum.example.org/object/new",
  "creditLine": null,
  "accessionNumber": "2026.001",
  "crawlDate": "2026-05-12"
}
```

**Response (200)**：`data` 为创建后的完整 **`RelicObject`**。

#### 5.2.8 上传文物图片

**POST** `/api/v1/data/relics/{objectId}/image`

- **权限**：与 **§5.2.1** 文物写操作一致，仅 **`user.user_type=ADMIN`**；否则 **403**。未认证 **401**。  
- **路径参数**：**`objectId`** — 文物主键（与 **`artifact.object_id`** 及 PRD 中的 **`artifact_id`** 为同一标识）。目标不存在或 **`isDeleted=1`**：**404**（与详情接口一致）。  
- **Content-Type**：**`multipart/form-data`**。  
- **表单字段**：**`file`**（**必填**）— 单个图片文件。

**支持格式（白名单）**

| 类型 | MIME（示例） | 落盘扩展名（小写） |
| :--- | :--- | :--- |
| JPEG | `image/jpeg` | **`.jpg`**（对 `image/jpeg` 统一保存为 **`.jpg`**） |
| PNG | `image/png` | **`.png`** |
| GIF | `image/gif` | **`.gif`** |
| WebP | `image/webp` | **`.webp`** |

- 服务端须通过 **Content-Type** 与/或文件魔数校验；不在上表内：**400**，`message` 建议为 **「不支持的图片格式」**。  
- **空文件**、缺少 **`file`**、请求体非 multipart：**400**。  
- **单文件最大体积**：**10 MB**；超出时响应 **`code`** **413**，`message` 为 **「单张图片不能超过 10 MB」**（与 **`MaxUploadSizeExceededException`** 处理一致；**HTTP** 状态码与全局 **`Result`** 约定一致，一般为 **200**）。

**存储路径与文件命名**

- **源码目录（Maven `admin-server` 模块）**：**`admin-server/src/main/resources/relics-images/`**（可通过 **`app.relic-images.directory`** 覆盖落盘根路径；未配置时开发环境常用 **`target/classes/relics-images`**）。打包后默认位于 classpath **`relics-images/`** 下。  
- **文件名**：**`{objectId}.<扩展名>`**，其中 **`<扩展名>`** 与实际上传格式对应且为小写（见上表）。  
- **覆盖**：同一 **`objectId`** 再次上传且扩展名相同时**覆盖**原文件；若新扩展名与旧文件不同，服务端**删除**同 **`objectId`** 下其它扩展名的旧文件，避免磁盘残留。

**Response (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "objectId": "550e8400-e29b-41d4-a716-446655440001",
    "imagePath": "relics-images/550e8400-e29b-41d4-a716-446655440001.jpg",
    "imageUrl": "https://cdn.example.org/relics-images/550e8400-e29b-41d4-a716-446655440001.jpg"
  }
}
```

- **`data.imagePath`**、**`data.imageUrl`**：与 **§5.2.2** **`RelicObject`** 字段语义一致；上传成功后服务端**同步更新**当前文物在库表中的 **`image_path`**、**`image_url`** 及 **`update_time`**。

#### 5.2.9 更新

**PUT** `/api/v1/data/relics/{objectId}`

- **Body**：支持**部分字段**更新；未出现的字段保持原值。  
- 目标 **`objectId`** 不存在，或记录 **`isDeleted=1`**：**404**。  
- 成功时 **`updateTime`** 由服务端更新；`data` 为更新后的完整 **`RelicObject`**。

**Request Body（示例，仅改标题）**

```json
{
  "title": "青铜鼎（修订）"
}
```

#### 5.2.10 删除（软删除）

**DELETE** `/api/v1/data/relics/{objectId}`

- **语义**：**软删除**，将对应行 **`isDeleted` 置为 1**，不物理删除数据。  
- 目标不存在或 **`isDeleted=1`**：**404**。  
- 成功：**HTTP 200**，`code` **200**，`data` 固定为：

```json
{
  "objectId": "550e8400-e29b-41d4-a716-446655440001",
  "isDeleted": 1
}
```

#### 5.2.11 错误与行为小结

| 场景 | HTTP | code |
| :--- | :--- | :--- |
| 未认证 / Token 无效 | 401 | 401 |
| 写操作且 `user.user_type` ≠ `ADMIN` | 403 | 403 |
| 参数缺失或超长、非法 `page` / `pageSize`（`page` 或 `pageSize` &lt; 1，由 `@Min` 校验） | 400 | 400 |
| 列表非法 `sort` | 400 | 400 |
| 上传：非白名单图片类型、空文件、缺 `file`、非法 multipart | 400 | 400 |
| 上传：单文件超过 **10 MB** | 413 | 413 |
| CSV：文件超过 **10 MB** | 413 | 413 |
| CSV：表头缺列、列冲突、无数据行、行必填缺失、日期非法、重复行、数据行超 **2000**、非 UTF-8 / 解析失败等 | 400 | 400 |
| CSV：整批插入事务内失败 | 500 或 400 | 500 或 400 |
| 详情：目标文物不存在、已软删，或无法解析任何图片 URL | 404 | 404 |
| 相关推荐：当前文物不存在或已软删 | 404 | 404 |
| 更新/删除/上传图片：目标文物不存在或已软删 | 404 | 404 |

#### 5.2.12 CSV 批量创建文物

**POST** `/api/v1/data/relics/import-csv`

- **权限**：与 **§5.2.1** 文物写操作一致，仅 **`user.user_type=ADMIN`**；否则 **403**。未认证 **401**。  
- **Content-Type**：**`multipart/form-data`**。  
- **表单字段**：**`file`**（**必填**）— 扩展名为 **`.csv`** 的文本文件。  
- **编码**：**UTF-8**（严格解码；非法字节序列返回 **`code`** **400**）；支持文件头 **UTF-8 BOM**。  
- **单文件最大体积**：**10 MB**；超出 **`code`** **413**，`message`：**「CSV 文件不能超过 10 MB」**。  
- **最大数据行数**（不含表头）：**2000**；超出 **`code`** **400**。

**表头与列名匹配**

- 首行必须为**表头**；表头不能为空；须能映射到 **`CreateRelicRequest`** 的**全部** Java 属性列（当前 **11** 列，顺序无关）：**`title`**、**`period`**、**`type`**、**`material`**、**`description`**、**`dimensions`**、**`museumId`**、**`detailUrl`**、**`creditLine`**、**`accessionNumber`**、**`crawlDate`**。  
- **列名规范化**（表头与属性名比较前均执行）：转小写、去掉 ASCII **`_`**、去掉所有 Unicode 空白字符。  
- **缺列**（任一属性无匹配列）或**列冲突**（两列表头规范化后映射到同一属性）：**`code`** **400**。  
- 其它未匹配到上述 **11** 属性的表头列：**忽略**。

**数据行**

- 自第二行起为数据行；**整行无非空白内容**的行**跳过**。  
- 须至少存在 **1** 条有效数据行；否则 **`code`** **400**，`message`：**「CSV 无数据行」**。  
- **必填单元格**（trim 后非空）：**`title`**、**`museumId`**、**`detailUrl`**、**`crawlDate`**（日期格式 **`yyyy-MM-dd`**，与 **ISO_LOCAL_DATE** 一致）。  
- **可选单元格**：**`period`**、**`type`**、**`material`**、**`description`**、**`dimensions`**、**`creditLine`**、**`accessionNumber`** 允许为空或仅空白。  
- 每行构造 **`CreateRelicRequest`** 后执行 **Bean Validation**；失败 **`code`** **400**，`message` 含 **`第 N 行:`** 前缀。  
- **重复行**：若两行在 **11** 个映射列上 trim 后的取值**完全相同**，**`code`** **400**，`message` 含行号说明。

**事务与插入**

- 全部行校验通过后，在**同一数据库事务**内按 CSV 数据行顺序依次调用与 **§5.2.7** 相同的插入逻辑（含 **`imageUrl`/`imagePath`** 服务端生成）。任一行插入失败：**整批回滚**，返回 **`code`** **500** 或 **400**（依错误类型）。  
- **成功**：**`code`** **200**，**`data`** 为对象 **`{ "objectIds": [ ... ] }`**，数组元素为按插入顺序生成的 **`objectId`**（UUID 字符串）。

**Response (200)**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "objectIds": [
      "550e8400-e29b-41d4-a716-446655440001",
      "660e8400-e29b-41d4-a716-446655440002"
    ]
  }
}
```

**配置说明（与 §5.2.7 共用）**

- **`app.relics.image-public-base-url`**、**`app.relics.default-image-extension`**：见 **`application.yml`** / 各环境 **`application-*.yml`**。

### 5.3 博物馆数据 CRUD

关系型存储对应数据库表 **`museum`**（字段定义见《后台管理子系统 数据库设计文档》**§3.4**「表 `museum`」）；REST 资源集合路径为 **`/api/v1/data/museums`**，JSON 字段一律 **camelCase**，主键字段 **`objectId`**（UUID v4 字符串）。

#### 5.3.1 权限

- 所有接口须在 Header 携带 **`Authorization: Bearer {accessToken}`**；未携带、格式错误、过期或签名校验失败返回 **401**（见 **§1.5**）。  
- **GET** `/api/v1/data/museums`、**GET** `/api/v1/data/museums/{objectId}`：任意 **有效** Token 即可访问（不区分 `user_type`）。  
- **POST** / **PUT** / **DELETE**：仅当当前登录用户在 **`user`** 表中 **`user_type` 取值为 `ADMIN`** 时允许；否则返回 **403**，`code` 为 **403**，`message` 建议为 **「无操作权限」**。博物馆写权限**不**依据 RBAC 的 `roles` / `roleCode` 判定。

#### 5.3.2 博物馆对象 `MuseumObject`（与 `museum` 对齐）

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| objectId | string | 响应必填；创建请求勿传 | UUID v4 |
| name | string | 是 | 博物馆英文名称，最大 **200** 字符 |
| nameCn | string | 否 | 中文名称，最大 **200** 字符 |
| location | string | 否 | 所在城市、国家等，最大 **200** 字符 |
| website | string | 否 | 官网 URL，最大 **500** 字符 |

> **说明**：当前库表 **`museum`** 无 `is_deleted` / `create_time` 等字段时，列表即为表内全部行；删除接口为**物理删除**。若数据库中文物表外键 **`artifact.museum_id`** 配置为 **`ON DELETE SET NULL`**，删除博物馆后相关文物的 **`museum_id`** 在库内会被置为 **NULL**。

#### 5.3.3 分页列表

**GET** `/api/v1/data/museums`

**Query 参数**

| 参数      | 类型   | 必填 | 说明 |
| :-------- | :----- | :--- | :--- |
| page      | number | 否   | 默认 **1**，最小 **1** |
| pageSize  | number | 否   | 默认 **10**，最大 **100**；超出时**推荐**按 **100** 截断，或返回 **400**（须在实现与 README 中声明策略） |
| keyword   | string | 否   | 可选；模糊匹配 **`name`** 或 **`nameCn`**（OR） |

**Response (200)** — 分页结构见 **§1.4**，`records[]` 元素为 **`MuseumObject`**。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "objectId": "m-550e8400-e29b-41d4-a716-446655440000",
        "name": "The Metropolitan Museum of Art",
        "nameCn": "大都会艺术博物馆",
        "location": "New York, USA",
        "website": "https://www.metmuseum.org"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

#### 5.3.4 详情

**GET** `/api/v1/data/museums/{objectId}`

- 路径参数 **`objectId`** 为博物馆主键。  
- 资源不存在：返回 **404**，`code` **404**，`data` 为 **null**。  
- 成功时 `data` 为单个 **`MuseumObject`**。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "objectId": "m-550e8400-e29b-41d4-a716-446655440000",
    "name": "The Metropolitan Museum of Art",
    "nameCn": "大都会艺术博物馆",
    "location": "New York, USA",
    "website": "https://www.metmuseum.org"
  }
}
```

#### 5.3.5 创建

**POST** `/api/v1/data/museums`

**Request Body**

- 客户端**勿传** `objectId`（由服务端生成 UUID v4）。  
- **必填**：`name`；其余字段按 **§5.3.2** 选填。  
- 任一字符串超出最大长度：**400**。

```json
{
  "name": "National Museum of China",
  "nameCn": "中国国家博物馆",
  "location": "Beijing, China",
  "website": "https://www.chnmuseum.cn"
}
```

**Response (200)**：`data` 为创建后的完整 **`MuseumObject`**。

#### 5.3.6 更新

**PUT** `/api/v1/data/museums/{objectId}`

- **Body**：支持**部分字段**更新；未出现的字段保持原值。  
- 目标 **`objectId`** 不存在：**404**。  
- 成功时 `data` 为更新后的完整 **`MuseumObject`**。

```json
{
  "nameCn": "中国国家博物馆（修订）"
}
```

#### 5.3.7 删除（物理删除）

**DELETE** `/api/v1/data/museums/{objectId}`

- **语义**：**物理删除** `museum` 表中对应行（与当前无软删列的表结构一致）。  
- 目标不存在：**404**。  
- 若数据库外键策略禁止删除（如未来改为 **RESTRICT** 且仍存在引用）：可实现为 **409** 或 **400**，须在 README 声明。  
- 成功：**HTTP 200**，`code` **200**，`data` 建议为：

```json
{
  "objectId": "m-550e8400-e29b-41d4-a716-446655440000",
  "deleted": true
}
```

#### 5.3.8 错误与行为小结

| 场景 | HTTP | code |
| :--- | :--- | :--- |
| 未认证 / Token 无效 | 401 | 401 |
| 写操作且 `user.user_type` ≠ `ADMIN` | 403 | 403 |
| 参数缺失（如创建缺 `name`）、超长、`page` / `pageSize` 非法（若实现校验） | 400 | 400 |
| 详情/更新/删除目标不存在 | 404 | 404 |
| 删除因外键等数据库约束失败（若实现） | 409 / 400 | 同左 |

### 5.4 知识图谱数据 CRUD

**GET** `/api/v1/data/knowledge-graph?page=1&pageSize=10`

**POST** `/api/v1/data/knowledge-graph`

**PUT** `/api/v1/data/knowledge-graph/{objectId}`

**DELETE** `/api/v1/data/knowledge-graph/{objectId}`

### 5.5 用户生成内容（UGC）管理

**GET** `/api/v1/data/ugc?page=1&pageSize=10&status=ALL&userId=xxx`

**GET** `/api/v1/data/ugc/{objectId}`

**DELETE** `/api/v1/data/ugc/{objectId}`
*仅支持删除，不可修改用户内容。*

### 5.6 数据一致性检查（选做）

**POST** `/api/v1/data/consistency-check`

**Response**

```json
{
  "code": 200,
  "data": {
    "taskId": "task-uuid",
    "status": "RUNNING"
  }
}
```

---

## 6. 备份与恢复模块

### 6.1 模块概述

备份与恢复模块负责系统数据的安全保护，支持手动备份、定时备份任务管理、备份记录查询及数据恢复功能，确保数据安全与业务连续性。

### 6.2 备份操作

#### 6.2.1 手动触发备份

**POST** `/api/v1/backup/manual`

**Request Body**

```json
{
  "backupType": "FULL",
  "description": "2026年5月6日手动全量备份"
}
```

### 6.3 定时备份任务管理

#### 6.3.1 获取定时任务列表

**GET** `/api/v1/backup/schedules`

#### 6.3.2 创建定时任务

**POST** `/api/v1/backup/schedules`

```json
{
  "cronExpression": "0 0 2 * * ?",
  "backupType": "INCREMENTAL",
  "enabled": true,
  "description": "每日凌晨2点增量备份"
}
```

#### 6.3.3 更新定时任务

**PUT** `/api/v1/backup/schedules/{objectId}`

#### 6.3.4 删除定时任务

**DELETE** `/api/v1/backup/schedules/{objectId}`

### 6.4 备份记录管理

#### 6.4.1 查询备份记录（分页）

**GET** `/api/v1/backup/records?page=1&pageSize=10&status=SUCCESS`

#### 6.4.2 获取备份详情（含下载链接）

**GET** `/api/v1/backup/records/{objectId}`

#### 6.4.3 删除备份文件

**DELETE** `/api/v1/backup/records/{objectId}`

### 6.5 数据恢复

#### 6.5.1 从备份恢复

**POST** `/api/v1/backup/restore/{objectId}`
*objectId 为备份记录 ID。*

**Response**

```json
{
  "code": 200,
  "data": {
    "restoreTaskId": "restore-uuid",
    "status": "PROCESSING"
  }
}
```

### 6.6 存储信息

#### 6.6.1 获取备份存储信息

**GET** `/api/v1/backup/storage-info`

**Response**

```json
{
  "code": 200,
  "data": {
    "totalSpaceMB": 20480,
    "usedSpaceMB": 3560,
    "backupCount": 12
  }
}
```

---

## 7. 日志管理

### 7.1 模块概述

日志管理模块负责系统关键行为留痕与审计追踪，覆盖操作日志、系统日志、安全日志查询，以及日志导出与下载能力，支持故障排查和合规审计。

### 7.2 权限

- 所有接口须在 Header 携带 **`Authorization: Bearer {accessToken}`**；未携带、格式错误、过期或签名校验失败返回 **401**（见 **§1.5**）。  
- **GET** `/api/v1/logs/operation`、**GET** `/api/v1/logs/operation/{objectId}`、**GET** `/api/v1/logs/system`、**GET** `/api/v1/logs/security`：需具备日志查询权限（如 `log:read`）。  
- **POST** `/api/v1/logs/export`、**GET** `/api/v1/logs/download`：需具备日志导出权限（如 `log:export`）。  
- 权限不足返回 **403**，`code` 为 **403**，`message` 建议为 **「无操作权限」**。

### 7.3 日志对象模型

#### 7.3.1 操作日志对象 `OperationLogVO`

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| objectId | string | 是 | 日志主键（UUID） |
| userId | string | 否 | 操作用户 ID |
| operator | string | 否 | 操作人展示名（昵称优先，其次用户名） |
| module | string | 是 | 业务模块标识 |
| action | string | 是 | 操作动作 |
| result | string | 否 | 操作结果（如 `SUCCESS` / `FAILED` / `UNKNOWN`） |
| operationTime | string | 是 | ISO 8601 时间 |

#### 7.3.2 操作日志详情对象 `OperationLogDetailVO`

在 `OperationLogVO` 基础上补充：

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| ipAddress | string | 否 | 操作来源 IP |
| requestParams | object | 否 | 请求参数（JSON 对象） |
| responseResult | object | 否 | 响应摘要（JSON 对象） |

#### 7.3.3 系统日志对象 `SystemLogVO`

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| objectId | string | 是 | 日志主键（UUID） |
| createTime | string | 是 | ISO 8601 时间 |
| level | string | 是 | 当前支持 `INFO` / `WARN` / `ERROR` |
| serviceName | string | 是 | 服务名，缺省回退 `admin-server` |
| messageSummary | string | 否 | 日志摘要（最长约 120 字符） |

#### 7.3.4 安全日志对象 `SecurityLogVO`

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| objectId | string | 是 | 日志主键（UUID） |
| eventType | string | 是 | 安全事件类型（如 `LOGIN`） |
| eventResult | string | 否 | 事件结果（如 `SUCCESS` / `DENIED` / `FAILED` / `UNKNOWN`） |
| userIdentity | string | 否 | 用户标识展示名 |
| ipAddress | string | 否 | 来源 IP |
| createTime | string | 是 | ISO 8601 时间 |

#### 7.3.5 导出结果对象 `LogExportVO`

| 字段名 | 类型 | 必填 | 约束与说明 |
| :----- | :--- | :--- | :----------- |
| downloadUrl | string | 是 | 下载 API 地址（形如 `/api/v1/logs/download?fileId=...`） |
| expireTime | string | 是 | 下载链接过期时间（ISO 8601） |

### 7.4 操作日志查询

#### 7.4.1 分页列表

**GET** `/api/v1/logs/operation`

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| page | number | 否 | 默认 **1**，最小 **1** |
| pageSize | number | 否 | 默认 **20**，最大 **100**；超出返回 **400** |
| userId | string | 否 | 精确匹配 `operation_log.user_id` |
| module | string | 否 | 精确匹配 `operation_log.module` |
| startTime | string(date-time) | 否 | ISO 8601；不传则默认 `endTime - 24h` |
| endTime | string(date-time) | 否 | ISO 8601；不传则默认当前时间 |

- 当 `startTime > endTime` 时返回 **400**。
- 返回结果按 `operationTime` 倒序。

**Response (200)** — 分页结构见 **§1.4**，`records[]` 元素为 **`OperationLogVO`**。

#### 7.4.2 详情

**GET** `/api/v1/logs/operation/{objectId}`

- 路径参数 **`objectId`** 为操作日志主键。  
- 资源不存在：返回 **404**，`code` **404**，`data` 为 **null**。  
- 成功时 `data` 为 **`OperationLogDetailVO`**。

### 7.5 系统日志分页查询

**GET** `/api/v1/logs/system`

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| page | number | 否 | 默认 **1**，最小 **1** |
| pageSize | number | 否 | 默认 **20**，最大 **100**；超出返回 **400** |
| level | string | 否 | 默认 **ERROR**；当前支持 `INFO` / `WARN` / `ERROR` |

**Response (200)** — 分页结构见 **§1.4**，`records[]` 元素为 **`SystemLogVO`**。

### 7.6 安全日志分页查询

**GET** `/api/v1/logs/security`

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| page | number | 否 | 默认 **1**，最小 **1** |
| pageSize | number | 否 | 默认 **20**，最大 **100**；超出返回 **400** |

**Response (200)** — 分页结构见 **§1.4**，`records[]` 元素为 **`SecurityLogVO`**。

### 7.7 日志导出

**POST** `/api/v1/logs/export`

**Request Body**

```json
{
  "type": "OPERATION",
  "format": "CSV",
  "filters": {
    "startTime": "2026-04-01T00:00:00",
    "endTime": "2026-05-01T23:59:59",
    "module": "AUDIT"
  }
}
```

**字段约束**

- `type`：必填，支持 `OPERATION` / `SYSTEM` / `SECURITY`。  
- `format`：必填，仅支持 `CSV`。  
- `filters.startTime` / `filters.endTime`：可选，需为 ISO 8601 时间；若同时存在且 `startTime > endTime`，返回 **400**。  
- `filters.module`：可选；对 `OPERATION` 与 `SYSTEM` 导出生效。  

**Response (200)**：`data` 为 **`LogExportVO`**。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "downloadUrl": "/api/v1/logs/download?fileId=log_operation_20260527123000_xxxxxxxx",
    "expireTime": "2026-05-27T13:00:00"
  }
}
```

### 7.8 日志下载

**GET** `/api/v1/logs/download?fileId=log_operation_20260527123000_xxxxxxxx`

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| fileId | string | 是 | 导出任务文件标识；由导出接口返回 |

**行为说明**

- 成功：返回文件流（`text/csv`）并触发浏览器下载。  
- `fileId` 不存在或文件已过期：返回 **404**。  
- 无权限访问：返回 **403**。  

### 7.9 错误与行为小结

| 场景 | HTTP | code |
| :--- | :--- | :--- |
| 未认证 / Token 无效 | 401 | 401 |
| 权限不足（无 `log:read` 或 `log:export`） | 403 | 403 |
| 分页参数、时间范围、`type`/`format`/`level` 非法 | 400 | 400 |
| 操作日志详情不存在 | 404 | 404 |
| 下载文件不存在或已过期 | 404 | 404 |
| 导出异常 | 500 / 200 | 5001 |

---

## 8. 系统监控看板

### 8.1 实时概览统计

**GET** `/api/v1/dashboard/statistics`

**Response**

```json
{
  "code": 200,
  "data": {
    "onlineUsers": 128,
    "todayVisits": 4567,
    "todayNewUsers": 89,
    "pendingAuditCount": 5,
    "systemCpuPercent": 42.5,
    "jvmMemoryUsedMB": 512
  }
}
```

### 8.2 访问量趋势

**GET** `/api/v1/dashboard/visits?period=WEEK`

**Response**

```json
{
  "code": 200,
  "data": [
    { "date": "2026-04-30", "visits": 3450 },
    { "date": "2026-05-01", "visits": 4100 }
  ]
}
```

### 8.3 数据增长统计

**GET** `/api/v1/dashboard/data-growth?period=MONTH`

**Response**

```json
{
  "code": 200,
  "data": {
    "relicCount": 2341,
    "knowledgeNodeCount": 15600,
    "ugcCount": 8920,
    "userCount": 4521
  }
}
```

### 8.4 异常告警列表（选做）

**GET** `/api/v1/dashboard/alerts?page=1&pageSize=10`

---

## 9. 系统配置管理（选做）

### 9.1 敏感词库

**GET** `/api/v1/config/sensitive-words?page=1&pageSize=50`

**POST** `/api/v1/config/sensitive-words`

```json
{ "word": "违规词" }
```

**DELETE** `/api/v1/config/sensitive-words/{objectId}`

### 9.2 系统公告

**GET** `/api/v1/config/announcements?status=ACTIVE`

**POST** `/api/v1/config/announcements`

```json
{
  "title": "系统升级通知",
  "content": "5月10日凌晨2点升级维护...",
  "status": "ACTIVE"
}
```

**PUT** `/api/v1/config/announcements/{objectId}`

**DELETE** `/api/v1/config/announcements/{objectId}`

### 9.3 功能开关

**GET** `/api/v1/config/features`

**PUT** `/api/v1/config/features`

```json
{
  "commentEnabled": true,
  "aiAuditEnabled": false,
  "registerEnabled": true
}
```

---

> **文档维护说明**：所有接口修改须同步更新本文档。接口路径统一使用 `/api/v1` 前缀，字段名、错误码严禁随意变更。