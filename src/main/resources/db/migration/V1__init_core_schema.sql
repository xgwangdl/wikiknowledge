CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(50),
    role          VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE knowledge_bases (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id    BIGINT REFERENCES users (id),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE documents (
    id                BIGSERIAL PRIMARY KEY,
    knowledge_base_id BIGINT      NOT NULL REFERENCES knowledge_bases (id) ON DELETE CASCADE,
    filename          VARCHAR(255) NOT NULL,
    file_hash         VARCHAR(64),
    content_type      VARCHAR(100),
    file_size         BIGINT,
    status            VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    error_message     TEXT,
    chunk_count       INTEGER     NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_knowledge_base_id ON documents (knowledge_base_id);

CREATE TABLE chunks (
    id                BIGSERIAL PRIMARY KEY,
    document_id       BIGINT        NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    knowledge_base_id BIGINT        NOT NULL REFERENCES knowledge_bases (id) ON DELETE CASCADE,
    content           TEXT          NOT NULL,
    meta              JSONB,
    token_count       INTEGER,
    seq_no            INTEGER       NOT NULL,
    embedding         VECTOR(1536),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chunks_document_seq UNIQUE (document_id, seq_no)
);

CREATE INDEX idx_chunks_knowledge_base_id ON chunks (knowledge_base_id);
CREATE INDEX idx_chunks_embedding ON chunks USING hnsw (embedding vector_cosine_ops);

CREATE TABLE sessions (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES users (id),
    knowledge_base_id BIGINT      NOT NULL REFERENCES knowledge_bases (id),
    title             VARCHAR(200),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sessions_user_id ON sessions (user_id);

CREATE TABLE messages (
    id               BIGSERIAL PRIMARY KEY,
    session_id       BIGINT       NOT NULL REFERENCES sessions (id) ON DELETE CASCADE,
    role             VARCHAR(20)  NOT NULL,
    content          TEXT         NOT NULL,
    citations        JSONB,
    tokens_in        INTEGER,
    tokens_out       INTEGER,
    feedback         VARCHAR(20),
    feedback_reason  VARCHAR(500),
    latency_ms       BIGINT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_session_id ON messages (session_id);

CREATE TABLE audit_logs (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT,
    action     VARCHAR(100) NOT NULL,
    target     VARCHAR(200),
    detail     JSONB,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

COMMENT ON TABLE users IS '系统用户表';
COMMENT ON COLUMN users.id IS '用户主键';
COMMENT ON COLUMN users.username IS '登录用户名，唯一';
COMMENT ON COLUMN users.password_hash IS 'BCrypt 加密后的密码';
COMMENT ON COLUMN users.display_name IS '用户显示名称';
COMMENT ON COLUMN users.role IS '角色，ROLE_USER 或 ROLE_ADMIN';
COMMENT ON COLUMN users.status IS '账号状态：ACTIVE 正常，DISABLED 禁用';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '最后更新时间';

COMMENT ON TABLE knowledge_bases IS '知识库表';
COMMENT ON COLUMN knowledge_bases.id IS '知识库主键';
COMMENT ON COLUMN knowledge_bases.name IS '知识库名称';
COMMENT ON COLUMN knowledge_bases.description IS '知识库描述';
COMMENT ON COLUMN knowledge_bases.owner_id IS '创建人用户 ID，管理员可为空';
COMMENT ON COLUMN knowledge_bases.status IS '知识库状态：ACTIVE 正常';
COMMENT ON COLUMN knowledge_bases.created_at IS '创建时间';
COMMENT ON COLUMN knowledge_bases.updated_at IS '最后更新时间';

COMMENT ON TABLE documents IS '上传的文档表';
COMMENT ON COLUMN documents.id IS '文档主键';
COMMENT ON COLUMN documents.knowledge_base_id IS '所属知识库 ID';
COMMENT ON COLUMN documents.filename IS '原始文件名';
COMMENT ON COLUMN documents.file_hash IS '文件 SHA-256，用于同一知识库去重';
COMMENT ON COLUMN documents.content_type IS '文件 MIME 类型';
COMMENT ON COLUMN documents.file_size IS '文件大小（字节）';
COMMENT ON COLUMN documents.status IS '解析状态：UPLOADED/PARSING/INDEXING/READY/FAILED';
COMMENT ON COLUMN documents.error_message IS '解析失败时的错误信息';
COMMENT ON COLUMN documents.chunk_count IS '生成的切片数量';
COMMENT ON COLUMN documents.created_at IS '创建时间';
COMMENT ON COLUMN documents.updated_at IS '最后更新时间';

COMMENT ON TABLE chunks IS '文档切片表，保存切出的文本块与向量';
COMMENT ON COLUMN chunks.id IS '切片主键';
COMMENT ON COLUMN chunks.document_id IS '来源文档 ID';
COMMENT ON COLUMN chunks.knowledge_base_id IS '所属知识库 ID，便于按库过滤检索';
COMMENT ON COLUMN chunks.content IS '切片文本内容';
COMMENT ON COLUMN chunks.meta IS '切片元数据 JSON，如标题、页码等';
COMMENT ON COLUMN chunks.token_count IS '估算的 token 数';
COMMENT ON COLUMN chunks.seq_no IS '切片在文档中的序号';
COMMENT ON COLUMN chunks.embedding IS '文本向量，pgvector 类型，用于相似度检索';
COMMENT ON COLUMN chunks.created_at IS '创建时间';

COMMENT ON TABLE sessions IS '问答会话表';
COMMENT ON COLUMN sessions.id IS '会话主键';
COMMENT ON COLUMN sessions.user_id IS '所属用户 ID';
COMMENT ON COLUMN sessions.knowledge_base_id IS '会话使用的知识库 ID';
COMMENT ON COLUMN sessions.title IS '会话标题';
COMMENT ON COLUMN sessions.created_at IS '创建时间';
COMMENT ON COLUMN sessions.updated_at IS '最后更新时间';

COMMENT ON TABLE messages IS '会话消息表';
COMMENT ON COLUMN messages.id IS '消息主键';
COMMENT ON COLUMN messages.session_id IS '所属会话 ID';
COMMENT ON COLUMN messages.role IS '消息角色：user 或 assistant';
COMMENT ON COLUMN messages.content IS '消息内容';
COMMENT ON COLUMN messages.citations IS 'AI 回答的引用来源，JSON 文本';
COMMENT ON COLUMN messages.tokens_in IS '输入 token 数';
COMMENT ON COLUMN messages.tokens_out IS '输出 token 数';
COMMENT ON COLUMN messages.feedback IS '用户反馈：UP/DOWN';
COMMENT ON COLUMN messages.feedback_reason IS '反馈原因';
COMMENT ON COLUMN messages.latency_ms IS '回答耗时（毫秒）';
COMMENT ON COLUMN messages.created_at IS '创建时间';

COMMENT ON TABLE audit_logs IS '审计日志表';
COMMENT ON COLUMN audit_logs.id IS '日志主键';
COMMENT ON COLUMN audit_logs.user_id IS '操作用户 ID';
COMMENT ON COLUMN audit_logs.action IS '操作类型，如 LOGIN、DELETE';
COMMENT ON COLUMN audit_logs.target IS '操作对象描述';
COMMENT ON COLUMN audit_logs.detail IS '操作详情 JSON';
COMMENT ON COLUMN audit_logs.created_at IS '操作时间';
