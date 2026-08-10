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

知识库管理：

- 创建、编辑、删除、列表、详情接口
- 普通用户只能管理自己创建的知识库
- 管理员可以管理所有知识库
- 删除知识库依赖数据库外键级联删除文档与切片
- `KnowledgeBaseServiceTest` 单元测试

文档上传与解析：

- 支持 pdf/docx/md/txt，单文件 20MB 限制
- SHA-256 文件指纹，同一知识库内自动去重
- 本地文件存储，上传后异步解析
- Apache Tika 自动识别格式并提取文本
- 按段落 + 500 字符/50 字符重叠进行切片入库
- 文档状态：`UPLOADED -> PARSING -> READY / FAILED`
- `DocumentServiceTest`、`TextChunkerTest`、`TikaTextExtractorTest`

向量化与 RAG：

- 文档解析完成后自动为每个切片生成 embedding
- 切片向量写入 `chunks.embedding`（pgvector HNSW 索引）
- 提问时先向量检索相似切片，再组装 Prompt
- `POST /api/chat` 以 SSE 流式返回回答和引用来源
- 相似度低于阈值时明确提示“未找到相关资料”
- 使用 DashScope OpenAI 兼容接口；未配置 API Key 时应用可启动，但 AI 调用会失败
- `EmbeddingServiceTest`、`RagServiceTest`

会话与消息：

- 会话创建、列表、详情、删除，只能访问自己的会话
- `POST /api/chat` 自动创建会话并持久化用户问题与 AI 回答
- 回答的引用来源以 JSON 文本保存到 `messages.citations`
- 新增 `V2` Flyway 迁移：`citations` 从 JSONB 改为 TEXT，便于 JPA 映射
- `ChatService` 负责聊天编排，`RagService` 保持纯检索与流式生成
- `SessionServiceTest`、`ChatServiceTest`

前端：

- Vue 3 + Element Plus + Vite 单页应用
- 登录/注册、知识库管理、文档上传、SSE 流式聊天
- `axios` 统一携带 JWT，401 自动跳转登录
- `fetch` 读取 SSE 流，实时渲染回答增量
- Vite 开发代理 `/api -> localhost:8080`
- `npm run build` 已通过，dev server 运行在 `http://localhost:5173`

部署与 CI：

- 后端多阶段 Dockerfile：Maven 构建 + JRE 运行
- 前端 Dockerfile：Node 构建 + Nginx 静态托管
- Nginx 将 `/api` 反代到 backend，SSE 关闭 buffering
- `docker-compose.yml` 包含 postgres/redis/backend/frontend 四个服务
- GitHub Actions：后端测试、前端构建、两个 Docker 镜像构建
- `docker compose config` 校验通过

黄金评估集：

- `eval_sets` / `eval_questions` / `eval_runs` / `eval_results` 四张评估表
- 评估集 CRUD，题目包含问题、期望答案、期望命中的 chunk id
- 运行评估时逐题向量检索，计算 Recall@k、Precision@k、MRR
- 评估结果与聚合指标（JSON）落库，接口仅管理员可访问
- `EvalSetServiceTest`、`EvalRunnerTest`

性能与安全加固：

- Redis 全局限流：每用户每分钟 20 次、每天 500 次，可通过环境变量调整
- 限流命中返回 429 `RATE_LIMITED`
- 提问前做提示注入防护和 2000 字符长度限制
- `traceId` 日志链路：请求头透传，控制台日志带 traceId
- `RateLimitServiceTest`、`PromptGuardServiceTest`

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
10. 知识库的 owner 权限为什么要放在 Service 层校验？
    - 控制器只负责参数和响应，权限规则集中在业务层，测试和维护都更清晰。

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

知识库模块补充问题：

11. 为什么删除知识库时没有手动删除文档和切片？
12. `AccessDeniedException` 为什么能统一转成 403 JSON？
13. 如果以后要支持“共享知识库”，现有 owner 模型要改哪里？

文档模块补充问题：

14. 为什么要用 SHA-256 做文件去重，而不是只比较文件名？
15. `@Async` 异步解析时，为什么要把解析逻辑放到独立的 `DocumentParser` 中？
16. 切片为什么要设置 overlap？overlap 太大会有什么问题？
17. 当前文件存在本地磁盘，生产环境应该换成什么方案？

RAG 模块补充问题：

18. 为什么用 `embedding <=> CAST(... AS vector)` 做余弦距离检索？
19. 为什么先过滤相似度阈值再交给大模型？
20. SSE 的 `start/delta/done/error` 事件分别解决了什么问题？
21. 如果文档没有 embedding，为什么不能被检索到？

会话模块补充问题：

22. 为什么 `ChatService` 要在 `doOnComplete` 里保存 assistant 消息？
23. 为什么会话详情只允许本人访问？在哪一层做的校验？
24. `citations` 为什么要改成 TEXT？JSONB 和 TEXT 各有什么取舍？

前端补充问题：

25. 为什么 `streamChat` 用 `fetch` 而不是 `axios`？
26. SSE 的 `start` 事件返回 `sessionId` 后，前端为什么要保存它？
27. 为什么 Vite 要把 `/api` 代理到后端，而不是直接跨域请求？

部署补充问题：

28. 前端 Nginx 为什么必须关闭 `proxy_buffering`？
29. 后端 Dockerfile 为什么要分两个阶段？
30. CI 里为什么先跑测试再打 Docker 镜像？

评估模块补充问题：

31. Recall@k 和 Precision@k 分别衡量检索的什么问题？
32. MRR 在什么情况下会比 Recall 更敏感？
33. 为什么评估接口要限制为 `ROLE_ADMIN`？

安全加固补充问题：

34. Redis 限流的 key 为什么包含“用户名 + IP + 分钟桶”？
35. 提示注入防护为什么放在 ChatService 而不是 Controller？
36. `traceId` 对排查线上问题有什么帮助？

## 验证结果

- `mvn -DskipTests package` 已通过，可生成可执行 jar。
- `docker compose up -d` 已启动 PostgreSQL 16 + Redis 7
- Flyway 已执行 `V1__init_core_schema.sql`，8 张核心表创建成功
- 应用已启动，`http://localhost:8080/actuator/health` 返回 `{"status":"UP"}`
- 应用进程仍在后台运行，方便你直接验证
- `mvn test` 通过：共 36 个测试全部通过（新增会话 4 个 + 聊天编排 2 个）
- `npm install` + `npm run build` 通过
- 前端 dev server：`http://localhost:5173`
- `docker compose config` 校验通过，包含 4 个服务
- `mvn test` 共 39 个测试全部通过（新增评估模块 3 个）
- `mvn test` 共 44 个测试全部通过（新增安全加固 5 个）
- 已补充完整 [README.md](README.md) 与 [INTERVIEW_PREP.md](INTERVIEW_PREP.md)
- 已新增 [scripts/api_smoke_test.ps1](scripts/api_smoke_test.ps1) 全链路联调脚本
- 说明：本会话沙箱没有 Docker Desktop 管理员权限，认证接口的 HTTP 联调需要你在本机启动 Docker 后验证

## 下一轮计划

1. 本机启动 Docker 后做完整联调
2. 配置真实 AI_API_KEY，验证上传文档 -> 向量化 -> 问答全链路
3. 按 INTERVIEW_PREP.md 做面试模拟

## 如何 review

1. 通读 `pom.xml`、`application.yml`、`V1__init_core_schema.sql`
2. 自己回答“请重点理解的问题”，不会的用 IDE 点进源码看
3. 把疑问记录到 Issue 或笔记里，我们下一轮开始前先解决
