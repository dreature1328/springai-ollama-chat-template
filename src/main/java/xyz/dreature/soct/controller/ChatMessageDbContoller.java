package xyz.dreature.soct.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.dreature.soct.common.model.entity.ChatMessage;
import xyz.dreature.soct.common.model.vo.Result;
import xyz.dreature.soct.controller.base.BaseDbController;
import xyz.dreature.soct.service.ChatMessageDbService;

import java.util.List;

// 操作接口（对话消息数据库）
@Slf4j
@RestController
@RequestMapping("/db/chat-message")
@Validated
public class ChatMessageDbContoller extends BaseDbController<ChatMessage, String, ChatMessageDbService> {
    @Autowired
    ChatMessageDbContoller(ChatMessageDbService chatMessageDbService) {
        super(chatMessageDbService);
    }

    // 按对话 ID 查询
    @RequestMapping("/select-by-conversation-id")
    public ResponseEntity<Result<List<ChatMessage>>> selectByConversationId(
            @RequestParam(name = "conversation-id")
            @NotBlank(message = "ID 不能为空")
            String conversationId
    ) {
        List<ChatMessage> result = dbService.selectByConversationId(conversationId);
        int resultCount = result.size();
        String message = String.format("查询 %d 条数据", resultCount);
        log.info("查询完成，条数:{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 按对话 ID 删除
    @RequestMapping("/delete-by-conversation-id")
    public ResponseEntity<Result<Void>> deleteByConversationId(
            @RequestParam(name = "conversation-id")
            @NotBlank(message = "ID 不能为空")
            String conversationId
    ) {
        int affectedRows = dbService.deleteByConversationId(conversationId);
        String message = String.format("删除 %d 条数据", affectedRows);
        log.info("删除完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }
}
