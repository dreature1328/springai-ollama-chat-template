package xyz.dreature.soct.service;


import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 历史记忆服务
@Service
public class MemoryService {
    // 键值对存储和管理对话历史
    private final Map<String, List<Message>> conversationHistory = new HashMap<>();

    // 添加用户消息到历史
    public void addUserMessage(String conversationId, String text) {
        List<Message> history = conversationHistory.getOrDefault(conversationId, new ArrayList<>());
        history.add(new UserMessage(text));
        conversationHistory.put(conversationId, history);
    }

    // 添加 AI 回复到历史
    public void addAssistantMessage(String conversationId, String text) {
        List<Message> history = conversationHistory.getOrDefault(conversationId, new ArrayList<>());
        history.add(new AssistantMessage(text));
        conversationHistory.put(conversationId, history);
    }

    // 获取完整对话历史
    public List<Message> getHistory(String conversationId) {
        return conversationHistory.getOrDefault(conversationId, new ArrayList<>());
    }
}
