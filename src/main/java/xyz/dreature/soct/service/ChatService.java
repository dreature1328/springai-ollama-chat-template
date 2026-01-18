package xyz.dreature.soct.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import xyz.dreature.soct.common.model.entity.Persona;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

// 聊天服务
@Slf4j
@Service
public class ChatService {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private MemoryService memoryService;
    @Autowired
    private VectorService vectorService;
    @Autowired
    private DbService<Persona, Long> personaDbService;

    // 阻塞式聊天（默认）
    public String chat(String conversationId, String userInput) {
        return chat(conversationId, userInput, null);
    }

    // 阻塞式聊天（人格模拟）
    public String chat(@Nullable String conversationId, String userInput, @Nullable Long personaId) {
        // 1. 对话 ID 生成
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        // 2. 以上下文对话或人格模拟构建提示词
        Prompt prompt = personaId != null ?
                buildPrompt(conversationId, userInput, personaId) :
                buildPrompt(conversationId, userInput);

        // 3. 调用 AI 并阻塞响应
        return getResponse(conversationId, prompt);
    }

    // 流式聊天（默认）
    public Flux<String> chatStream(String conversationId, String userInput) {
        return chatStream(conversationId, userInput, null);
    }

    // 流式聊天（人格模拟）
    public Flux<String> chatStream(@Nullable String conversationId, String userInput, @Nullable Long personaId) {
        // 1. 对话 ID 生成
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        // 2. 以上下文对话或人格模拟构建提示词
        Prompt prompt = personaId != null ?
                buildPrompt(conversationId, userInput, personaId) :
                buildPrompt(conversationId, userInput);

        // 3. 调用 AI 并流响应
        return getResponseStream(conversationId, prompt);
    }

    // 构建提示词（默认）
    public Prompt buildPrompt(String conversationId, String userInput) {
        // 1. 记忆用户输入
        memoryService.addUserMessage(conversationId, userInput);

        // 2. 获取历史记录作为上下文
        List<Message> history = memoryService.getHistory(conversationId);

        log.debug("对话 ID：{}", conversationId);
        log.debug("对话上下文：{}", history);

        return new Prompt(history);
    }

    // 构建提示词（人格模拟）
    public Prompt buildPrompt(String conversationId, String userInput, Long personaId) {
        // 1. 检索人格设定
        Persona persona = personaDbService.selectById(personaId);

        String personaPrompt = """
                    - 个性特点：%s
                    - 语言风格：%s
                    - 背景故事：%s
                    - 关键词：%s
                """.formatted(
                Objects.requireNonNullElse(persona.getPersonality(), "（暂无个性特点）"),
                Objects.requireNonNullElse(persona.getLanguageStyle(), "（暂无语言风格）"),
                Objects.requireNonNullElse(persona.getBackground(), "（暂无背景故事）"),
                Objects.requireNonNullElse(StrUtil.join("、", persona.getKeywords()), "（暂无关键词）")
        );

        // 2. 检索文段记录
        List<Document> relevantDocs = vectorService.similaritySearch(userInput, 5, 0.7);
        String docPrompt = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // 3. 检索对话历史
        List<Message> history = memoryService.getHistory(conversationId);
        String historyPrompt;
        if (history.isEmpty()) {
            historyPrompt = "（暂无对话历史）";
        } else {
            historyPrompt = history.stream()
                    .map(message -> {
                        String prefix = "";
                        if (message instanceof SystemMessage) prefix = "系统：";
                        else if (message instanceof UserMessage) prefix = "用户：";
                        else if (message instanceof AssistantMessage) prefix = "助手：";
                        return prefix + message.getText();
                    })
                    .collect(Collectors.joining("\n\n"));
        }

        String rulePrompt = """
                    1. 请以第一人称口吻，用自然、亲切的语气回答。
                    2. 必须基于提供的人格设定和文字记录（体会语气、了解经历、理解观点）来回答。
                    3. 如果记忆中没有相关信息，请坦诚地说：“根据我的记忆，我不太确定这件事”。
                    4. 可以合理推断，但不要编造明确未提及的事实。
                """;

//        String rulePrompt = """
//            1. 请保持客观中立的态度，给出简洁、准确、专业的回答。
//            2. 必须基于提供的文档内容，提取相关信息归纳、整理、分析。
//            3. 如果文档中没有相关信息，请说：“根据现有文档，没有找到相关信息”。
//            4. 基于整理的信息，不要编造、假设或猜测信息。
//        """;

        String systemPrompt = "你是一个名为%s的数字化身。".formatted(persona.getName());
        String userPrompt = """
                【核心人格设定】
                %s
                            
                【相关文段记录】
                %s
                            
                【最近的对话历史】
                %s
                            
                【用户当前的问题】
                %s
                            
                【回答要求】
                %s
                            
                【你的回答】
                """.formatted(
                personaPrompt,
                docPrompt,
                historyPrompt,
                rulePrompt,
                userInput
        );

        log.debug("对话 ID：{}", conversationId);
        log.debug("系统提示词：{}", systemPrompt);
        log.debug("用户提示词：{}", userPrompt);

        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(userPrompt);
        Prompt prompt = new Prompt(Arrays.asList(systemMessage, userMessage));

        // 4. 记忆用户输入
        memoryService.addUserMessage(conversationId, userInput);

        return prompt;
    }

    // 阻塞式响应
    public String getResponse(String conversationId, Prompt prompt) {
        String response = chatClient.prompt(prompt).call().content();

        // 记忆 AI 回复
        memoryService.addAssistantMessage(conversationId, response);

        return response;
    }

    // 流式响应
    public Flux<String> getResponseStream(String conversationId, Prompt prompt) {
        StringBuilder responseBuilder = new StringBuilder();

        return chatClient.prompt(prompt)
                .stream()
                .content()  // 获取响应内容的 Flux
                .doOnNext(chunk -> {
                    // 实时处理每个数据块
                    responseBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流完成后记忆 AI 回复
                    String fullResponse = responseBuilder.toString();
                    memoryService.addAssistantMessage(conversationId, fullResponse);
                })
                .doOnError(error -> {
                    // 错误处理
                    System.err.println("AI 调用失败: " + error.getMessage());
                    // 保存错误信息
                    memoryService.addAssistantMessage(conversationId, "抱歉，处理请求时出错");
                });
    }
}
