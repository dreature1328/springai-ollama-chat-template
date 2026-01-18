package xyz.dreature.soct.common.model.vo;

import java.io.Serializable;
import java.time.Instant;

// 响应结果
public class Result<T> implements Serializable {
    // ===== 字段 =====
    private boolean success; // 操作是否成功
    private String code;     // 业务状态码
    private String message;  // 人类可读消息
    private T data;          // 业务数据
    private long timestamp;  // 响应时间戳（毫秒）

    // ===== 构造方法 =====
    // 无参构造器
    public Result() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    // 全参构造器
    private Result(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toEpochMilli();
    }

    // 复制构造器
    public Result(Result<T> result) {
        this.success = result.success;
        this.code = result.code;
        this.message = result.message;
        this.data = result.data;
        this.timestamp = result.timestamp;
    }

    // ===== 成功响应 =====
    public static <T> Result<T> success() {
        return new Result<>(true, "SUCCESS", "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "SUCCESS", "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, "SUCCESS", message, data);
    }

    // ===== 错误响应 =====
    public static <T> Result<T> error(String code, String message) {
        return new Result<>(false, code, message, null);
    }

    public static <T> Result<T> error(String code, String message, T data) {
        return new Result<>(false, code, message, data);
    }

    // ===== Getter 与 Setter 方法 =====
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ===== 其他 =====
    // 字符串表示
    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
