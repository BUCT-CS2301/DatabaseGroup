# 后台管理子系统

> **注意：** 本仓库采用“文档与代码分离”的分支策略：  
> - 所有项目文档（计划、设计、周报等）提交到 `main` 分支  
> - 所有前后端代码、Docker 配置等提交到 `dev` 分支或由此创建的功能分支  

---

## 前置要求
- Docker Desktop（并配置国内镜像加速器）
- Git
- JDK 17
- Node.js 18 

## 快速开始
1.克隆项目  



```bash
git clone <仓库地址>
cd admin-platform/docker
```



2.启动基础服务（MySQL、Redis、Neo4j、MinIO）



```bash
docker compose -f docker-compose.dev.yml up -d
```



3.验证服务

- MySQL: 用 Navicat 连接 `localhost:3306`，root / root123(或改成自己的)
- Redis: 执行 `docker exec -it dev-redis redis-cli ping`，应返回 PONG
- Neo4j: 浏览器访问 [http://localhost:7474](http://localhost:7474/)，neo4j / password123
- MinIO: 浏览器访问 [http://localhost:9001](http://localhost:9001/)，minioadmin / minioadmin123

4.后端

- 在 IDEA 中打开 `admin-server`
- 确保 `application-dev.yml` 中数据库地址正确（如端口非 3306 则对应修改）

5.前端



```bash
cd admin-web
npm install
npm run dev
```



------

## 组员日常开发操作手册

### 一、分支管理规范

- **`main` 分支**：存放项目文档，由组长维护，**组员不要直接推送**。
- **`dev` 分支**：集成开发分支，所有新功能的代码最终都合并到这里。
- **功能分支**：`feature/<模块名>`，例如 `feature/user-management`。组员在 dev 分支的基础上创建自己的功能分支进行开发。

### 二、每日开发流程（从克隆仓库到提交代码）

1. **克隆仓库并进入项目**

   

   ```bash
   git clone <仓库地址>
   cd admin-platform
   ```

   

2. **切换到 dev 分支并拉取最新代码**

   

   ```bash
   git checkout dev
   git pull origin dev
   ```

   

3. **启动 Docker 开发环境**

   

   ```bash
   cd docker
   docker compose -f docker-compose.dev.yml up -d
   ```

   

   如果数据库结构有更新，组员需执行销毁重建（**注意：会清空当前数据**）：

   

   ```bash
   docker compose -f docker-compose.dev.yml down -v
   docker compose -f docker-compose.dev.yml up -d
   ```

   

4. **基于 dev 创建自己的功能分支**

   

   ```bash
   git checkout -b feature/<你的模块名>
   ```

   

   例如负责数据管理模块：

   

   ```bash
   git checkout -b feature/data-management
   ```

   

5. **编写代码并提交**
   在对应子目录（admin-server 或 admin-web）内写代码。完成一个小的功能点后：

   

   ```bash
   git add .
   git commit -m "feat(data): 添加文物批量导入功能"
   ```

   

   **提交信息约定**：建议采用 `type(scope): 简短描述`，例如 `feat(audit): 实现敏感词自动过滤`、`fix(backup): 修复定时任务 cron 表达式错误`。

6. **定期将本地分支推送到 GitHub**

   

   ```bash
   git push -u origin feature/<你的模块名>
   ```

   

   之后只需 `git push` 即可。

### 三、如何发起 Pull Request (PR) 给组长审核

1. **推送功能分支后，浏览器打开 GitHub 仓库页面**。
2. 通常页面顶部会自动出现一个黄色提示框 **“feature/xxx had recent pushes”**，点击其中的 **“Compare & pull request”**。
3. 如果没有提示，则手动进入 **“Pull requests”** 标签，点击绿色 **“New pull request”** 按钮。
4. **配置 PR 合并方向**（非常重要）：
   - **base** 选择 `dev` 分支（目标分支，代码最终要进入 dev）
   - **compare** 自动是你的功能分支，无需改动
5. 填写标题和描述：
   - **标题**：一句话概括做了什么，例如 “用户管理模块：用户列表与状态切换”
   - **描述**：可以列出改动点、测试情况、需要重点关注的地方等。
6. 在右侧面板 **Reviewers** 中选择 **组长（王海鑫）** 作为审核人。
7. 点击 **“Create pull request”** 完成创建。

### 四、审核过程中的修改

- 组长可能会在 PR 中提出修改意见（行内评论）。

- 你在本地功能分支上继续修改代码，然后提交并推送：

  

  ```bash
  git add .
  git commit -m "fix: 根据审核意见调整..."
  git push
  ```

  

- PR 会自动更新，无需重新创建。

### 五、PR 合并后（组长操作）

组长审核通过并合并 PR 后，你可以删除本地和远程的功能分支：



```bash
git checkout dev
git pull origin dev                # 拉取包含你代码的最新 dev
git branch -d feature/你的分支名   # 删除本地分支
git push origin --delete feature/你的分支名  # 删除远程分支（可选）
```



然后基于最新的 dev 创建下一个功能分支继续开发。

------

## 日常环境维护

- 每天早上启动基础服务：`docker compose -f docker-compose.dev.yml up -d`
- 晚上可停止服务：`docker compose -f docker-compose.dev.yml down`
- 数据库结构变更后，通知全组执行 `docker compose down -v` 后重新 `up -d`

------

## 端口说明

- MySQL: 3306（若本地已有 MySQL 占用，可改为 3307:3306 映射，并相应修改后端配置）
- Redis: 6379
- Neo4j: 7474（HTTP） / 7687（Bolt）
- MinIO: 9000（API） / 9001（控制台）
- 后端: 8080
- 前端开发服务器: 5173

------

## 小组概况

| 姓名   | 角色              | 核心模块            |
| :----- | :---------------- | :------------------ |
| 王海鑫 | 文档撰写+项目管理 |                     |
| 申由田 | 汇报+异常处理     |                     |
| 陈翔钰 | 前端开发          | 讲解审核+用户管理   |
| 杨文康 | 后端开发          | 讲解审核+用户管理   |
| 陈柯珉 | 前端开发          | 数据管理+备份与恢复 |
| 李一川 | 后端开发          | 数据管理+备份与恢复 |

------

## 小组周报

周报具体可查看【WeekReport】文件夹，按照周数进行编号。

## 小组文档

### 项目管理计划

Docs/项目管理计划.md
