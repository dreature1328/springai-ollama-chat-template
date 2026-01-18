CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    personality TEXT,
    language_style TEXT,
    background TEXT,
    keywords TEXT[]
);

COMMENT ON COLUMN persona.id IS 'ID';
COMMENT ON COLUMN persona.name IS '人格名称';
COMMENT ON COLUMN persona.personality IS '个性特点';
COMMENT ON COLUMN persona.language_style IS '语言风格';
COMMENT ON COLUMN persona.background IS '背景故事';
COMMENT ON COLUMN persona.keywords IS '关键词';
