package xyz.dreature.soct.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import xyz.dreature.soct.service.ChatService;

@RestController
public class ChatController {
    // 聊天服务
    @Autowired
    ChatService chatService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public String chat(
            @RequestParam(name = "conversation-id", required = false)
            String conversationId,

            @RequestParam(name = "user-input")
            @NotBlank(message = "输入不能为空")
            String userInput
    ) {
        return chatService.chat(conversationId, userInput);
    }

    @RequestMapping(value = "/chat-stream", produces = "text/html;charset=utf-8")
    public Flux<String> chatStream(
            @RequestParam(name = "conversation-id", required = false)
            String conversationId,

            @RequestParam(name = "user-input")
            @NotBlank(message = "输入不能为空")
            String userInput
    ) {
        return chatService.chatStream(conversationId, userInput);
    }
}
