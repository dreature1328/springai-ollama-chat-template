package xyz.dreature.soct.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.dreature.soct.common.model.entity.Persona;
import xyz.dreature.soct.mapper.PersonaMapper;
import xyz.dreature.soct.service.base.BaseDbService;

// 人格设定数据库服务
@Service
@Transactional
public class PersonaDbService extends BaseDbService<Persona, Long, PersonaMapper> {
    @Autowired
    PersonaDbService(PersonaMapper personaMapper) {
        super(personaMapper);
    }

    // ===== 业务扩展操作 =====
}
