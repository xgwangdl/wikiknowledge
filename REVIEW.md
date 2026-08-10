# Review 指引

每轮开发完成后，请按本文件学习代码并 review 关键决策，然后我们再进入下一步。

## 本轮交付

- 项目命名：维基知识库（wikiknowledge）
- 工程路径：`C:\workspace\wikiknowledge`
- 已创建：
  - `pom.xml`：Maven 工程与依赖
  - `src/main/java/com/wikiknowledge/WikiknowledgeApplication.java`：启动类
  - `src/main/resources/application.yml`：基础配置
  - `src/main/resources/db/migration/V1__init_core_schema.sql`：核心表结构
  - `docker-compose.yml`：PostgreSQL + Redis
  - `.env.example`：环境变量模板
  - `REQUIREMENTS.md`：需求文档

本轮新增：

- Spring Security + JWT 登录认证
- 注册、登录、刷新令牌、登出、当前用户接口
- Redis 存储 refresh token，实现刷新轮换
- BCrypt 密码加密
- 管理员种子账号
- 统一 401/403/400 JSON 错误响应
- `JwtServiceTest` 与 `AuthServiceTest` 单元测试

## 本轮关键技术决策

1. 为什么选 Spring Boot 3.5.16 而不是 4.1.0？
   - Spring Boot 4 太新，生态迁移风险大；3.5 是当前主流稳定线，适合学习与求职。
2. 为什么选 Spring AI 1.1.8？
   - 当前稳定 GA 版本，避免使用 Milestone/SNAPSHOT 版本。
3. 为什么用 DashScope OpenAI 兼容接口，而不是 Spring AI Alibaba starter？
   - Spring AI Alibaba 在 Maven Central 上仍停留在 1.0.0-M6.1 Milestone；用官方 Spring AI + OpenAI 兼容协议更稳定，通义千问同样可用。
4. 为什么本轮还没有引入 Spring AI starter？
   - Spring AI 的 OpenAI starter 在没有 API Key 时连应用都无法启动；为了让骨架先跑通数据库和健康检查，AI 依赖会在 RAG 模块阶段再引入。
5. 为什么用 PostgreSQL + pgvector？
   - 向量检索和业务数据可以放在同一个数据库，降低运维复杂度，是生产环境常见方案。
6. 为什么用 Flyway？
   - 数据库结构像代码一样版本化管理，部署和团队协作更可靠。
7. 为什么先做单体而不是微服务？
   - 业务规模和学习成本都不适合微服务，单体 + 分层足够，也更容易讲清楚。
8. 为什么 refresh token 要存 Redis 而不是无状态？
   - 刷新令牌需要支持登出失效和轮换，存 Redis 可以在服务端主动吊销，比纯无状态 JWT 更安全。
9. 为什么密码用 BCrypt 而不是 MD5/SHA？
   - BCrypt 自带盐和慢哈希，能抵抗彩虹表和暴力破解。

## 请重点理解的问题

1. Spring Boot 启动流程：`@SpringBootApplication` 里包含了哪些自动配置？
2. `application.yml` 中哪些配置来自环境变量？为什么要这样设计？
3. `V1__init_core_schema.sql` 中为什么用 `BIGSERIAL`、`TIMESTAMPTZ`、`JSONB`、`VECTOR(1536)`？
4. `chunks` 表为什么建 HNSW 索引？`vector_cosine_ops` 是什么意思？
5. `docker-compose.yml` 为什么单独挂载数据卷？
6. 如果本地没有数据库，项目能直接启动吗？为什么？

认证模块补充问题：

7. JWT 过滤器为什么放在 `UsernamePasswordAuthenticationFilter` 之前？
8. Access Token 和 Refresh Token 的过期时间为什么不一样？
9. 为什么 `logout` 只删除 Redis 中的 refresh token，而不处理 access token？
10. 如果 JWT 密钥泄漏，会有什么风险？生产环境应该怎么配置？

## 验证结果

- `mvn -DskipTests package` 已通过，可生成可执行 jar。
- `docker compose up -d` 已启动 PostgreSQL 16 + Redis 7
- Flyway 已执行 `V1__init_core_schema.sql`，8 张核心表创建成功
- 应用已启动，`http://localhost:8080/actuator/health` 返回 `{"status":"UP"}`
- 应用进程仍在后台运行，方便你直接验证
- `mvn test` 通过：`AuthServiceTest` 6 个用例 + `JwtServiceTest` 2 个用例，共 8 个测试全部通过
- 说明：本会话沙箱没有 Docker Desktop 管理员权限，认证接口的 HTTP 联调需要你在本机启动 Docker 后验证

## 下一轮计划

1. 知识库管理：创建/编辑/删除/列表
2. 文档上传与解析
3. 再实现文档解析与 RAG

## 如何 review

1. 通读 `pom.xml`、`application.yml`、`V1__init_core_schema.sql`
2. 自己回答“请重点理解的问题”，不会的用 IDE 点进源码看
3. 把疑问记录到 Issue 或笔记里，我们下一轮开始前先解决
