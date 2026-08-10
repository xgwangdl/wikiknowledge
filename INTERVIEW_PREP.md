# 面试准备材料

本文档把 wikiknowledge 整理成一份可以“讲得清楚、答得上追问”的面试素材。

## 一句话介绍

> 我独立完成了一个基于 Java 21 + Spring Boot + Spring AI 的 RAG 知识库问答系统，支持文档上传解析、向量检索、SSE 流式回答、会话历史、限流、评估和 Docker 部署。

## 30 秒介绍

> 我做的项目叫维基知识库，是一个 AI 知识库问答系统。管理员上传 PDF、Word 等文档后，系统用 Tika 提取文本、切片并生成向量，用户提问时先在 pgvector 里做相似度检索，再把检索结果作为上下文交给大模型，通过 SSE 流式返回回答和引用来源。项目还包含 JWT 登录、Redis 限流、测试、CI 和 Docker 部署。

## 3 分钟详细讲解

1. 业务场景：企业内部文档多、人工查找慢，需要一个“问文档”的系统。
2. 核心链路：上传文档 -> 解析 -> 切片 -> embedding -> pgvector -> 检索 -> Prompt 组装 -> 大模型流式回答。
3. 关键技术点：
   - Tika 统一解析多种文档格式
   - 切片采用段落优先 + 500 字符/50 重叠
   - pgvector HNSW 余弦索引
   - SSE 流式输出，前端逐字渲染
   - 引用来源从命中的 chunk 生成
4. 工程化：
   - JWT 无状态认证 + Refresh Token 轮换
   - Redis 限流与会话信息
   - Flyway 管理数据库版本
   - 44 个单元测试 + 前端构建
   - Docker Compose 四个服务 + GitHub Actions CI
   - 黄金评估集计算 Recall/Precision/MRR

## 简历项目描述模板

```text
项目名称：维基知识库（wikiknowledge）
项目角色：独立开发
技术栈：Java 21、Spring Boot 3.5、Spring AI、PostgreSQL + pgvector、
        Redis、Vue 3、Docker、GitHub Actions

项目内容：
- 实现文档上传、Tika 文本解析、切片与向量化，支持 PDF/DOCX/MD/TXT
- 基于 pgvector 实现 RAG 检索，SSE 流式问答并返回引用来源
- 实现 JWT 登录、Refresh Token 轮换、RBAC 权限控制
- 实现 Redis 限流、提示注入防护、traceId 日志链路
- 建立黄金评估集，计算 Recall@k、Precision@k、MRR
- Docker Compose 一键部署，CI 覆盖后端测试与前端构建
```

## 高频追问与参考回答

### 1. 为什么用 RAG，而不是直接让大模型回答？

直接回答会编造内容，也无法引用企业内部资料。RAG 先检索真实文档片段，再让模型基于片段回答，既能控制答案来源，也方便更新知识。

### 2. 切片策略怎么设计的？

先按段落切，段落过长再按 500 字符切，重叠 50 字符。这样既保留语义完整性，又避免检索时把无关内容混进同一块。

### 3. 为什么选 pgvector？

业务数据本来就在 PostgreSQL，向量也放同一个库可以少维护一套组件，HNSW 索引能满足中小规模检索性能要求。

### 4. 检索效果怎么评估？

用黄金评估集：每个问题预先标注应该命中的 chunk id，运行后计算 Recall@k、Precision@k、MRR。项目里有管理员评估接口和报告。

### 5. 为什么用 SSE 流式输出？

大模型回答耗时长，SSE 可以让前端边生成边显示，首字延迟更低，体验更好。Nginx 反代时关闭了 buffering。

### 6. JWT 和 Refresh Token 怎么设计的？

Access Token 短时效用于接口鉴权，Refresh Token 存 Redis 并支持轮换和登出吊销，服务端可以主动让旧令牌失效。

### 7. Redis 限流怎么做的？

以“用户名 + IP + 分钟桶/日期”作为 key 自增计数，配合 TTL 实现每分钟和每天两个维度的限制，超过阈值返回 429。

### 8. 提示注入怎么防护？

在进入 RAG 之前校验输入长度和危险指令模式，防止用户绕过 System Prompt；同时用“仅根据资料回答”约束模型行为。

### 9. 测试覆盖了什么？

认证、知识库权限、文档校验、切片、Tika 解析、RAG 事件、会话归属、评估指标、限流和提示防护等核心业务逻辑。

### 10. 生产部署要注意什么？

换真实 API Key、密钥放环境变量或密钥管理服务、文件存储从本地换成 OSS/MinIO、配置 HTTPS、日志脱敏、监控告警。

## 讲解注意事项

- 不要只背概念，讲清楚“你的代码里在哪一层做了什么”
- 被问“这是你写的吗”可以诚实说：项目由我主导，AI 辅助完成部分编码
- 面试官更看重你能否讲清设计原因和踩过的坑
- 准备一个“如果重做会怎么改进”的答案

## 上线前 TODO

- [ ] 配置真实 DashScope API Key
- [ ] JWT Secret 使用随机强密钥
- [ ] 文件存储迁移到 OSS/MinIO
- [ ] 配置 HTTPS 与域名
- [ ] 日志与指标接入 Prometheus/Grafana
- [ ] 补充集成测试（Testcontainers）
