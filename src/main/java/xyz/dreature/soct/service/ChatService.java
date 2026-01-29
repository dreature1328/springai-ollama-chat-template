package xyz.dreature.soct.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import xyz.dreature.soct.common.model.dto.ChatRequest;

import java.util.List;

// 对话服务
@Slf4j
@Service
public class ChatService {
    @Autowired
    private ChatClient chatClient;  // 以客户端进行对话
    @Autowired
    private ChatMemory cacheService;   // 缓存对话上下文（近期对话）
    @Autowired
    private ChatMemory persistentService;  // 持久化对话消息（所有对话）
    @Autowired
    private PromptService promptService;  // 构建提示词

    // 阻塞式聊天
    public String chat(ChatRequest request) {
        // 构建提示词
        Prompt prompt = promptService.buildPrompt(request);
        // 阻塞响应
        String response = chatClient.prompt(prompt).call().content();
        // 手动保存用户输入和助手回复
        if (request.isEnableContext()) {
            saveUserInput(request.getConversationId(), request.getUserInput());
            saveAssistantResponse(request.getConversationId(), response);
        }

        log.debug("完成一轮对话：{}", request.getConversationId());

        return response;
    }

    // 流式聊天
    public Flux<String> chatStream(ChatRequest request) {
        // 构建提示词
        Prompt prompt = promptService.buildPrompt(request);
        // 流式响应
        StringBuilder responseBuilder = new StringBuilder();
        Flux<String> response = chatClient.prompt(prompt)
                .stream()
                .content()  // 获取响应内容的 Flux
                .doOnNext(chunk -> {
                    // 实时处理每个数据块
                    responseBuilder.append(chunk);
                })
                .doOnComplete(() -> { // 流完成后
                    // 手动保存用户输入和助手回复
                    if (request.isEnableContext()) {
                        saveUserInput(request.getConversationId(), request.getUserInput());
                        saveAssistantResponse(request.getConversationId(), responseBuilder.toString());
                    }
                    log.debug("完成一轮对话：{}", request.getConversationId());
                })
                .doOnError(error -> {
                    // 错误处理
                    System.err.println("AI 调用失败: " + error.getMessage());
                    // 手动保存错误信息
                    if (request.isEnableContext()) {
                        saveAssistantResponse(request.getConversationId(), "抱歉，处理请求时出错");
                    }
                });

        return response;
    }

    // 保存用户消息
    private void saveUserInput(String conversationId, String userInput) {
        UserMessage userMessage = new UserMessage(userInput);

        // 持久化对话消息
        persistentService.add(conversationId, List.of(userMessage));

        // 缓存对话上下文
        cacheService.add(conversationId, List.of(userMessage));

        log.debug("用户消息已保存，对话ID: {}, 内容长度: {}", conversationId, userInput.length());
    }

    // 保存助手消息
    private void saveAssistantResponse(String conversationId, String response) {
        AssistantMessage assistantMessage = new AssistantMessage(response);

        // 持久化对话消息
        persistentService.add(conversationId, List.of(assistantMessage));

        // 缓存对话上下文
        cacheService.add(conversationId, List.of(assistantMessage));

        log.debug("助手消息已保存，对话ID: {}, 内容长度: {}", conversationId, response.length());
    }

    // 清空对话上下文
    public void clearContext(String conversationId) {
        // 清空对话消息的持久化数据
        persistentService.clear(conversationId);

        // 清空对话上下文的缓存数据
        cacheService.clear(conversationId);

        log.debug("上下文已清空，对话ID: {}", conversationId);
    }
}
