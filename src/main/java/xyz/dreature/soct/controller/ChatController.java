package xyz.dreature.soct.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import xyz.dreature.soct.service.ChatService;

// 操作接口（对话）
@RestController
public class ChatController {
    // 聊天服务
    @Autowired
    ChatService chatService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public ResponseEntity<String> chat(
            @RequestParam(name = "conversation-id", required = false)
            String conversationId,

            @RequestParam(name = "user-input")
            @NotBlank(message = "输入不能为空")
            String userInput,

            @RequestParam(name = "persona-id", required = false)
            Long personaId
    ) {
        return ResponseEntity.ok(chatService.chat(conversationId, userInput, personaId));
    }

    @RequestMapping(value = "/chat-stream", produces = "text/html;charset=utf-8")
    public ResponseEntity<Flux<String>> chatStream(
            @RequestParam(name = "conversation-id", required = false)
            String conversationId,

            @RequestParam(name = "user-input")
            @NotBlank(message = "输入不能为空")
            String userInput,

            @RequestParam(name = "persona-id", required = false)
            Long personaId
    ) {
        return ResponseEntity.ok(chatService.chatStream(conversationId, userInput, personaId));
    }
}
