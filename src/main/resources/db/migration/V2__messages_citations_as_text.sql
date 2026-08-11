ALTER TABLE messages ALTER COLUMN citations TYPE TEXT USING citations::text;

COMMENT ON COLUMN messages.citations IS 'AI 回答的引用来源，JSON 文本，记录命中的 chunk/document 信息';
