# 后台管理系统组 - API接口需求文档（修订版）

> 本文档整理自M5模块需增接口、M5设计文档接口部分、前端用户交互部分仍然缺少的接口三个文档，结合后台管理子系统API接口文档(v1.0.7)和数据库设计文档(v1.0)，筛选出后台管理系统组**需要实现但尚未实现**的接口需求。
>
> **说明**：以下标注为"✅ 已实现"的接口表示在其他组的接口文档中已由后台管理系统组实现，标注为"❌ 待实现"的接口表示仍需要后台管理系统组实现。

---

## 一、文物数据相关接口（需前端M2模块使用）

### 1.1 文物筛选选项接口

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取所有可用的筛选选项（年代、类型、材质、博物馆） |
| 请求方式 | GET |
| 接口路径 | `/api/v1/artifacts/filters` |
| 调用场景 | 前端筛选器选项数据来源 |
| 实现状态 | ❌ **待实现** |
| 备注 | 前端M2模块需要此接口获取筛选选项 |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "periods": ["唐", "宋", "元", "明", "清"],
      "types": ["瓷器", "青铜器", "书画", "玉器", "漆器"],
      "materials": ["青花瓷", "粉彩", "青铜", "丝绸", "玉石"],
      "museums": ["大英博物馆", "大都会艺术博物馆", "克利夫兰艺术博物馆"]
   }
}
```

**数据库对应**：
- `periods` - 查询 `artifact` 表的 `DISTINCT period` 字段
- `types` - 查询 `artifact` 表的 `DISTINCT type` 字段
- `materials` - 查询 `artifact` 表的 `DISTINCT material` 字段
- `museums` - 查询 `museum` 表的 `name` 和 `name_cn` 字段

---

### 1.2 文物交互摘要接口

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取文物交互摘要（点赞数、收藏数、评论数等） |
| 请求方式 | GET |
| 接口路径 | `/api/v1/artifacts/{objectId}/interaction-summary` |
| 调用场景 | 文物详情页交互数据展示 |
| 实现状态 | ❌ **待实现** |
| 备注 | 前端M2模块需要此接口展示文物互动数据 |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "artifactId": "artifact_001",
      "likeCount": 156,
      "favoriteCount": 89,
      "commentCount": 23,
      "viewCount": 1234
   }
}
```

**数据库对应**：
- `likeCount` - 需要统计 `ugc_image` 表和 `ugc_comment` 表的点赞数（当前数据库可能需要扩展）
- `favoriteCount` - 需要新建 `user_favorite` 表或扩展现有表
- `commentCount` - 统计 `ugc_comment` 表中对应文物的评论数
- `viewCount` - 需要新建 `artifact_view_log` 表记录浏览

---

## 二、内容审核相关接口（补充）

### 2.1 敏感词库管理（选做功能）

| 项目 | 内容 |
|------|------|
| 接口用途 | 敏感词库管理 |
| 请求方式 | GET/POST/DELETE |
| 接口路径 | `/api/v1/config/sensitive-words` 等 |
| 实现状态 | ✅ **已实现**（见API文档§9.1） |
| 备注 | 后台管理子系统API文档中已有完整实现 |

**已实现接口：**

- `GET /api/v1/config/sensitive-words` - 获取敏感词列表
- `POST /api/v1/config/sensitive-words` - 新增敏感词
- `DELETE /api/v1/config/sensitive-words/{objectId}` - 删除敏感词

**数据库对应表**：`sensitive_word`

---

### 2.2 审核统计（选做功能）

| 项目 | 内容 |
|------|------|
| 接口用途 | 审核数据统计 |
| 请求方式 | GET |
| 接口路径 | `/api/v1/audit/statistics` |
| 实现状态 | ✅ **已实现**（见API文档§4.5） |
| 备注 | 后台管理子系统API文档中已有完整实现 |

**已实现接口：**

- `GET /api/v1/audit/statistics` - 审核统计数据

**数据库对应**：
- 统计 `audit_queue` 表的各状态数量

---

## 三、评论与点赞相关接口（前端用户交互功能）

> 以下接口是前端用户交互模块需要的评论和点赞功能，需要后台管理系统提供数据支持。

### 3.1 获取文物评论列表

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取文物评论列表（分页） |
| 请求方式 | GET |
| 接口路径 | `/api/v1/artifacts/{artifactId}/comments` |
| 调用场景 | 文物详情页评论展示 |
| 实现状态 | ❌ **待实现** |
| 备注 | 支持分页、支持回复层级展示 |
| 优先级 | **高**（前端核心功能） |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| artifactId | string | 是 | 文物ID，拼接在路径中 |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "total": 45,
      "page": 1,
      "size": 20,
      "items": [
         {
            "objectId": "comment_001",
            "userId": "user_123",
            "userName": "张三",
            "userAvatar": "https://xxx/avatar.jpg",
            "content": "这件文物太精美了！",
            "likeCount": 12,
            "replyCount": 3,
            "createTime": "2024-03-15T10:30:00Z",
            "replies": [
               {
                  "objectId": "reply_001",
                  "userId": "user_456",
                  "userName": "李四",
                  "content": "同意！",
                  "createTime": "2024-03-15T11:00:00Z"
               }
            ]
         }
      ]
   }
}
```

**数据库对应**：
- 主表：`ugc_comment`
- 关联用户信息：`user` 表
- 统计点赞数：需要统计 `comment_like` 表（需新建）

---

### 3.2 发布评论

| 项目 | 内容 |
|------|------|
| 接口用途 | 用户发表评论 |
| 请求方式 | POST |
| 接口路径 | `/api/v1/artifacts/{artifactId}/comments` |
| 调用场景 | 文物详情页发表评论 |
| 实现状态 | ❌ **待实现** |
| 备注 | 评论需要进入审核队列 |
| 优先级 | **高**（前端核心功能） |

**请求参数：**

```json
{
   "content": "这件文物太精美了！",
   "parentId": null
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| artifactId | string | 是 | 文物ID，拼接在路径中 |
| content | string | 是 | 评论内容，最大500字符 |
| parentId | string | 否 | 父评论ID（回复时填写） |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "objectId": "comment_001",
      "status": "PENDING",
      "message": "评论已提交，等待审核"
   }
}
```

**实现逻辑**：
1. 评论内容自动进行敏感词检测
2. 通过审核的评论直接发布，否则进入人工审核队列
3. 写入 `ugc_comment` 表，状态为 `PENDING` 或 `APPROVED`

---

### 3.3 回复评论

| 项目 | 内容 |
|------|------|
| 接口用途 | 回复某条评论 |
| 请求方式 | POST |
| 接口路径 | `/api/v1/artifacts/{artifactId}/comments` |
| 调用场景 | 评论区的回复功能 |
| 实现状态 | ❌ **待实现** |
| 备注 | 与发布评论使用同一接口，通过 `parentId` 区分 |
| 优先级 | **高**（前端核心功能） |

**请求参数：**

```json
{
   "content": "我也这么认为！",
   "parentId": "comment_001"
}
```

---

### 3.4 获取我的评论

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取当前用户的评论历史 |
| 请求方式 | GET |
| 接口路径 | `/api/v1/users/{username}/comments` |
| 调用场景 | 个人中心-我的评论 |
| 实现状态 | ❌ **待实现** |
| 备注 | 需要JWT认证，只能查看自己的评论 |
| 优先级 | **中** |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "total": 12,
      "page": 1,
      "size": 20,
      "items": [
         {
            "objectId": "comment_001",
            "artifactId": "artifact_123",
            "artifactTitle": "青花云龙纹象耳瓶",
            "content": "这件文物太精美了！",
            "status": "APPROVED",
            "likeCount": 12,
            "createTime": "2024-03-15T10:30:00Z"
         }
      ]
   }
}
```

---

### 3.5 评论点赞

| 项目 | 内容 |
|------|------|
| 接口用途 | 用户对评论点赞 |
| 请求方式 | POST |
| 接口路径 | `/api/v1/comments/{commentId}/likes` |
| 调用场景 | 评论点赞按钮 |
| 实现状态 | ❌ **待实现** |
| 备注 | 需要防止重复点赞 |
| 优先级 | **中** |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "likeCount": 13,
      "isLiked": true
   }
}
```

**数据库设计建议**：

```sql
-- 需要新建 comment_like 表
CREATE TABLE comment_like (
    object_id VARCHAR(36) PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES ugc_comment(object_id),
    FOREIGN KEY (user_id) REFERENCES user(object_id),
    UNIQUE KEY uk_comment_user (comment_id, user_id)
);
```

---

### 3.6 文物点赞

| 项目 | 内容 |
|------|------|
| 接口用途 | 用户对文物点赞 |
| 请求方式 | POST |
| 接口路径 | `/api/v1/artifacts/{artifactId}/likes` |
| 调用场景 | 文物详情页点赞按钮 |
| 实现状态 | ❌ **待实现** |
| 备注 | 需要防止重复点赞 |
| 优先级 | **中** |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "likeCount": 157,
      "isLiked": true
   }
}
```

---

### 3.7 取消文物点赞

| 项目 | 内容 |
|------|------|
| 接口用途 | 取消文物点赞 |
| 请求方式 | DELETE |
| 接口路径 | `/api/v1/artifacts/{artifactId}/likes` |
| 调用场景 | 文物详情页取消点赞 |
| 实现状态 | ❌ **待实现** |
| 优先级 | **中** |

---

### 3.8 获取我的点赞列表

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取当前用户的点赞历史 |
| 请求方式 | GET |
| 接口路径 | `/api/v1/users/{username}/likes` |
| 调用场景 | 个人中心-我的点赞 |
| 实现状态 | ❌ **待实现** |
| 优先级 | **低** |

**数据库设计建议**：

```sql
-- 需要新建 artifact_like 表
CREATE TABLE artifact_like (
    object_id VARCHAR(36) PRIMARY KEY,
    artifact_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (artifact_id) REFERENCES artifact(object_id),
    FOREIGN KEY (user_id) REFERENCES user(object_id),
    UNIQUE KEY uk_artifact_user (artifact_id, user_id)
);
```

---

## 四、用户数据管理相关接口（选做）

### 4.1 用户收藏数据管理

| 项目 | 内容 |
|------|------|
| 接口用途 | 用户收藏数据查询与管理 |
| 请求方式 | GET |
| 接口路径 | `/api/v1/users/{username}/favorites` |
| 实现状态 | ❌ **待实现** |
| 备注 | 数据库中尚无对应表，需新建 `user_favorite` 表 |
| 优先级 | **低**（选做功能） |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名，拼接在路径中 |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "total": 45,
      "page": 1,
      "size": 20,
      "items": [
         {
            "artifactId": "artifact_001",
            "groupName": "我的收藏",
            "createTime": "2024-03-15T10:30:00Z"
         }
      ]
   }
}
```

**数据库设计建议**：

```sql
-- 需要新建 user_favorite 表
CREATE TABLE user_favorite (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    artifact_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(100) DEFAULT 'default',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(object_id),
    FOREIGN KEY (artifact_id) REFERENCES artifact(object_id)
);

-- 需要新建 user_favorite_group 表（可选）
CREATE TABLE user_favorite_group (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(object_id)
);
```

---

### 4.2 用户浏览历史管理

| 项目 | 内容 |
|------|------|
| 接口用途 | 用户浏览历史数据查询与记录 |
| 请求方式 | GET/POST |
| 接口路径 | `/api/v1/users/{username}/history` |
| 实现状态 | ❌ **待实现** |
| 备注 | 数据库中尚无对应表，需新建 `user_browse_history` 表 |
| 优先级 | **低**（选做功能） |

**数据库设计建议**：

```sql
-- 需要新建 user_browse_history 表
CREATE TABLE user_browse_history (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    artifact_id VARCHAR(36) NOT NULL,
    browse_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(object_id),
    FOREIGN KEY (artifact_id) REFERENCES artifact(object_id)
);
```

---

## 五、前端M2模块文物数据接口需求（供前端使用）

> 以下接口是前端文物浏览模块（M2）需要调用后台管理系统提供的接口，需要后台管理系统实现。

### 5.1 文物列表查询（简化版，供前端调用）

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取文物列表（供前端M2模块调用） |
| 请求方式 | GET |
| 接口路径 | `/api/v1/artifacts` |
| 调用场景 | 前端首页文物列表展示 |
| 实现状态 | ✅ **已实现**（见API文档§5.2） |
| 备注 | 后台管理子系统API文档§5.2已有完整实现 |

**已实现接口**：

- `GET /api/v1/data/relics` - 文物列表（分页、搜索）

**响应字段映射**：

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
            "museumId": "m-550e8400...",
            "imageUrl": "https://cdn.example.org/relics/12345.jpg"
         }
      ],
      "total": 1,
      "page": 1,
      "pageSize": 10
   }
}
```

---

### 5.2 文物详情查询（供前端调用）

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取文物详情（供前端M2模块调用） |
| 请求方式 | GET |
| 接口路径 | `/api/v1/artifacts/{objectId}` |
| 调用场景 | 文物详情页 |
| 实现状态 | ✅ **已实现**（见API文档§5.2） |
| 备注 | 后台管理子系统API文档§5.2已有完整实现 |

---

### 5.3 相关文物推荐（需补充实现）

| 项目 | 内容 |
|------|------|
| 接口用途 | 获取相关文物推荐 |
| 请求方式 | GET |
| 接口路径 | `/api/v1/artifacts/{objectId}/related` |
| 调用场景 | 文物详情页相关推荐区域 |
| 实现状态 | ❌ **待实现** |
| 备注 | 需要基于年代、类型等关联查询推荐文物 |
| 优先级 | **中** |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| objectId | string | 是 | 当前文物唯一标识 |
| count | int | 否 | 推荐数量，默认 6 |

**响应示例：**

```json
{
   "code": 200,
   "message": "success",
   "data": {
      "items": [
         {
            "objectId": "artifact_045",
            "title": "元青花鬼谷子下山图罐",
            "period": "元",
            "type": "瓷器",
            "museum": "大英博物馆",
            "imageUrl": "https://xxx/thumbnail/artifact_045.jpg"
         }
      ]
   }
}
```

**实现建议**：
- 基于当前文物的 `period` 和 `type` 进行相似度推荐
- 可使用Neo4j图数据库进行关联查询
- 限制返回数量，避免性能问题

---

## 六、数据库扩展

> 以下是实现上述待实现接口所需的数据库扩展：

### 6.1 文物浏览记录表（支持统计浏览量）

```sql
-- 需要新建 artifact_view_log 表
CREATE TABLE artifact_view_log (
    object_id VARCHAR(36) PRIMARY KEY,
    artifact_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),  -- 可为空，匿名访问
    view_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (artifact_id) REFERENCES artifact(object_id)
);

-- 创建索引
CREATE INDEX idx_artifact_views ON artifact_view_log(artifact_id, view_time);
```

### 6.2 用户收藏表（支持收藏功能）

```sql
-- 需要新建 user_favorite 表
CREATE TABLE user_favorite (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    artifact_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(100) DEFAULT 'default',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(object_id),
    FOREIGN KEY (artifact_id) REFERENCES artifact(object_id),
    UNIQUE KEY uk_user_artifact (user_id, artifact_id)
);
```

### 6.3 用户浏览历史表（支持历史记录）

```sql
-- 需要新建 user_browse_history 表
CREATE TABLE user_browse_history (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    artifact_id VARCHAR(36) NOT NULL,
    browse_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(object_id),
    FOREIGN KEY (artifact_id) REFERENCES artifact(object_id)
);

-- 创建索引
CREATE INDEX idx_user_browse ON user_browse_history(user_id, browse_time DESC);
```

