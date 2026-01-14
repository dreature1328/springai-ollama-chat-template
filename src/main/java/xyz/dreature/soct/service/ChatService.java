package xyz.dreature.soct.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

// 聊天服务
@Slf4j
@Service
public class ChatService {
    @Autowired
    private ChatClient chatClient; // Spring AI 的聊天客户端
    @Autowired
    private MemoryService memoryService;

    // 阻塞式响应
    public String chat(String conversationId, String userInput) {
        // 1. 保存用户输入
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        memoryService.addUserMessage(conversationId, userInput);

        // 2. 获取历史记录作为上下文
        List<Message> history = memoryService.getHistory(conversationId);

        log.debug("对话 ID：{}，对话上下文：{}", conversationId, history);

        // 3. 创建包含上下文的 Prompt
        Prompt prompt = new Prompt(history);

        // 4. 调用 AI 并获取回复
        String aiResponse = chatClient.prompt(prompt).call().content();

        // 5. 保存 AI 回复
        memoryService.addAssistantMessage(conversationId, aiResponse);

        // 6. 返回 AI 回复
        return aiResponse;
    }

    // 流式响应
    public Flux<String> chatStream(String conversationId, String userInput) {
        // 1. 保存用户输入
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }
        memoryService.addUserMessage(conversationId, userInput);

        // 2. 获取历史记录作为上下文
        List<Message> history = memoryService.getHistory(conversationId);

        log.debug("对话 ID：{}，对话上下文：{}", conversationId, history);

        // 3. 创建包含上下文的 Prompt
        Prompt prompt = new Prompt(history);

        // 4. 创建用于收集 AI 响应的 StringBuilder
        StringBuilder aiResponseBuilder = new StringBuilder();

        // 5. 调用 AI 流式 API
        final String finalConversationId = conversationId;
        return chatClient.prompt(prompt)
                .stream()
                .content()  // 获取响应内容的Flux
                .doOnNext(chunk -> {
                    // 6. 实时处理每个数据块
                    aiResponseBuilder.append(chunk);
                    // 可选：如果需要实时推送，可以在这里添加
                })
                .doOnComplete(() -> {
                    // 7. 流完成后保存完整响应
                    String fullResponse = aiResponseBuilder.toString();
                    memoryService.addAssistantMessage(finalConversationId, fullResponse);
                })
                .doOnError(error -> {
                    // 8. 错误处理
                    System.err.println("AI调用失败: " + error.getMessage());
                    // 保存错误信息
                    memoryService.addAssistantMessage(finalConversationId, "抱歉，处理请求时出错");
                });
    }
}
