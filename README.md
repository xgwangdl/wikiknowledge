# 维基知识库（wikiknowledge）

基于 Java 21 + Spring Boot + Spring AI 的生产级 RAG 知识库问答系统。

本项目按“实战项目”标准开发，面向求职场景：功能完整、有测试、可部署、可评估。

## 文档

- [REQUIREMENTS.md](REQUIREMENTS.md)：需求文档
- [REVIEW.md](REVIEW.md)：每轮交付后的学习与 review 指引

## 技术栈

- Java 21
- Spring Boot
- Spring AI
- PostgreSQL + pgvector
- Redis
- Flyway
- Vue 3 + Element Plus + Vite
- Docker Compose

## 目录结构

```text
wikiknowledge/
├── docs/
├── src/
├── pom.xml
└── README.md
```

## 开发状态

- [x] 项目初始化
- [x] 工程骨架（Maven + Spring Boot + 基础配置）
- [x] 本地 PostgreSQL/Redis + Flyway + 健康检查
- [x] 登录认证（Spring Security + JWT + 刷新令牌）
- [x] 知识库管理（创建/编辑/删除/列表 + 权限控制）
- [x] 文档上传与解析（Tika 文本提取 + 切片入库）
- [x] 向量化与 RAG 问答（pgvector 检索 + SSE 流式回答 + 引用来源）
- [x] 会话历史与消息持久化
- [x] 管理后台与前端（Vue 3 + Element Plus）
- [x] Docker Compose 部署与 GitHub Actions CI
- [x] 黄金评估集与评估报告
- [x] 性能与安全加固（Redis 限流、提示注入防护、traceId 日志）
- [ ] README 完善与面试准备材料
- [ ] 管理后台
- [ ] 测试与部署
