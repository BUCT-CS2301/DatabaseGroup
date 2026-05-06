# 后台管理子系统 API 接口文档 (v1.0)

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

------

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

------

## 3. 角色与权限管理

### 3.1 获取角色列表（分页）

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



### 3.2 创建角色

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



### 3.3 更新角色

**PUT** `/api/v1/roles/{objectId}`

**Request Body** 同创建（不含 `roleCode` 即可）。

### 3.4 删除角色

**DELETE** `/api/v1/roles/{objectId}`
*注：系统内置角色不可删除，返回错误码 2002。*

### 3.5 获取角色权限树

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



### 3.6 设置角色权限

**PUT** `/api/v1/roles/{objectId}/permissions`

**Request Body**



```json
{
  "permissionIds": ["p-001", "p-003"]
}
```



### 3.7 获取所有权限树

**GET** `/api/v1/permissions`

------

## 4. 用户管理

### 4.1 获取用户列表

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



### 4.2 创建用户

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



### 4.3 更新用户

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



### 4.4 删除用户

**DELETE** `/api/v1/users/{objectId}`
*逻辑删除，用户状态变为 `DISABLED`。*

### 4.5 更新用户状态

**PUT** `/api/v1/users/{objectId}/status`

**Request Body**



```json
{
  "status": "DISABLED"
}
```



`status` 枚举：`ENABLED`、`DISABLED`。

### 4.6 查看用户行为记录

**GET** `/api/v1/users/{objectId}/logs?page=1&pageSize=20`

### 4.7 用户违规处理（选做）

**POST** `/api/v1/users/{objectId}/violations`

**Request Body**



```json
{
  "type": "WARNING",
  "reason": "多次发布违规评论",
  "punishment": "MUTE_3D"
}
```



------

## 5. 内容审核

### 5.1 获取审核规则配置

**GET** `/api/v1/audit/rules`

### 5.2 更新审核规则

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



### 5.3 待审核队列（分页）

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



### 5.4 获取审核详情

**GET** `/api/v1/audit/queue/{objectId}`

### 5.5 人工审核通过

**POST** `/api/v1/audit/queue/{objectId}/approve`

**Request Body (可选)**



```json
{
  "remark": "内容合规"
}
```



### 5.6 人工审核拒绝

**POST** `/api/v1/audit/queue/{objectId}/reject`

**Request Body**



```json
{
  "reason": "包含违规内容",
  "remark": "已屏蔽"
}
```



### 5.7 批量通过

**POST** `/api/v1/audit/queue/batch-approve`

**Request Body**



```json
{
  "objectIds": ["ugc-001", "ugc-002"],
  "remark": "批量操作"
}
```



### 5.8 批量拒绝

**POST** `/api/v1/audit/queue/batch-reject`

**Request Body**



```json
{
  "objectIds": ["ugc-003"],
  "reason": "广告骚扰"
}
```



### 5.9 审核统计

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



------

## 6. 数据管理

### 6.1 文物数据 CRUD

**GET** `/api/v1/data/relics?page=1&pageSize=10&keyword=青铜&categoryId=xxx`

**POST** `/api/v1/data/relics`

**PUT** `/api/v1/data/relics/{objectId}`

**DELETE** `/api/v1/data/relics/{objectId}`
*请求/响应示例略，均为统一分页或单对象结构。*

### 6.2 知识图谱数据 CRUD

**GET** `/api/v1/data/knowledge-graph?page=1&pageSize=10`

**POST** `/api/v1/data/knowledge-graph`

**PUT** `/api/v1/data/knowledge-graph/{objectId}`

**DELETE** `/api/v1/data/knowledge-graph/{objectId}`

### 6.3 用户生成内容（UGC）管理

**GET** `/api/v1/data/ugc?page=1&pageSize=10&status=ALL&userId=xxx`

**GET** `/api/v1/data/ugc/{objectId}`

**DELETE** `/api/v1/data/ugc/{objectId}`
*仅支持删除，不可修改用户内容。*

### 6.4 数据一致性检查（选做）

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



------

## 7. 数据备份与恢复

### 7.1 手动触发备份

**POST** `/api/v1/backup/manual`

**Request Body**



```json
{
  "backupType": "FULL",
  "description": "2026年5月6日手动全量备份"
}
```



### 7.2 定时备份任务管理

**GET** `/api/v1/backup/schedules`

**POST** `/api/v1/backup/schedules`



```json
{
  "cronExpression": "0 0 2 * * ?",
  "backupType": "INCREMENTAL",
  "enabled": true,
  "description": "每日凌晨2点增量备份"
}
```



**PUT** `/api/v1/backup/schedules/{objectId}`

**DELETE** `/api/v1/backup/schedules/{objectId}`

### 7.3 备份记录查询

**GET** `/api/v1/backup/records?page=1&pageSize=10&status=SUCCESS`

**GET** `/api/v1/backup/records/{objectId}` (下载链接等详情)

**DELETE** `/api/v1/backup/records/{objectId}` (删除备份文件)

### 7.4 从备份恢复

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



### 7.5 备份存储信息

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



------

## 8. 日志管理

### 8.1 操作日志

**GET** `/api/v1/logs/operation?page=1&pageSize=20&userId=xxx&module=USER&startTime=2026-05-01&endTime=2026-05-06`

**GET** `/api/v1/logs/operation/{objectId}`

### 8.2 系统日志

**GET** `/api/v1/logs/system?page=1&pageSize=20&level=ERROR`

### 8.3 安全日志

**GET** `/api/v1/logs/security?page=1&pageSize=20`

### 8.4 日志导出

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



**Response**



```json
{
  "code": 200,
  "data": {
    "downloadUrl": "https://oss.xxx.com/exports/log_20260506.csv",
    "expireTime": "2026-05-07T12:00:00"
  }
}
```



------

## 9. 系统监控看板

### 9.1 实时概览统计

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



### 9.2 访问量趋势

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



### 9.3 数据增长统计

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



### 9.4 异常告警列表（选做）

**GET** `/api/v1/dashboard/alerts?page=1&pageSize=10`

------

## 10. 系统配置管理（选做）

### 10.1 敏感词库

**GET** `/api/v1/config/sensitive-words?page=1&pageSize=50`

**POST** `/api/v1/config/sensitive-words`



```json
{ "word": "违规词" }
```



**DELETE** `/api/v1/config/sensitive-words/{objectId}`

### 10.2 系统公告

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

### 10.3 功能开关

**GET** `/api/v1/config/features`

**PUT** `/api/v1/config/features`



```json
{
  "commentEnabled": true,
  "aiAuditEnabled": false,
  "registerEnabled": true
}
```



------

> **文档维护说明**：所有接口修改须同步更新本文档。接口路径统一使用 `/api/v1` 前缀，字段名、错误码严禁随意变更。