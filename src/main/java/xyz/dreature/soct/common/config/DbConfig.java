package xyz.dreature.soct.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.dreature.soct.common.model.entity.Persona;
import xyz.dreature.soct.mapper.PersonaMapper;
import xyz.dreature.soct.service.DbService;

// 数据库配置
@Configuration
public class DbConfig {
    // ===== 数据库服务定义 =====
    @Bean
    public DbService<Persona, Long> personaDbService(PersonaMapper personaMapper) {
        return new DbService<>(personaMapper);
    }

}
