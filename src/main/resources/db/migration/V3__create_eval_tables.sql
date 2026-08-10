CREATE TABLE eval_sets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE eval_questions (
    id BIGSERIAL PRIMARY KEY,
    eval_set_id BIGINT NOT NULL REFERENCES eval_sets (id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    expected_answer TEXT,
    expected_chunk_ids TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_eval_questions_eval_set_id ON eval_questions (eval_set_id);

CREATE TABLE eval_runs (
    id BIGSERIAL PRIMARY KEY,
    eval_set_id BIGINT NOT NULL REFERENCES eval_sets (id),
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    metrics TEXT,
    report TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE eval_results (
    id BIGSERIAL PRIMARY KEY,
    eval_run_id BIGINT NOT NULL REFERENCES eval_runs (id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES eval_questions (id),
    retrieved_chunk_ids TEXT,
    recall DOUBLE PRECISION,
    precision DOUBLE PRECISION,
    mrr DOUBLE PRECISION,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_eval_results_eval_run_id ON eval_results (eval_run_id);
