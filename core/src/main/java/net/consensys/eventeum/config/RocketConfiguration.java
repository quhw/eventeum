package net.consensys.eventeum.config;

import net.consensys.eventeum.annotation.ConditionalOnRocketRequired;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.RocketSettings;
import net.consensys.eventeum.utils.RocketTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for RocketMQ related beans.
 *
 * @author quhuanwen
 */
@Configuration("eventeumRocketConfiguration")
@ConditionalOnRocketRequired
public class RocketConfiguration {
    @Autowired
    private RocketSettings settings;

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public RocketTemplate<String, EventeumMessage> eventeumRocketTemplate() {
        return new RocketTemplate<>(settings);
    }

}
