package xyz.dreature.soct.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.TypeUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.dreature.soct.common.model.vo.Result;
import xyz.dreature.soct.service.DbService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

// 基操作接口（数据库）
@Slf4j
@Validated
public abstract class BaseDbController<T, ID extends Serializable> {
    protected final DbService<T, ID> dbService;
    protected final Class<T> entityClass;
    protected final Class<ID> idClass;

    protected BaseDbController(DbService<T, ID> dbService) {
        this.dbService = dbService;
        this.entityClass = (Class<T>) TypeUtil.getTypeArgument(this.getClass(), 0);
        this.idClass = (Class<ID>) TypeUtil.getTypeArgument(this.getClass(), 1);
    }

    // 查询总数
    @RequestMapping("/count-all")
    public ResponseEntity<Result<Integer>> countAll() {
        int count = dbService.countAll();
        String message = String.format("查询总数为 %d 条", count);
        log.info("查询总数完成，条数：{}", count);
        return ResponseEntity.ok().body(Result.success(message, count));
    }

    // 查询全部
    @RequestMapping("/select-all")
    public ResponseEntity<Result<List<T>>> selectAll() {
        List<T> result = dbService.selectAll();
        int resultCount = result.size();
        String message = String.format("全表查询 %d 条数据", resultCount);
        log.info("全表查询完成，条数：{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 查询随机
    @RequestMapping("/select-random")
    public ResponseEntity<Result<List<T>>> selectRandom(
            @RequestParam(name = "limit", defaultValue = "10")
            @Positive(message = "条数必须为正")
            int limit
    ) {
        List<T> result = dbService.selectRandom(limit);
        int resultCount = result.size();
        String message = String.format("随机查询 %d 条数据", resultCount);
        log.info("随机查询完成，条数：{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 查询页面
    @RequestMapping("/select-by-page")
    public ResponseEntity<Result<List<T>>> selectByPage(
            @RequestParam(name = "offset", defaultValue = "0")
            @Min(value = 0, message = "偏移量不能为负")
            int offset,

            @RequestParam(name = "limit")
            @Min(value = 0, message = "限数不能为负")
            int limit
    ) {
        List<T> result = dbService.selectByPage(offset, limit);
        int resultCount = result.size();
        String message = String.format("页面查询 %d 条数据", resultCount);
        log.info("页面查询完成，条数:{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 条件查询
    @RequestMapping("/select-by-condition")
    public ResponseEntity<Result<List<T>>> selectByCondition(
            @RequestBody
            @NotEmpty(message = "查询条件不能为空")
            Map<String, Object> condition
    ) {
        List<T> result = dbService.selectByCondition(condition);
        int resultCount = result.size();
        String message = String.format("条件查询 %d 条数据", resultCount);
        log.info("条件查询完成，条数：{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 逐项查询
    @RequestMapping("/select-by-ids")
    public ResponseEntity<Result<List<T>>> selectByIds(
            @RequestParam(name = "ids")
            @NotBlank(message = "ID 不能为空")
            @Pattern(regexp = "^\\d+(,\\d+)*$", message = "ID 需由逗号分隔")
            String ids
    ) {
        List<ID> idList = Convert.toList(idClass, StrSplitter.split(ids, ',', 0, true, false));
        ID[] idArray = ArrayUtil.toArray(idList, idClass);

        List<T> result = dbService.selectByIds(idArray);
        int resultCount = result.size();
        String message = String.format("逐项查询 %d 条数据", resultCount);
        log.info("逐项查询完成，条数:{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 分批查询
    @RequestMapping("/select-batch-by-ids")
    public ResponseEntity<Result<List<T>>> selectBatchByIds(
            @RequestParam(name = "ids")
            @NotBlank(message = "ID 不能为空")
            @Pattern(regexp = "^\\d+(,\\d+)*$", message = "ID 需由逗号分隔")
            String ids,

            @RequestParam(name = "batch-size", defaultValue = "1000")
            @Positive(message = "批大小必须为正")
            int batchSize
    ) {
        List<ID> idList = Convert.toList(idClass, StrSplitter.split(ids, ',', 0, true, false));

        List<T> result = dbService.selectBatchByIds(idList, batchSize);
        int resultCount = result.size();
        String message = String.format("分批查询 %d 条数据", resultCount);
        log.info("分批查询完成，条数：{}", resultCount);
        return ResponseEntity.ok().body(Result.success(message, result));
    }

    // 逐项插入
    @RequestMapping("/insert")
    public ResponseEntity<Result<Void>> insert(
            @RequestBody
            @NotEmpty(message = "插入的数据不能为空")
            @Valid
            T[] entities
    ) {
        int affectedRows = dbService.insert(entities);
        String message = String.format("逐项插入 %d 条数据", affectedRows);
        log.info("逐项插入完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 分批插入
    @RequestMapping("/insert-batch")
    public ResponseEntity<Result<Void>> insertBatch(
            @RequestBody
            @NotEmpty(message = "插入的数据不能为空")
            @Valid
            List<T> entities,

            @RequestParam(name = "batch-size", defaultValue = "1000")
            @Positive(message = "批大小必须为正")
            int batchSize
    ) {
        int affectedRows = dbService.insertBatch(entities, batchSize);
        String message = String.format("分批插入 %d 条数据", affectedRows);
        log.info("分批插入完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 逐项更新
    @RequestMapping("/update")
    public ResponseEntity<Result<Void>> update(
            @RequestBody
            @NotEmpty(message = "更新的数据不能为空")
            @Valid
            T[] entities
    ) {
        int affectedRows = dbService.update(entities);
        String message = String.format("逐项更新 %d 条数据", affectedRows);
        log.info("逐项更新完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 分批更新
    @RequestMapping("/update-batch")
    public ResponseEntity<Result<Void>> updateBatch(
            @RequestBody
            @NotEmpty(message = "更新的数据不能为空")
            @Valid
            List<T> entities,

            @RequestParam(name = "batch-size", defaultValue = "1000")
            @Positive(message = "批大小必须为正")
            int batchSize
    ) {
        int affectedRows = dbService.updateBatch(entities, batchSize);
        String message = String.format("分批更新 %d 条数据", affectedRows);
        log.info("分批更新完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 逐项插入或更新
    @RequestMapping("/upsert")
    public ResponseEntity<Result<Void>> upsert(
            @RequestBody
            @NotEmpty(message = "插入或更新的数据不能为空")
            @Valid
            List<T> entities
    ) {
        T[] entityArray = ArrayUtil.toArray(entities, entityClass);

        int affectedRows = dbService.upsert(entityArray);
        String message = String.format("逐项插入或更新 %d 条数据", affectedRows);
        log.info("逐项插入或更新完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 分批插入或更新
    @RequestMapping("/upsert-batch")
    public ResponseEntity<Result<Void>> upsertBatch(
            @RequestBody
            @NotEmpty(message = "插入或更新的数据不能为空")
            @Valid
            List<T> entities,

            @RequestParam(name = "batch-size", defaultValue = "1000")
            @Positive(message = "批大小必须为正")
            int batchSize
    ) {
        int affectedRows = dbService.upsertBatch(entities, batchSize);
        String message = String.format("分批插入或更新 %d 条数据", affectedRows);
        log.info("分批插入或更新完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 逐项删除
    @RequestMapping("/delete-by-ids")
    public ResponseEntity<Result<Void>> deleteById(
            @RequestParam(name = "ids")
            @NotBlank(message = "ID 不能为空")
            @Pattern(regexp = "^\\d+(,\\d+)*$", message = "ID 需由逗号分隔")
            String ids
    ) {
        List<ID> idList = Convert.toList(idClass, StrSplitter.split(ids, ',', 0, true, false));
        ID[] idArray = ArrayUtil.toArray(idList, idClass);

        int affectedRows = dbService.deleteByIds(idArray);
        String message = String.format("逐项删除 %d 条数据", affectedRows);
        log.info("逐项删除完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 分批删除
    @RequestMapping("/delete-batch-by-ids")
    public ResponseEntity<Result<Void>> deleteBatchByIds(
            @RequestParam(name = "ids")
            @NotBlank(message = "ID 不能为空")
            @Pattern(regexp = "^\\d+(,\\d+)*$", message = "ID 需由逗号分隔")
            String ids,

            @RequestParam(name = "batch-size", defaultValue = "1000")
            @Positive(message = "批大小必须为正")
            int batchSize
    ) {
        List<ID> idList = Convert.toList(idClass, StrSplitter.split(ids, ',', 0, true, false));

        int affectedRows = dbService.deleteBatchByIds(idList, batchSize);
        String message = String.format("分批删除 %d 条数据", affectedRows);
        log.info("分批删除完成，影响行数：{}", affectedRows);
        return ResponseEntity.ok().body(Result.success(message, null));
    }

    // 清空
    @RequestMapping("/truncate")
    public ResponseEntity<Result<Void>> truncate() {
        int count = dbService.countAll();
        dbService.truncate();
        String message = String.format("清空 %d 条数据", count);
        log.info("清空完成，条数：{}", count);
        return ResponseEntity.ok().body(Result.success(message, null));
    }
}
