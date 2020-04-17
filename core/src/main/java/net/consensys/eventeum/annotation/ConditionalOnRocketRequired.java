package net.consensys.eventeum.annotation;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Configuration annotation for a conditional element that depends on RocketMQ being required
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnRocketRequiredCondition.class)
public @interface ConditionalOnRocketRequired {

    boolean value() default true;
}
