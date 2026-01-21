package xyz.dreature.soct.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

// 对话配置
@Configuration
public class ChatConfig {
    // 本地内存记忆服务（可按需切换）
    @Bean
    @Lazy
    public ChatMemory inMemoryChatMemory() {
        return new InMemoryChatMemory();
    }

    // 对话客户端
    @Bean
    public ChatClient chatClient(@Qualifier("ollamaChatModel") ChatModel model) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
//                        new MessageChatMemoryAdvisor(chatMemory) // 自动添加对话到记忆
                )
                .build();
    }
}
