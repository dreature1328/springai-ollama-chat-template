package xyz.dreature.soct.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.dreature.soct.common.model.entity.Persona;
import xyz.dreature.soct.controller.base.BaseDbController;
import xyz.dreature.soct.service.PersonaDbService;

// 操作接口（人格设定数据库）
@Slf4j
@RestController
@RequestMapping("/db/persona")
@Validated
public class PersonaDbContoller extends BaseDbController<Persona, Long, PersonaDbService> {
    @Autowired
    PersonaDbContoller(PersonaDbService personaDbService) {
        super(personaDbService);
    }
}
