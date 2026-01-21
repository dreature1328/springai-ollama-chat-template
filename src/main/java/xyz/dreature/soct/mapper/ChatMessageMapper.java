package xyz.dreature.soct.mapper;

import org.apache.ibatis.annotations.Mapper;
import xyz.dreature.soct.common.model.entity.ChatMessage;
import xyz.dreature.soct.mapper.base.BaseMapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage, String> {
    // 按对话 ID 查询
    List<ChatMessage> selectByConversationId(String conversationId);

    // 按对话 ID 查询（指定最新数量）
    List<ChatMessage> selectLastNByConversationId(String conversationId, int lastN);

    // 按对话 ID 删除
    int deleteByConversationId(String conversationId);
}
