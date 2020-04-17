package net.consensys.eventeum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.KafkaSettings;
import net.consensys.eventeum.integration.PulsarSettings;
import net.consensys.eventeum.integration.RabbitSettings;
import net.consensys.eventeum.integration.RocketSettings;
import net.consensys.eventeum.integration.broadcast.blockchain.*;
import net.consensys.eventeum.utils.RocketTemplate;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;

/**
 * Spring bean configuration for the BlockchainEventBroadcaster.
 * <p>
 * Registers a broadcaster bean based on the value of the broadcaster.type property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class BlockchainEventBroadcasterConfiguration {

    private static final String EXPIRATION_PROPERTY = "${broadcaster.cache.expirationMillis}";
    private static final String BROADCASTER_PROPERTY = "broadcaster.type";
    private static final String ENABLE_BLOCK_NOTIFICATIONS = "${broadcaster.enableBlockNotifications:true}";

    private Long onlyOnceCacheExpirationTime;
    private boolean enableBlockNotifications;

    @Autowired
    public BlockchainEventBroadcasterConfiguration(@Value(EXPIRATION_PROPERTY) Long onlyOnceCacheExpirationTime, @Value(ENABLE_BLOCK_NOTIFICATIONS) boolean enableBlockNotifications) {
        this.onlyOnceCacheExpirationTime = onlyOnceCacheExpirationTime;
        this.enableBlockNotifications = enableBlockNotifications;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "KAFKA")
    public BlockchainEventBroadcaster kafkaBlockchainEventBroadcaster(KafkaTemplate<String, EventeumMessage> kafkaTemplate,
                                                                      KafkaSettings kafkaSettings,
                                                                      CrudRepository<ContractEventFilter, String> filterRepository) {
        final BlockchainEventBroadcaster broadcaster =
                new KafkaBlockchainEventBroadcaster(kafkaTemplate, kafkaSettings, filterRepository);

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "ROCKETMQ")
    public BlockchainEventBroadcaster rocketBlockchainEventBroadcaster(RocketTemplate<String, EventeumMessage> rocketTemplate,
                                                                       RocketSettings rocketSettings,
                                                                       CrudRepository<ContractEventFilter, String> filterRepository) {
        final BlockchainEventBroadcaster broadcaster =
                new RocketBlockchainEventBroadcaster(rocketTemplate, rocketSettings, filterRepository);

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "HTTP")
    public BlockchainEventBroadcaster httpBlockchainEventBroadcaster(HttpBroadcasterSettings settings, RetryTemplate retryTemplate) {
        final BlockchainEventBroadcaster broadcaster =
                new HttpBlockchainEventBroadcaster(settings, retryTemplate);

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "RABBIT")
    public BlockchainEventBroadcaster rabbitBlockChainEventBroadcaster(RabbitTemplate rabbitTemplate, RabbitSettings rabbitSettings) {
        final BlockchainEventBroadcaster broadcaster =
                new RabbitBlockChainEventBroadcaster(rabbitTemplate, rabbitSettings);

        return onlyOnceWrap(broadcaster);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = BROADCASTER_PROPERTY, havingValue = "PULSAR")
    public BlockchainEventBroadcaster pulsarBlockChainEventBroadcaster(PulsarSettings settings, ObjectMapper mapper) throws PulsarClientException {
        final BlockchainEventBroadcaster broadcaster =
                new PulsarBlockChainEventBroadcaster(settings, mapper);

        return onlyOnceWrap(broadcaster);
    }


    private BlockchainEventBroadcaster onlyOnceWrap(BlockchainEventBroadcaster toWrap) {
        return new EventBroadcasterWrapper(onlyOnceCacheExpirationTime, toWrap, enableBlockNotifications);
    }
}
