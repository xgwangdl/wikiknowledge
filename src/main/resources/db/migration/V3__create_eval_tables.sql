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

COMMENT ON TABLE eval_sets IS '黄金评估集：一组用于评估检索质量的问答题目';
COMMENT ON COLUMN eval_sets.id IS '评估集主键';
COMMENT ON COLUMN eval_sets.name IS '评估集名称';
COMMENT ON COLUMN eval_sets.description IS '评估集描述';
COMMENT ON COLUMN eval_sets.created_at IS '创建时间';

COMMENT ON TABLE eval_questions IS '评估题目表';
COMMENT ON COLUMN eval_questions.id IS '题目主键';
COMMENT ON COLUMN eval_questions.eval_set_id IS '所属评估集 ID';
COMMENT ON COLUMN eval_questions.question IS '评估问题';
COMMENT ON COLUMN eval_questions.expected_answer IS '期望答案，供人工参考';
COMMENT ON COLUMN eval_questions.expected_chunk_ids IS '期望命中的 chunk id，逗号分隔';
COMMENT ON COLUMN eval_questions.created_at IS '创建时间';

COMMENT ON TABLE eval_runs IS '评估运行记录表';
COMMENT ON COLUMN eval_runs.id IS '运行主键';
COMMENT ON COLUMN eval_runs.eval_set_id IS '使用的评估集 ID';
COMMENT ON COLUMN eval_runs.status IS '运行状态：RUNNING/COMPLETED/FAILED';
COMMENT ON COLUMN eval_runs.metrics IS '聚合指标 JSON：Recall、Precision、MRR';
COMMENT ON COLUMN eval_runs.report IS '失败原因或详细报告文本';
COMMENT ON COLUMN eval_runs.created_at IS '创建时间';

COMMENT ON TABLE eval_results IS '每条评估题目的检索结果';
COMMENT ON COLUMN eval_results.id IS '结果主键';
COMMENT ON COLUMN eval_results.eval_run_id IS '所属评估运行 ID';
COMMENT ON COLUMN eval_results.question_id IS '评估题目 ID';
COMMENT ON COLUMN eval_results.retrieved_chunk_ids IS '实际检索到的 chunk id，逗号分隔';
COMMENT ON COLUMN eval_results.recall IS 'Recall@k：命中的期望结果比例';
COMMENT ON COLUMN eval_results.precision IS 'Precision@k：检索结果中命中比例';
COMMENT ON COLUMN eval_results.mrr IS 'MRR：第一个命中结果的倒数排名';
COMMENT ON COLUMN eval_results.created_at IS '创建时间';
