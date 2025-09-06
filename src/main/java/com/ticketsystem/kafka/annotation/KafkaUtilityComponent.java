package com.ticketsystem.kafka.annotation;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface KafkaUtilityComponent {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
