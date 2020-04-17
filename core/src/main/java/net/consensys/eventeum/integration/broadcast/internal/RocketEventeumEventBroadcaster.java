package net.consensys.eventeum.integration.broadcast.internal;

import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.integration.RocketSettings;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import net.consensys.eventeum.utils.JSON;
import net.consensys.eventeum.utils.RocketTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An EventeumEventBroadcaster that broadcasts the events to a RocketMQ queue.
 * <p>
 * The topic name can be configured via the rocket.topic.eventeumEvents property.
 *
 * @author quhuanwen
 */
public class RocketEventeumEventBroadcaster implements EventeumEventBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(RocketEventeumEventBroadcaster.class);

    private RocketSettings rocketSettings;
    private RocketTemplate<String, EventeumMessage> rocketTemplate;

    public RocketEventeumEventBroadcaster(RocketTemplate<String, EventeumMessage> rocketTemplate,
                                          RocketSettings rocketSettings) {
        this.rocketTemplate = rocketTemplate;
        this.rocketSettings = rocketSettings;
    }

    @Override
    public void broadcastEventFilterAdded(ContractEventFilter filter) {
        sendMessage(createContractEventFilterAddedMessage(filter));
    }

    @Override
    public void broadcastEventFilterRemoved(ContractEventFilter filter) {
        sendMessage(createContractEventFilterRemovedMessage(filter));
    }

    @Override
    public void broadcastTransactionMonitorAdded(TransactionMonitoringSpec spec) {
        sendMessage(createTransactionMonitorAddedMessage(spec));
    }

    @Override
    public void broadcastTransactionMonitorRemoved(TransactionMonitoringSpec spec) {
        sendMessage(createTransactionMonitorRemovedMessage(spec));
    }

    protected EventeumMessage createContractEventFilterAddedMessage(ContractEventFilter filter) {
        return new ContractEventFilterAdded(filter);
    }

    protected EventeumMessage createContractEventFilterRemovedMessage(ContractEventFilter filter) {
        return new ContractEventFilterRemoved(filter);
    }

    protected EventeumMessage createTransactionMonitorAddedMessage(TransactionMonitoringSpec spec) {
        return new TransactionMonitorAdded(spec);
    }

    protected EventeumMessage createTransactionMonitorRemovedMessage(TransactionMonitoringSpec spec) {
        return new TransactionMonitorRemoved(spec);
    }

    private void sendMessage(EventeumMessage message) {
        LOG.info("Sending message: " + JSON.stringify(message));
        rocketTemplate.send(rocketSettings.getEventeumEventsTopic(), message.getId(), message);
    }
}
