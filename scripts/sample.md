# 维基知识库联调文档

维基知识库是一个基于 Java 和 Spring AI 的 RAG 问答系统。

它支持文档上传、切片、向量化、检索和流式问答。管理员可以上传 PDF、Word、Markdown 和纯文本文件。

系统使用 PostgreSQL 的 pgvector 存储向量，并使用 Redis 实现限流和刷新令牌管理。

用户登录后可以选择知识库提问，系统会返回带引用来源的回答。
