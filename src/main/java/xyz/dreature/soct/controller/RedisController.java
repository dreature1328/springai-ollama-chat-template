package xyz.dreature.soct.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.dreature.soct.common.model.vo.Result;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// 操作接口（Redis）
@Slf4j
@RestController
@RequestMapping("/redis")
@Validated
public class RedisController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ===== Key 操作 =====
    // 删除指定键
    @RequestMapping("/key/delete")
    public ResponseEntity<Result> deleteKey(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Boolean result = redisTemplate.delete(key);
        String message = result ? "删除成功" : "键不存在";

        log.info("键删除完成，结果：{}，键名：{}", result, key);
        return ResponseEntity.ok(Result.success(message, result));
    }

    // 检查键是否存在
    @RequestMapping("/key/exist")
    public ResponseEntity<Result> hasKey(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Boolean exists = redisTemplate.hasKey(key);
        String message = exists ? "键存在" : "键不存在";

        log.info("键检查完成，存在：{}，键名：{}", exists, key);
        return ResponseEntity.ok(Result.success(message, exists));
    }

    // 设置键过期时间
    @RequestMapping("/key/expire")
    public ResponseEntity<Result> setExpire(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam
            @Min(value = 0, message = "过期时间不能为负")
            long timeout,

            @RequestParam(required = false, defaultValue = "SECONDS")
            TimeUnit unit
    ) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        String message = result ? "设置过期成功" : "键不存在";

        log.info("键过期时间设置完成，结果：{}，键名：{}", result, key);
        return ResponseEntity.ok(Result.success(message, result));
    }

    // ===== String 操作 =====
    // 设置键值对
    @RequestMapping("/string/set")
    public ResponseEntity<Result> setStringValue(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam
            @NotNull(message = "值不能为空")
            @Valid
            String value,

            @RequestParam(required = false)
            @Min(value = 0, message = "过期时间不能为负")
            Long timeout,

            @RequestParam(required = false, defaultValue = "SECONDS")
            TimeUnit unit
    ) {
        if (timeout != null && unit != null) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.info("字符串值设置完成(带过期时间)，键名：{}", key);
            return ResponseEntity.ok(Result.success("设置带过期时间的键值对成功", null));

        } else {
            redisTemplate.opsForValue().set(key, value);
            log.info("字符串值设置完成，键名：{}", key);
            return ResponseEntity.ok(Result.success("设置键值对成功", null));
        }
    }

    // 获取键值
    @RequestMapping("/string/get")
    public ResponseEntity<Result> getStringValue(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Object value = redisTemplate.opsForValue().get(key);
        String message = value != null ? "获取值成功" : "键不存在";

        log.info("字符串值获取完成，键名：{}，存在：{}", key, value != null);
        return ResponseEntity.ok(Result.success(message, value));
    }

    // 键不存在时设置值
    @RequestMapping("/string/set-if-absent")
    public ResponseEntity<Result> setStringIfAbsent(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestBody
            @NotNull(message = "值不能为空")
            Object value
    ) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
        String message = result ? "设置成功" : "键已存在";

        log.info("字符串值设置(仅当不存在)完成，结果：{}，键名：{}", result, key);
        return ResponseEntity.ok(Result.success(message, result));
    }

    // 值递增操作
    @RequestMapping("/string/increment")
    public ResponseEntity<Result> incrementStringValue(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam
            @Min(value = 0, message = "递增值不能为负")
            long delta
    ) {
        Long result = redisTemplate.opsForValue().increment(key, delta);

        log.info("字符串值递增完成，结果：{}，键名：{}", result, key);
        return ResponseEntity.ok(Result.success("递增成功", result));
    }

    // ===== Hash 操作 =====
    // 设置哈希字段值
    @RequestMapping("/hash/set")
    public ResponseEntity<Result> setHash(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam
            @NotBlank(message = "字段名不能为空")
            String field,

            @RequestBody
            @NotNull(message = "值不能为空")
            Object value
    ) {
        redisTemplate.opsForHash().put(key, field, value);
        log.info("哈希字段值设置完成，键名：{}，字段：{}", key, field);
        return ResponseEntity.ok(Result.success("设置哈希字段成功", null));
    }

    // 获取哈希字段值
    @RequestMapping("/hash/get")
    public ResponseEntity<Result> getHash(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam
            @NotBlank(message = "字段名不能为空")
            String field
    ) {
        Object value = redisTemplate.opsForHash().get(key, field);
        String message = value != null ? "获取哈希字段成功" : "字段不存在";

        log.info("哈希字段值获取完成，键名：{}，字段：{}，存在：{}", key, field, value != null);
        return ResponseEntity.ok(Result.success(message, value));
    }

    // 获取整个哈希表
    @RequestMapping("/hash/all")
    public ResponseEntity<Result> getAllHash(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        int entryCount = entries.size();
        log.info("整个哈希表获取完成，键名：{}，字段数：{}", key, entryCount);
        return ResponseEntity.ok(Result.success("获取整个哈希表成功", entries));

    }

    // 删除哈希字段
    @DeleteMapping("/hash/delete")
    public ResponseEntity<Result> deleteHash(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam
            @NotBlank(message = "字段名不能为空")
            String field
    ) {
        Long result = redisTemplate.opsForHash().delete(key, field);

        log.info("哈希字段删除完成，删除数：{}，键名：{}，字段：{}", result, key, field);
        return ResponseEntity.ok(Result.success("删除哈希字段成功", result));
    }

    // ===== List 操作 =====
    // 左端添加元素
    @RequestMapping("/list/push-left")
    public ResponseEntity<Result> leftPushToList(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestBody
            @NotNull(message = "值不能为空")
            Object value
    ) {
        Long size = redisTemplate.opsForList().leftPush(key, value);

        log.info("列表左端添加完成，新长度：{}，键名：{}", size, key);
        return ResponseEntity.ok(Result.success("左端添加元素成功", size));

    }

    // 右端添加元素
    @RequestMapping("/list/push-right")
    public ResponseEntity<Result> rightPushToList(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestBody
            @NotNull(message = "值不能为空")
            Object value
    ) {
        Long size = redisTemplate.opsForList().rightPush(key, value);

        log.info("列表右端添加完成，新长度：{}，键名：{}", size, key);
        return ResponseEntity.ok(Result.success("右端添加元素成功", size));
    }

    // 获取列表范围元素
    @RequestMapping("/list/range")
    public ResponseEntity<Result> getListRange(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "起始位置不能为负")
            long start,

            @RequestParam(defaultValue = "-1")
            long end
    ) {
        List<Object> range = redisTemplate.opsForList().range(key, start, end);
        int rangeCount = range != null ? range.size() : 0;
        log.info("列表范围获取完成，键名：{}，获取元素数：{}", key, rangeCount);
        return ResponseEntity.ok(Result.success("获取列表范围成功", range));

    }

    // 获取列表长度
    @RequestMapping("/list/size")
    public ResponseEntity<Result> getListSize(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Long size = redisTemplate.opsForList().size(key);

        log.info("列表长度获取完成，键名：{}，长度：{}", key, size);
        return ResponseEntity.ok(Result.success("获取列表长度成功", size));
    }

    // 左端弹出元素
    @RequestMapping("/list/pop-left")
    public ResponseEntity<Result> leftPopFromList(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Object value = redisTemplate.opsForList().leftPop(key);

        log.info("列表左端弹出完成，键名：{}，弹出元素存在：{}", key, value != null);
        return ResponseEntity.ok(Result.success("左端弹出元素成功", value));
    }

    // 右端弹出元素
    @RequestMapping("/list/pop-right")
    public ResponseEntity<Result> rightPopFromList(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Object value = redisTemplate.opsForList().rightPop(key);

        log.info("列表右端弹出完成，键名：{}，弹出元素存在：{}", key, value != null);
        return ResponseEntity.ok(Result.success("右端弹出元素成功", value));
    }


    // ===== Set 操作 =====
    // 添加集合元素
    @RequestMapping("/set/add")
    public ResponseEntity<Result> addToSet(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestBody
            @NotNull(message = "值不能为空")
            Object value
    ) {
        Long result = redisTemplate.opsForSet().add(key, value);

        log.info("集合元素添加完成，键名：{}，添加结果：{}", key, result);
        return ResponseEntity.ok(Result.success("添加集合元素成功", result));
    }

    // 获取集合所有元素
    @RequestMapping("/set/members")
    public ResponseEntity<Result> getSetMembers(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key
    ) {
        Set<Object> members = redisTemplate.opsForSet().members(key);

        int memberCount = members != null ? members.size() : 0;
        log.info("集合所有元素获取完成，键名：{}，元素数：{}", key, memberCount);
        return ResponseEntity.ok(Result.success("获取集合所有元素成功", members));

    }

    // ===== ZSet 操作 =====
    // 添加有序集合元素
    @RequestMapping("/zset/add")
    public ResponseEntity<Result> addToZSet(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestBody
            @NotNull(message = "值不能为空")
            Object value,

            @RequestParam
            @Min(value = 0, message = "分数不能为负")
            double score
    ) {
        Boolean result = redisTemplate.opsForZSet().add(key, value, score);

        log.info("有序集合元素添加完成，键名：{}，结果：{}", key, result);
        return ResponseEntity.ok(Result.success("添加有序集合元素成功", result));

    }

    // 获取有序集合范围元素
    @RequestMapping("/zset/range")
    public ResponseEntity<Result> getZSetRange(
            @RequestParam
            @NotBlank(message = "键不能为空")
            String key,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "起始位置不能为负")
            long start,

            @RequestParam(defaultValue = "-1")
            long end
    ) {
        Set<Object> range = redisTemplate.opsForZSet().range(key, start, end);

        int rangeCount = range != null ? range.size() : 0;
        log.info("有序集合范围获取完成，键名：{}，获取元素数：{}", key, rangeCount);
        return ResponseEntity.ok(Result.success("获取有序集合范围成功", range));

    }
}
