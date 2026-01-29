package xyz.dreature.soct.mapper.base;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.cursor.Cursor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BaseMapper<T, ID extends Serializable> {
    // ===== 通用基础操作 =====
    // 查询总数
    int countAll();

    // 查询全部
    List<T> selectAll();

    // 查询全部（游标）
    @Options(fetchSize = 1000)
    Cursor<T> selectAllWithCursor();

    // 查询随机
    List<T> selectRandom(int limit);

    // 查询页面
    List<T> selectByPage(int offset, int limit);

    // 条件查询
    List<T> selectByCondition(Map<String, Object> condition);

    // 单项查询
    T selectById(ID id);

    // 单批查询
    List<T> selectBatchByIds(List<ID> ids);

    // 单项插入
    int insert(T entity);

    // 单批插入
    int insertBatch(List<T> entities);

    // 单项更新
    int update(T entity);

    // 单批更新
    int updateBatch(List<T> entities);

    // 单项插入或更新
    int upsert(T entity);

    // 单批插入或更新
    int upsertBatch(List<T> entities);

    // 单项删除
    int deleteById(ID id);

    // 单批删除
    int deleteBatchByIds(List<ID> ids);

    // 清空
    void truncate();
}
