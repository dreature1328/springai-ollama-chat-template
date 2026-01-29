package xyz.dreature.soct.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import xyz.dreature.soct.common.model.dto.ChatRequest;
import xyz.dreature.soct.common.model.vo.Result;
import xyz.dreature.soct.service.ChatService;

// 操作接口（对话）
@RestController
@RequestMapping("/chat")
@Validated
public class ChatController {
    // 对话服务
    @Autowired
    ChatService chatService;

    // 阻塞式对话
    @RequestMapping(value = "/complete", produces = "text/html;charset=utf-8")
    public ResponseEntity<String> chat(@RequestBody @Validated ChatRequest chatRequest) {
        return ResponseEntity.ok(chatService.chat(chatRequest));
    }

    // 流式对话
    @RequestMapping(value = "/complete-stream", produces = "text/html;charset=utf-8")
    public ResponseEntity<Flux<String>> chatStream(@RequestBody @Validated ChatRequest chatRequest) {
        return ResponseEntity.ok(chatService.chatStream(chatRequest));
    }

    // 清空上下文
    @RequestMapping(value = "/clear")
    public ResponseEntity<Result<Void>> clear(
            @RequestParam(name = "conversation-id")
            @NotBlank(message = "ID 不能为空")
            String conversationId
    ) {
        chatService.clearContext(conversationId);
        return ResponseEntity.ok(Result.success("对话上下文已清空", null));
    }
}
