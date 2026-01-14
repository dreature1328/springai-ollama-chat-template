import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import reactor.core.publisher.Flux;

@Slf4j
public class Chat {
    public ChatClient getChatClient() {
        return ChatClient
                .builder(getChatModel())
                .defaultSystem("你是智能聊天助手。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                .build();
    }

    public ChatModel getChatModel() {
        OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");
        ChatModel model = OllamaChatModel.builder()
                .defaultOptions(OllamaOptions.builder()
                        .model("deepseek-r1:8b")
                        .build())
                .ollamaApi(ollamaApi)
                .build();
        return model;
    }

    @Test
    public void chat() throws InterruptedException {
        String message = "告诉我你是谁";
        ChatClient chatClient = getChatClient();
        Flux<String> result = chatClient.prompt()
                .user(message).stream().content()
                .doOnNext(System.out::print);

        result.subscribe();

        Thread.sleep(10000);
    }
}
