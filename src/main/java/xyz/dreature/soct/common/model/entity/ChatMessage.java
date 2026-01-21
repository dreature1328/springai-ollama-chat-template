package xyz.dreature.soct.common.model.entity;

import org.springframework.ai.chat.messages.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// 对话消息实体
public class ChatMessage {
    // ===== 字段 =====
    private String id;        // ID
    private String conversationId;   // 所属对话的 ID
    private MessageType messageType;        // 角色
    private String text;          // 内容
    private Map<String, Object> metadata; // 元信息
    private LocalDateTime createdAt;  // 创建时间

    // ===== 构造方法 =====
    // 无参构造器
    public ChatMessage() {
    }

    // 全参构造器
    public ChatMessage(String id, String conversationId, MessageType messageType, String text, Map<String, Object> metadata, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.messageType = messageType;
        this.text = text;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    // 复制构造器
    public ChatMessage(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.conversationId = chatMessage.getConversationId();
        this.messageType = chatMessage.getMessageType();
        this.text = chatMessage.getText();
        this.metadata = chatMessage.getMetadata();
        this.createdAt = chatMessage.getCreatedAt();
    }

    // 转换构造器
    public ChatMessage(String conversationId, Message message) {
        this.id = UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.messageType = message.getMessageType();
        this.text = message.getText();
        this.metadata = message.getMetadata();
        this.createdAt = LocalDateTime.now();
    }

    // 转换方法
    public Message toMessage() {
        return switch (messageType) {
            case SYSTEM -> new SystemMessage(text);
            case USER -> new UserMessage(text, List.of(), metadata);
            case ASSISTANT -> new AssistantMessage(text, metadata, List.of(), List.of());
            default -> throw new IllegalArgumentException("不受支持的消息类型：" + messageType);
        };
    }

    // ===== Getter 与 Setter 方法 =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ===== 其他 =====
    // 字符串表示
    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", messageType=" + messageType +
                ", text='" + text + '\'' +
                ", metadata=" + metadata +
                ", createdAt=" + createdAt +
                '}';
    }
}
