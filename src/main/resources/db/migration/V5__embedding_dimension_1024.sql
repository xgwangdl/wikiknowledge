-- DashScope text-embedding-v3 输出 1024 维向量，调整 chunks.embedding 列与索引
DROP INDEX IF EXISTS idx_chunks_embedding;
ALTER TABLE chunks ALTER COLUMN embedding TYPE vector(1024) USING (CASE WHEN embedding IS NULL THEN NULL ELSE embedding::vector(1024) END);
CREATE INDEX idx_chunks_embedding ON chunks USING hnsw (embedding vector_cosine_ops);
