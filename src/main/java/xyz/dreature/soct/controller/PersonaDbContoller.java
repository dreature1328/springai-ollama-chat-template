package xyz.dreature.soct.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.dreature.soct.common.model.entity.Persona;
import xyz.dreature.soct.service.DbService;

// 操作接口（人格数据）
@Slf4j
@RestController
@RequestMapping("/db/persona")
public class PersonaDbContoller extends BaseDbController<Persona, Long> {
    @Autowired
    PersonaDbContoller(DbService<Persona, Long> dbService) {
        super(dbService);
    }
}
