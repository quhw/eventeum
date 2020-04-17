package net.consensys.eventeum.integration.consumer;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.integration.RocketSettings;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.service.SubscriptionService;
import net.consensys.eventeum.service.TransactionMonitoringService;
import net.consensys.eventeum.service.exception.NotFoundException;
import net.consensys.eventeum.utils.JSON;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A FilterEventConsumer that consumes ContractFilterEvents messages from a RocketMQ topic.
 * <p>
 * The topic to be consumed from can be configured via the rocket.topic.contractEvents property.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class RocketFilterEventConsumer implements EventeumInternalEventConsumer, MessageListenerConcurrently {

    private static final Logger logger = LoggerFactory.getLogger(RocketFilterEventConsumer.class);

    private final Map<String, Consumer<EventeumMessage>> messageConsumers;

    private RocketSettings rocketSettings;

    private DefaultMQPushConsumer consumer;


    @Autowired
    public RocketFilterEventConsumer(SubscriptionService subscriptionService,
                                     TransactionMonitoringService transactionMonitoringService,
                                     RocketSettings rocketSettings) {

        messageConsumers = new HashMap<>();
        messageConsumers.put(ContractEventFilterAdded.TYPE, (message) -> {
            subscriptionService.registerContractEventFilter(
                    (ContractEventFilter) message.getDetails(), false);
        });

        messageConsumers.put(ContractEventFilterRemoved.TYPE, (message) -> {
            try {
                subscriptionService.unregisterContractEventFilter(
                        ((ContractEventFilter) message.getDetails()).getId(), false);
            } catch (NotFoundException e) {
                logger.debug("Received filter removed message but filter doesn't exist. (We probably sent message)");
            }
        });

        messageConsumers.put(TransactionMonitorAdded.TYPE, (message) -> {
            transactionMonitoringService.registerTransactionsToMonitor(
                    (TransactionMonitoringSpec) message.getDetails(), false);
        });

        messageConsumers.put(TransactionMonitorRemoved.TYPE, (message) -> {
            try {
                transactionMonitoringService.stopMonitoringTransactions(
                        ((TransactionMonitoringSpec) message.getDetails()).getId(), false);
            } catch (NotFoundException e) {
                logger.debug("Received transaction monitor removed message but monitor doesn't exist. (We probably sent message)");
            }
        });

        this.rocketSettings = rocketSettings;
    }

    public void init() throws Exception {
        consumer = new DefaultMQPushConsumer(rocketSettings.getGroupId());
        consumer.subscribe(rocketSettings.getEventeumEventsTopic(), "*");
        consumer.registerMessageListener(this);
        consumer.start();
    }

    public void destroy() {
        consumer.shutdown();
    }

    @Override
    public void onMessage(EventeumMessage message) {
        final Consumer<EventeumMessage> consumer = messageConsumers.get(message.getType());

        if (consumer == null) {
            logger.error(String.format("No consumer for message type %s!", message.getType()));
            return;
        }

        consumer.accept(message);
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            for (MessageExt msg : msgs) {
                String payload = new String(msg.getBody(), "UTF-8");
                EventeumMessage message = JSON.parseJson(payload, EventeumMessage.class);
                onMessage(message);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            logger.error("Consume message error", e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}
