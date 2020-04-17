package net.consensys.eventeum.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Mostly taken from org.springframework.boot.autoconfigure.condition.OnExpressionCondition
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnRocketRequiredCondition extends OnMultiExpressionCondition {

    private static final String ROCKET_REQUIRED_EXPRESSION =
            "'${broadcaster.multiInstance}' == 'true' || '${broadcaster.type}' == 'ROCKETMQ'";

    private static final String ROCKET_NOT_REQUIRED_EXPRESSION =
            "'${broadcaster.multiInstance}' == 'false' && '${broadcaster.type}' != 'ROCKETMQ'";

    public OnRocketRequiredCondition() {
        super(ROCKET_REQUIRED_EXPRESSION, ROCKET_NOT_REQUIRED_EXPRESSION, ConditionalOnRocketRequired.class);
    }
}