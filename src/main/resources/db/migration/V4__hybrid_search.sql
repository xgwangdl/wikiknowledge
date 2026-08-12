ALTER TABLE chunks ADD COLUMN IF NOT EXISTS search_vector tsvector;

UPDATE chunks SET search_vector = to_tsvector('simple', coalesce(content, '')) WHERE search_vector IS NULL;

CREATE INDEX IF NOT EXISTS idx_chunks_search_vector ON chunks USING GIN (search_vector);

CREATE OR REPLACE FUNCTION chunks_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('simple', coalesce(NEW.content, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_chunks_search_vector ON chunks;

CREATE TRIGGER trg_chunks_search_vector
    BEFORE INSERT OR UPDATE OF content ON chunks
    FOR EACH ROW
    EXECUTE FUNCTION chunks_search_vector_update();

COMMENT ON COLUMN chunks.search_vector IS '切片全文检索向量，由 content 自动生成，用于关键词混合检索';
