package net.consensys.eventeum.config;

import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.RocketSettings;
import net.consensys.eventeum.integration.broadcast.internal.DoNothingEventeumEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.EventeumEventBroadcaster;
import net.consensys.eventeum.integration.broadcast.internal.RocketEventeumEventBroadcaster;
import net.consensys.eventeum.integration.consumer.EventeumInternalEventConsumer;
import net.consensys.eventeum.integration.consumer.RocketFilterEventConsumer;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.TransactionMonitoringService;
import net.consensys.eventeum.utils.RocketTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring bean configuration for the FilterEvent broadcaster and consumer.
 * <p>
 * If broadcaster.multiInstance is set to true, then register a Kafka broadcaster,
 * otherwise register a dummy broadcaster that does nothing.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class EventeumEventConfiguration {

//    @Bean
//    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="true")
//    public EventeumEventBroadcaster kafkaFilterEventBroadcaster(KafkaTemplate<String, EventeumMessage> kafkaTemplate,
//                                                                KafkaSettings kafkaSettings) {
//        return new KafkaEventeumEventBroadcaster(kafkaTemplate, kafkaSettings);
//    }
//
//    @Bean
//    @ConditionalOnProperty(name="broadcaster.multiInstance", havingValue="true")
//    public EventeumInternalEventConsumer kafkaFilterEventConsumer(SubscriptionService subscriptionService,
//                                                                  TransactionMonitoringService transactionMonitoringService,
//                                                                  KafkaSettings kafkaSettings) {
//        return new KafkaFilterEventConsumer(subscriptionService, transactionMonitoringService, kafkaSettings);
//    }

    @Bean
    @ConditionalOnProperty(name = "broadcaster.multiInstance", havingValue = "true")
    public EventeumEventBroadcaster rocketFilterEventBroadcaster(RocketTemplate<String, EventeumMessage> rocketTemplate,
                                                                 RocketSettings rocketSettings) {
        return new RocketEventeumEventBroadcaster(rocketTemplate, rocketSettings);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnProperty(name = "broadcaster.multiInstance", havingValue = "true")
    public EventeumInternalEventConsumer rocketFilterEventConsumer(SubscriptionService subscriptionService,
                                                                   TransactionMonitoringService transactionMonitoringService,
                                                                   RocketSettings rocketSettings) {
        return new RocketFilterEventConsumer(subscriptionService, transactionMonitoringService, rocketSettings);
    }

    @Bean
    @ConditionalOnProperty(name = "broadcaster.multiInstance", havingValue = "false")
    public EventeumEventBroadcaster doNothingFilterEventBroadcaster() {
        return new DoNothingEventeumEventBroadcaster();
    }
}
