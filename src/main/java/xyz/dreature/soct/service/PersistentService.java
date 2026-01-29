package xyz.dreature.soct.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dreature.soct.common.model.entity.ChatMessage;
import xyz.dreature.soct.mapper.ChatMessageMapper;

import java.util.Collections;
import java.util.List;

// 记忆持久化服务（所有对话）
@Slf4j
@Service
public class PersistentService implements ChatMemory {
    @Autowired
    ChatMessageMapper chatMessageMapper;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<ChatMessage> chatMessages = messages.stream()
                .map(message -> new ChatMessage(conversationId, message))
                .toList();

        int affectedRows = chatMessageMapper.upsertBatch(chatMessages);
        log.info("添加完成，影响行数：{}", affectedRows);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectLastNByConversationId(conversationId, lastN);
        Collections.reverse(chatMessages); // 使对话消息顺序由旧至新
        List<Message> messages = chatMessages.stream().map(ChatMessage::toMessage).toList();
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        int affectedRows = chatMessageMapper.deleteByConversationId(conversationId);
        log.info("删除完成，影响行数：{}", affectedRows);
    }
}
