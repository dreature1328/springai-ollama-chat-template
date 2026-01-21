CREATE TABLE chat_messages (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    text TEXT,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN chat_message.id IS '消息ID';
COMMENT ON COLUMN chat_message.conversation_id IS '对话ID';
COMMENT ON COLUMN chat_message.message_type IS '消息类型';
COMMENT ON COLUMN chat_message.text IS '文本';
COMMENT ON COLUMN chat_message.metadata IS '元数据';
COMMENT ON COLUMN chat_message.created_at IS '创建时间';