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
- [ ] 登录认证
- [ ] 知识库管理
- [ ] 文档解析与 RAG
- [ ] 管理后台
- [ ] 测试与部署
