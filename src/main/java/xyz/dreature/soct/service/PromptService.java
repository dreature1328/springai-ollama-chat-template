package xyz.dreature.soct.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dreature.soct.common.model.dto.ChatRequest;
import xyz.dreature.soct.common.model.entity.Persona;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

// 提示词服务
@Slf4j
@Service
public class PromptService {
    @Autowired
    PersonaDbService personaDbService;  // 检索人格设定
    @Autowired
    VectorService vectorService;  // 检索文段记录
    @Autowired
    CacheService cacheService;  // 检索对话上下文
    @Autowired
    PersistentService persistentService;  // 检索对话历史

    // 构建提示词
    public Prompt buildPrompt(ChatRequest request) {
        SystemMessage systemMessage = buildSystemMessage(request);
        UserMessage userMessage = buildUserMessage(request);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return prompt;
    }

    // 构建系统提示词
    public SystemMessage buildSystemMessage(ChatRequest request) {
        String systemPrompt = "你是智能对话助手。";

        if (request.isEnablePersona()) {
            long personaId = request.getPersonaId();
            Persona persona = personaDbService.selectById(personaId);
            String personaName = Optional.ofNullable(persona).map(Persona::getName).orElse("（未知）");
            systemPrompt = "你是一个名为%s的数字化身。".formatted(personaName);
            return new SystemMessage(systemPrompt);
        }

        log.debug("系统提示词：{}", systemPrompt);

        return new SystemMessage(systemPrompt);
    }

    // 构建用户提示词
    public UserMessage buildUserMessage(ChatRequest request) {
        StringBuilder promptBuilder = new StringBuilder("\n");

        // 1. 【核心人格设定】
        if (request.isEnablePersona()) {
            promptBuilder.append("【核心人格设定】\n");
            promptBuilder.append(buildPersonaPrompt(request.getPersonaId()));
            promptBuilder.append("\n\n");
        }

        // 2. 【相关文段记录】
        if (request.isEnableRag()) {
            promptBuilder.append("【相关文段记录】\n");
            promptBuilder.append(buildDocPrompt(
                    request.getUserInput(),
                    request.getTopK(),
                    request.getThreshold()
            ));
            promptBuilder.append("\n\n");
        }

        // 3. 【近期对话上下文】
        if (request.isEnableContext()) {
            promptBuilder.append("【近期对话上下文】\n");
            promptBuilder.append(buildContextPrompt(
                    request.getConversationId(),
                    request.getLastN()
            ));
            promptBuilder.append("\n\n");
        }

        // 4. 【用户当前问题】
        promptBuilder.append("【用户当前问题】\n");
        promptBuilder.append(request.getUserInput());
        promptBuilder.append("\n\n");

        // 5. 【回答要求】
        promptBuilder.append("【回答要求】\n");
        promptBuilder.append(buildRulePrompt(request.getRuleType(), request.isEnableRag()));
        promptBuilder.append("\n\n");

        // 6. 【你的回答】
        promptBuilder.append("【你的回答】");

        log.debug("用户提示词：{}", promptBuilder);

        return new UserMessage(promptBuilder.toString());
    }


    // 构建【核心人格设定】提示词
    public String buildPersonaPrompt(long personaId) {
        // 检索人格设定
        return Optional.ofNullable(personaDbService.selectById(personaId))
                .map(persona -> """
                        - 个性特点：%s
                        - 语言风格：%s
                        - 背景故事：%s
                        - 关键词：%s
                        """.formatted(
                        // 安全获取每个属性并设置默认值
                        Objects.requireNonNullElse(persona.getPersonality(), "（暂无个性特点）"),
                        Objects.requireNonNullElse(persona.getLanguageStyle(), "（暂无语言风格）"),
                        Objects.requireNonNullElse(persona.getBackground(), "（暂无背景故事）"),
                        Objects.requireNonNullElse(StrUtil.join("、", persona.getKeywords()), "（暂无关键词）")
                ))
                .orElse("（暂无人格设定）");
    }

    // 构建【相关文段记录】提示词
    String buildDocPrompt(String userInput, int topK, double threshold) {
        // 检索文段记录
        List<Document> relevantDocs = vectorService.similaritySearch(userInput, topK, threshold);
        String docPrompt;
        if (relevantDocs.isEmpty()) {
            docPrompt = "（暂无相关文段记录）";
        } else {
            docPrompt = relevantDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));
        }

        return docPrompt;
    }

    // 构建【近期对话上下文】提示词
    public String buildContextPrompt(String conversationId, int lastN) {
        // 检索对话上下文
        // 1. 优先查询缓存
        List<Message> context = cacheService.get(conversationId, lastN);

        // 2. 若无缓存命中，则查询数据库
        if (context.isEmpty()) {
            log.debug("缓存未命中，对话 ID: {}", conversationId);
            context = persistentService.get(conversationId, lastN);

            // 3. 如果数据库中有数据，同步到缓存
            if (!context.isEmpty()) {
                cacheService.add(conversationId, context);
                log.debug("查询数据库成功，同步至缓存，对话 ID: {}", conversationId);
            }
        } else {
            log.debug("缓存命中，对话 ID: {}", conversationId);
        }

        String contextPrompt;
        if (context.isEmpty()) {
            contextPrompt = "（暂无对话上下文）";
        } else {
            contextPrompt = context.stream()
                    .map(message -> {
                        String prefix = "";
                        if (message instanceof SystemMessage) prefix = "系统：";
                        else if (message instanceof UserMessage) prefix = "用户：";
                        else if (message instanceof AssistantMessage) prefix = "助手：";
                        return prefix + message.getText();
                    })
                    .collect(Collectors.joining("\n\n"));
        }
        return contextPrompt;
    }

    // 构建【回答要求】提示词
    public String buildRulePrompt(ChatRequest.RuleType ruleType, boolean enableRAG) {
        return switch (ruleType) {
            case NONE -> "（暂无回答要求）";
            case ASSISTANT -> {
                if (enableRAG) {
                    yield """
                                1. 请保持客观中立的态度，给出简洁、准确、专业的回答。
                                2. 必须基于提供的文档内容，提取相关信息归纳、整理、分析。
                                3. 如果文档中没有相关信息，请说："根据现有文档，没有找到相关信息"。
                                4. 基于整理的信息，不要编造、假设或猜测信息。
                            """;
                } else {
                    yield """
                                1. 请保持客观中立的态度，给出简洁、准确、专业的回答。
                            """;
                }
            }
            case PERSONA -> {
                if (enableRAG) {
                    yield """
                                1. 请以第一人称口吻，用自然、亲切的语气回答。
                                2. 必须基于提供的人格设定和相关文档（体会语气、了解经历、理解观点）来回答。
                                3. 如果记忆中没有相关信息，请坦诚地说："根据我的记忆，我不太确定这件事"。
                                4. 可以合理推断，但不要编造明确未提及的事实。
                            """;
                } else {
                    yield """
                                1. 请以第一人称口吻，用自然、亲切的语气回答。
                                2. 必须基于提供的人格设定来回答。
                            """;
                }
            }
        };
    }
}
