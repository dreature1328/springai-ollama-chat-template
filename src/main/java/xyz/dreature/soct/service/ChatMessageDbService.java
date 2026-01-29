package xyz.dreature.soct.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.dreature.soct.common.model.entity.ChatMessage;
import xyz.dreature.soct.mapper.ChatMessageMapper;
import xyz.dreature.soct.service.base.BaseDbService;

import java.util.List;

// 对话消息数据库服务
@Service
@Transactional
public class ChatMessageDbService extends BaseDbService<ChatMessage, String, ChatMessageMapper> {
    @Autowired
    ChatMessageDbService(ChatMessageMapper chatMessageMapper) {
        super(chatMessageMapper);
    }

    // ===== 业务扩展操作 =====
    // 按对话 ID 查询
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatMessage> selectByConversationId(String conversationId) {
        return mapper.selectByConversationId(conversationId);
    }

    // 按对话 ID 查询（指定最新数量）
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ChatMessage> selectLastNByConversationId(String conversationId, int lastN) {
        return mapper.selectLastNByConversationId(conversationId, lastN);
    }

    // 按对话 ID 删除
    public int deleteByConversationId(String conversationId) {
        return mapper.deleteByConversationId(conversationId);
    }
}
