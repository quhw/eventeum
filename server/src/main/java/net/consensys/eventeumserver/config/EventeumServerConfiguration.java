package net.consensys.eventeumserver.config;

import net.consensys.eventeum.annotation.ConditionalOnRocketRequired;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableKafkaDeadLetter(topics = {"#{eventeumKafkaSettings.eventeumEventsTopic}"},
//                       containerFactoryBeans = {"kafkaListenerContainerFactory", "eventeumKafkaListenerContainerFactory"},
//                       serviceId = "eventeum")
@ConditionalOnRocketRequired
public class EventeumServerConfiguration {
}
