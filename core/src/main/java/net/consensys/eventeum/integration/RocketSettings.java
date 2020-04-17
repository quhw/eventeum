package net.consensys.eventeum.integration;

import lombok.Data;
import net.consensys.eventeum.annotation.ConditionalOnRocketRequired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * An encapsulation of RocketMQ related properties.
 *
 * @author quhuanwen
 */
@Component("eventeumRocketMQSettings")
@ConditionalOnRocketRequired
@Data
public class RocketSettings {

    @Value("${rocket.name.addresses}")
    private String nameAddresses;

    @Value("${rocket.topic.contractEvents}")
    private String contractEventsTopic;

    @Value("${rocket.topic.blockEvents}")
    private String blockEventsTopic;

    @Value("${rocket.topic.eventeumEvents}")
    private String eventeumEventsTopic;

    @Value("${rocket.topic.transactionEvents}")
    private String transactionEventsTopic;

    @Value("${rocket.groupId}")
    private String groupId;
}
