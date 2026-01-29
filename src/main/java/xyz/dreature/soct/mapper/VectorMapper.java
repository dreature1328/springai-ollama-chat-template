package xyz.dreature.soct.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VectorMapper {
    // 清空
    void truncate();
}
