package xyz.dreature.soct.common.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// 对话请求
public class ChatRequest {
    // ===== 字段 =====
    // 对话 ID
    private String conversationId = UUID.randomUUID().toString();

    // 用户输入
    @NotBlank(message = "用户输入不能为空")
    private String userInput;

    // 功能开关
    private boolean enablePersona = false;
    private boolean enableRag = true;
    private boolean enableContext = true;

    // 人格参数
    private long personaId = -1L; //-1 表示未选择

    // RAG 参数
    @Min(value = 1, message = "相似匹配数至少为 1")
    @Max(value = 50, message = "相似匹配数至多为 50")
    private int topK = 10;

    @Min(value = 0, message = "相似度阈值至少为 0")
    @Max(value = 1, message = "相似度阈值至多为 1")
    private double threshold = 0.7;

    // 上下文参数
    @Min(value = 1, message = "上下文数量至少为 1")
    @Max(value = 100, message = "上下文数量至多为 100")
    private int lastN = 10;

    // 规则类型
    @NotNull(message = "规则类型不能为空")
    private RuleType ruleType = RuleType.ASSISTANT;

    // ===== Getter 与 Setter 方法 =====
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public boolean isEnablePersona() {
        return enablePersona;
    }

    public void setEnablePersona(boolean enablePersona) {
        this.enablePersona = enablePersona;
    }

    public boolean isEnableRag() {
        return enableRag;
    }

    public void setEnableRag(boolean enableRag) {
        this.enableRag = enableRag;
    }

    public boolean isEnableContext() {
        return enableContext;
    }

    public void setEnableContext(boolean enableContext) {
        this.enableContext = enableContext;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getLastN() {
        return lastN;
    }

    public void setLastN(int lastN) {
        this.lastN = lastN;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    // ===== 其他 =====
    // 字符串表示
    @Override
    public String toString() {
        return "ChatRequest{" +
                "conversationId='" + conversationId + '\'' +
                ", userInput='" + userInput + '\'' +
                ", enablePersona=" + enablePersona +
                ", enableRag=" + enableRag +
                ", enableContext=" + enableContext +
                ", personaId=" + personaId +
                ", topK=" + topK +
                ", threshold=" + threshold +
                ", lastN=" + lastN +
                ", ruleType=" + ruleType +
                '}';
    }

    public enum RuleType {
        NONE,         // 无任何约束
        PERSONA,      // 人格化约束
        ASSISTANT     // 助手向约束
    }
}
