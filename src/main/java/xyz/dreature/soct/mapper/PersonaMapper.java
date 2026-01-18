package xyz.dreature.soct.mapper;

import org.apache.ibatis.annotations.Mapper;
import xyz.dreature.soct.common.model.entity.Persona;

@Mapper
public interface PersonaMapper extends BaseMapper<Persona, Long> {
    // ===== 业务扩展操作 =====
}
