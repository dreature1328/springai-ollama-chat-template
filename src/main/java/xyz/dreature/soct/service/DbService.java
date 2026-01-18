package xyz.dreature.soct.service;

import org.apache.ibatis.cursor.Cursor;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.DataProcessingException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.dreature.soct.common.util.BatchUtils;
import xyz.dreature.soct.mapper.BaseMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

// 数据库服务
@Transactional
public class DbService<T, ID extends Serializable> {
    // ORM 映射器
    protected BaseMapper<T, ID> mapper;

    public DbService(BaseMapper<T, ID> mapper) {
        this.mapper = mapper;
    }

    // 查询总数
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public int countAll() {
        return mapper.countAll();
    }

    // 查询全部
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectAll() {
        return mapper.selectAll();
    }

    // 查询全部（游标）
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Cursor<T> selectAllWithCursor() {
        return mapper.selectAllWithCursor();
    }

    // 处理全部（游标）
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void processAllWithCursor(Consumer<T> processor) {
        try (Cursor<T> cursor = mapper.selectAllWithCursor()) {
            BatchUtils.processEach(cursor, processor);
        } catch (IOException e) {
            throw new DataProcessingException("游标处理失败", e);
        }
    }

    // 转换全部（游标）
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public <R> List<R> transformAllWithCursor(Function<T, List<R>> transformer) {
        try (Cursor<T> cursor = mapper.selectAllWithCursor()) {
            return BatchUtils.flatMapEach(cursor, transformer);
        } catch (IOException e) {
            throw new DataProcessingException("游标处理失败", e);
        }
    }

    // 查询随机
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectRandom(int limit) {
        return mapper.selectRandom(limit);
    }

    // 查询页面
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectByPage(int offset, int limit) {
        return mapper.selectByPage(offset, limit);
    }

    // 条件查询
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectByCondition(Map<String, Object> condition) {
        return mapper.selectByCondition(condition);
    }

    // 单项查询
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public T selectById(ID id) {
        return mapper.selectById(id);
    }

    // 逐项查询
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectByIds(ID... ids) {
        return BatchUtils.mapEach(Arrays.asList(ids), mapper::selectById);
    }

    // 单批查询
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectBatchByIds(List<ID> ids) {
        return mapper.selectBatchByIds(ids);
    }

    // 分批查询
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<T> selectBatchByIds(List<ID> ids, int batchSize) {
        return BatchUtils.flatMapBatch(ids, batchSize, mapper::selectBatchByIds);
    }

    // 单项插入
    public int insert(T entity) {
        return mapper.insert(entity);
    }

    // 逐项插入
    public int insert(T... entities) {
        return BatchUtils.reduceEachToInt(Arrays.asList(entities), mapper::insert);
    }

    // 单批插入
    public int insertBatch(List<T> list) {
        return mapper.insertBatch(list);
    }

    // 分批插入
    public int insertBatch(List<T> list, int batchSize) {
        return BatchUtils.reduceBatchToInt(list, batchSize, mapper::insertBatch);
    }

    // 单项更新
    public int update(T entity) {
        return mapper.update(entity);
    }

    // 逐项更新
    public int update(T... entities) {
        return BatchUtils.reduceEachToInt(Arrays.asList(entities), mapper::update);
    }

    // 单批更新
    public int updateBatch(List<T> list) {
        return mapper.updateBatch(list);
    }

    // 分批更新
    public int updateBatch(List<T> list, int batchSize) {
        return BatchUtils.reduceBatchToInt(list, batchSize, mapper::updateBatch);
    }

    // 单项插入或更新
    public int upsert(T entity) {
        return mapper.upsert(entity);
    }

    // 逐项插入或更新
    public int upsert(T... entities) {
        return BatchUtils.reduceEachToInt(Arrays.asList(entities), mapper::upsert);
    }

    // 单批插入或更新
    public int upsertBatch(List<T> list) {
        return mapper.upsertBatch(list);
    }

    // 分批插入或更新
    public int upsertBatch(List<T> list, int batchSize) {
        return BatchUtils.reduceBatchToInt(list, batchSize, mapper::upsertBatch);
    }

    // 单项删除
    public int deleteById(ID id) {
        return mapper.deleteById(id);
    }

    // 逐项删除
    public int deleteByIds(ID... ids) {
        return BatchUtils.reduceEachToInt(Arrays.asList(ids), mapper::deleteById);
    }

    // 单批删除
    public int deleteBatchByIds(List<ID> ids) {
        return mapper.deleteBatchByIds(ids);
    }

    // 分批删除
    public int deleteBatchByIds(List<ID> ids, int batchSize) {
        return BatchUtils.reduceBatchToInt(ids, batchSize, mapper::deleteBatchByIds);
    }

    // 清空
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void truncate() {
        mapper.truncate();
    }
}
