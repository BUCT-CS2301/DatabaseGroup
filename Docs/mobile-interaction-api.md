# 掌上博物馆移动端用户交互接口说明

本文档用于说明本次为掌上博物馆移动端补充和完善的后端接口，主要覆盖文物搜索、收藏夹、收藏、隐私设置、个人信息、头像上传、华为一键登录、用户上传照片、点赞、浏览历史、评论及评论点赞等功能。

---

## 一、基础说明

### 1. 接口基础地址

本地测试时：

```text
http://localhost:8080
```

组内联调时：

```text
http://运行后端电脑IP:8080
```

例如：

```text
http://10.4.122.25:8080
```

正式部署后：

```text
http://服务器IP:8080
或
https://正式域名
```

本文档中只写接口路径，不固定写死某台电脑的 IP。

---

### 2. token 说明

除登录、mock-token、华为一键登录等接口外，大部分接口需要 token。

普通 JSON 请求头：

```http
Authorization: Bearer 你的accessToken
Content-Type: application/json
```

上传图片接口请求头：

```http
Authorization: Bearer 你的accessToken
Content-Type: multipart/form-data
```

---

## 二、认证与个人信息接口

### 1. 获取 mock token

```http
GET /api/v1/auth/mock-token
```

用途：本地测试时快速获取 token。

---

### 2. 获取当前用户信息

```http
GET /api/v1/auth/current-user
```

需要 token。

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "objectId": "mock-user-1",
    "username": "test1",
    "nickname": "测试用户",
    "avatar": "http://localhost:8080/uploads/avatars/xxx.png",
    "bio": "掌上博物馆用户",
    "phone": "17205865351",
    "email": "test@example.com",
    "roles": ["MOBILE"],
    "permissions": ["user:read", "user:write"]
  }
}
```

说明：

`roles` 和 `permissions` 会根据当前用户类型和后端权限配置动态返回。前端个人信息页一般重点使用：

```text
objectId
username
nickname
avatar
bio
phone
email
```

---

### 3. 修改个人资料

```http
PUT /api/v1/users/me/profile
```

需要 token。

请求体：

```json
{
  "nickname": "新的昵称",
  "bio": "新的简介",
  "phone": "17205865351",
  "email": "test@example.com"
}
```

说明：

只修改当前登录用户自己的资料，不通过 `username` 修改别人资料。

---

### 4. 修改头像

```http
POST /api/v1/users/me/avatar
```

需要 token。

Content-Type：

```http
multipart/form-data
```

字段：

```text
file = 图片文件
```

支持格式：

```text
jpg
jpeg
png
webp
```

成功返回示例：

```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "avatar": "http://localhost:8080/uploads/avatars/mock-user-1-xxx.png"
  }
}
```

说明：

数据库中保存相对路径，例如：

```text
/uploads/avatars/xxx.png
```

接口返回时根据配置项或请求 Host 拼接完整 URL。

本地联调时，如果需要让 App 或其他电脑访问头像，可以在启动后端前设置：

```powershell
$env:FILE_PUBLIC_BASE_URL="http://10.4.122.25:8080"
```

---

### 5. 修改密码

```http
PUT /api/v1/auth/password
```

需要 token。

请求体：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

成功返回：

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

原密码错误返回：

```json
{
  "code": 400,
  "message": "原密码错误",
  "data": null
}
```

---

### 6. 华为一键登录

```http
POST /api/v1/auth/huawei-login
```

不需要 token。

说明：

当前为课程项目联调版，暂不做华为服务器校验，也暂不校验 `idToken` 签名。正式上线版本后续需要接入华为 Account Kit 服务端校验。

请求体示例：

```json
{
  "openId": "huawei-openid-test-001",
  "unionId": "huawei-unionid-test-001",
  "nickname": "Huawei Test User",
  "avatar": "",
  "email": "huawei-test@example.com"
}
```

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "expiresIn": 7200,
    "accessToken": "xxx",
    "refreshToken": "xxx",
    "user": {
      "objectId": "xxx",
      "username": "huawei_xxxxxxxxxxxxxxxx",
      "nickname": "Huawei Test User",
      "avatar": "",
      "bio": "掌上博物馆用户",
      "phone": "",
      "email": "huawei-test@example.com"
    }
  }
}
```

说明：

新创建的华为登录用户 `user_type` 固定为：

```text
MOBILE
```

---

## 三、文物搜索接口

### 搜索文物

```http
GET /api/v1/artifacts/search?q=瓷器&page=1&size=20
```

需要 token。

参数说明：

```text
q      搜索关键字
page   页码，默认 1
size   每页数量，默认 20
```

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 1,
    "page": 1,
    "size": 20,
    "items": [
      {
        "objectId": "artifact-test-001",
        "title": "测试文物",
        "period": "明代",
        "type": "瓷器",
        "material": "陶瓷",
        "museum": "测试博物馆",
        "imageUrl": "",
        "popularity": 0
      }
    ]
  }
}
```

空 `q` 参数应返回 400。

---

## 四、收藏夹与收藏接口

### 1. 获取收藏夹列表

```http
GET /api/v1/users/{username}/favorite-groups
```

需要 token。

---

### 2. 新建收藏夹

```http
POST /api/v1/users/{username}/favorite-groups
```

需要 token。

请求体：

```json
{
  "groupName": "my-ceramics"
}
```

---

### 3. 删除收藏夹

```http
DELETE /api/v1/users/{username}/favorite-groups/{groupName}
```

需要 token。

说明：

默认收藏夹 `default` 不允许删除。

---

### 4. 收藏夹统计

```http
GET /api/v1/users/{username}/favorite-groups/summary
```

需要 token。

---

### 5. 收藏文物

```http
POST /api/v1/users/{username}/favorites
```

需要 token。

请求体：

```json
{
  "artifactId": "artifact-test-001",
  "groupName": "my-ceramics"
}
```

说明：

如果 `groupName` 不传，默认收藏到 `default`。

---

### 6. 修改收藏分组

```http
PUT /api/v1/users/{username}/favorites/{artifactId}/group
```

需要 token。

请求体：

```json
{
  "groupName": "default"
}
```

---

### 7. 取消收藏

```http
DELETE /api/v1/users/{username}/favorites/{artifactId}
```

需要 token。

---

### 8. 我的收藏列表

```http
GET /api/v1/users/{username}/favorites?page=1&size=20
```

需要 token。

说明：

最低要求返回：

```text
artifactId
groupName
createTime
```

如果“我的收藏”页面需要直接展示收藏卡片，可以使用增强字段：

```text
title
imageUrl
period
museum
```

如果“我的收藏”页面只负责拿 `artifactId` 跳转文物详情页，则 `title / imageUrl / period / museum` 不是该接口的强制字段。

---

## 五、隐私设置接口

### 1. 获取隐私设置

```http
GET /api/v1/users/{username}/privacy
```

需要 token。

说明：

允许登录用户查看他人的隐私 visible 配置，用于他人主页展示判断。

成功返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "favoritesVisible": true,
    "likesVisible": true,
    "commentsVisible": true,
    "uploadsVisible": true
  }
}
```

---

### 2. 修改隐私设置

```http
PUT /api/v1/users/{username}/privacy
```

需要 token。

说明：

只能修改当前登录用户自己的隐私设置。token 用户和 path 中 `username` 不一致时返回 403。

请求体：

```json
{
  "favoritesVisible": true,
  "likesVisible": false,
  "commentsVisible": true,
  "uploadsVisible": true
}
```

成功返回更新后的完整隐私配置。

---

## 六、用户上传照片接口

### 1. 上传文物照片

```http
POST /api/v1/artifacts/{artifactId}/uploads
```

需要 token。

Content-Type：

```http
multipart/form-data
```

字段：

```text
file = 图片文件
```

成功返回示例：

```json
{
  "code": 200,
  "message": "上传成功，等待审核",
  "data": {
    "uploadId": "upload-001",
    "artifactId": "artifact-test-001",
    "title": "测试文物",
    "imageUrl": "http://localhost:8080/uploads/user-photos/xxx.png",
    "period": "明代",
    "museum": "测试博物馆",
    "status": "PENDING",
    "createTime": "2026-06-02T12:00:00",
    "reviewTime": null,
    "reviewComment": null
  }
}
```

说明：

上传后默认审核状态为：

```text
PENDING
```

---

### 2. 我的上传列表

```http
GET /api/v1/users/{username}/uploads?page=1&size=20
```

需要 token。

说明：

只能查看自己的上传记录。

返回字段：

```text
uploadId
artifactId
title
imageUrl
period
museum
status
createTime
reviewTime
reviewComment
```

---

### 3. 上传详情 / 审核状态

```http
GET /api/v1/users/{username}/uploads/{uploadId}
```

需要 token。

说明：

用于查看单条上传记录和审核状态。

---

## 七、文物点赞接口

### 1. 点赞文物

```http
POST /api/v1/artifacts/{artifactId}/likes
```

需要 token。

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "likeCount": 1,
    "isLiked": true
  }
}
```

---

### 2. 取消点赞文物

```http
DELETE /api/v1/artifacts/{artifactId}/likes
```

需要 token。

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "likeCount": 0,
    "isLiked": false
  }
}
```

---

### 3. 我的点赞列表

```http
GET /api/v1/users/{username}/likes?page=1&size=20
```

需要 token。

返回字段：

```text
artifactId
title
artifactTitle
imageUrl
period
museum
likedAt
createTime
```

---

## 八、浏览历史接口

### 1. 记录浏览历史

```http
POST /api/v1/users/{username}/history
```

需要 token。

请求体：

```json
{
  "artifactId": "artifact-test-001"
}
```

规则：

```text
同一用户浏览同一文物会自动去重，只保留最近一次浏览记录。
```

---

### 2. 我的浏览历史

```http
GET /api/v1/users/{username}/history?page=1&size=20
```

需要 token。

返回字段：

```text
objectId
artifactId
title
artifactTitle
imageUrl
period
museum
browseTime
```

返回排序：

```text
按 browseTime 倒序。
```

---

## 九、评论与评论点赞接口

### 1. 发布评论

```http
POST /api/v1/artifacts/{artifactId}/comments
```

需要 token。

---

### 2. 获取文物评论列表

```http
GET /api/v1/artifacts/{artifactId}/comments
```

需要 token。

---

### 3. 我的评论列表

```http
GET /api/v1/users/{username}/comments
```

需要 token。

---

### 4. 评论点赞

```http
POST /api/v1/comments/{commentId}/likes
```

需要 token。

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "likeCount": 1,
    "isLiked": true
  }
}
```

---

## 十、数据库初始化说明

本次补充了 `mobile_extra_tables.sql`，包括：

```text
user_favorite_group
user_privacy_setting
user_favorite
user_artifact_upload
artifact_like
user_browse_history
comment_like
```

同时补充字段：

```text
artifact.image_url
artifact.image_path
user.bio
user_artifact_upload.update_time
user_artifact_upload.is_deleted
user_browse_history.browse_time
```

并统一移动端交互相关表 collation，避免 JOIN 时出现：

```text
Illegal mix of collations
```

干净数据库测试时建议依次导入：

```sql
SOURCE F:/data/docker/mysql/init.sql;
SOURCE F:/data/docker/mysql/artifact_image.sql;
SOURCE F:/data/docker/mysql/interaction_tables.sql;
SOURCE F:/data/docker/mysql/mobile_extra_tables.sql;
```

说明：

如果本地项目路径不是 `F:/data`，请替换为自己的实际路径。

---

## 十一、联动前提说明

用户交互接口需要传数据库真实 `objectId`。

如果前端仍使用本地 `mockArtifacts` 中的模拟 id，例如：

```text
1
2
3
```

则收藏、点赞、评论、浏览历史、用户上传等交互接口无法稳定关联真实文物记录。

因此完整联调时需要保证：

```text
1. 文物详情页使用真实后端文物数据；
2. 交互请求统一传真实 artifact objectId。
```

这不是后端新增接口问题，而是前端联调数据源问题。

---

## 十二、本次暂不实现的接口

暂不实现：

```http
GET /api/v1/artifacts/{objectId}/interaction-summary
```

原因：

当前前端已不强依赖详情页交互统计摘要展示，因此不作为本次后端补接口项。
