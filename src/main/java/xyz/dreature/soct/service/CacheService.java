package xyz.dreature.soct.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.dreature.soct.common.model.entity.ChatMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

// 记忆缓存服务（近期对话）
@Slf4j
@Component
public class CacheService implements ChatMemory {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${app.chat.cache.key-prefix}")
    private String PREFIX;
    @Value("${app.chat.cache.expire-seconds}")
    private long expireSeconds;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        String cacheKey = PREFIX + conversationId;

        List<String> list = messages.stream()
                .map(message -> new ChatMessage(conversationId, message))
                .map(chatMessage -> {
                    try {
                        return objectMapper.writeValueAsString(chatMessage);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("序列化聊天消息失败", e);
                    }
                }).toList();
        // 全部添加到列表右端
        redisTemplate.opsForList().rightPushAll(cacheKey, list);

        // 设置或刷新过期时间
        redisTemplate.expire(cacheKey, expireSeconds, TimeUnit.SECONDS);

        log.debug("已添加消息到缓存，条数：{}，对话 ID: {}", messages.size(), conversationId);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String cacheKey = PREFIX + conversationId;
        long size = redisTemplate.opsForList().size(cacheKey);

        List<String> list = redisTemplate.opsForList().range(cacheKey, size - lastN, size - 1);

        log.debug("已从缓存中获取消息，条数：{}，对话 ID: {}", list.size(), conversationId);

        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(s -> {
            try {
                return objectMapper.readValue(s, ChatMessage.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).map(ChatMessage::toMessage).toList();
    }

    @Override
    public void clear(String conversationId) {
        String cacheKey = PREFIX + conversationId;
        redisTemplate.delete(cacheKey);
        log.debug("已删除缓存，对话 ID: {}", conversationId);
    }
}
