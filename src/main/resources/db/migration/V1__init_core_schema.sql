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
