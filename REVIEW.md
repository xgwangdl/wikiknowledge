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

## 本轮关键技术决策

1. 为什么选 Spring Boot 3.5.16 而不是 4.1.0？
   - Spring Boot 4 太新，生态迁移风险大；3.5 是当前主流稳定线，适合学习与求职。
2. 为什么选 Spring AI 1.1.8？
   - 当前稳定 GA 版本，避免使用 Milestone/SNAPSHOT 版本。
3. 为什么用 DashScope OpenAI 兼容接口，而不是 Spring AI Alibaba starter？
   - Spring AI Alibaba 在 Maven Central 上仍停留在 1.0.0-M6.1 Milestone；用官方 Spring AI + OpenAI 兼容协议更稳定，通义千问同样可用。
4. 为什么用 PostgreSQL + pgvector？
   - 向量检索和业务数据可以放在同一个数据库，降低运维复杂度，是生产环境常见方案。
5. 为什么用 Flyway？
   - 数据库结构像代码一样版本化管理，部署和团队协作更可靠。
6. 为什么先做单体而不是微服务？
   - 业务规模和学习成本都不适合微服务，单体 + 分层足够，也更容易讲清楚。

## 请重点理解的问题

1. Spring Boot 启动流程：`@SpringBootApplication` 里包含了哪些自动配置？
2. `application.yml` 中哪些配置来自环境变量？为什么要这样设计？
3. `V1__init_core_schema.sql` 中为什么用 `BIGSERIAL`、`TIMESTAMPTZ`、`JSONB`、`VECTOR(1536)`？
4. `chunks` 表为什么建 HNSW 索引？`vector_cosine_ops` 是什么意思？
5. `docker-compose.yml` 为什么单独挂载数据卷？
6. 如果本地没有数据库，项目能直接启动吗？为什么？

## 验证结果

- `mvn -DskipTests package` 已通过，可生成可执行 jar。
- 尚未启动数据库，因此本轮未验证运行态；下一步会安装/启动 Docker 后跑通健康检查。

## 下一轮计划

1. 启动 PostgreSQL + Redis，跑通 Flyway 和 `/actuator/health`
2. 引入 Spring Security + JWT 登录注册
3. 再做知识库和文档管理

## 如何 review

1. 通读 `pom.xml`、`application.yml`、`V1__init_core_schema.sql`
2. 自己回答“请重点理解的问题”，不会的用 IDE 点进源码看
3. 把疑问记录到 Issue 或笔记里，我们下一轮开始前先解决
