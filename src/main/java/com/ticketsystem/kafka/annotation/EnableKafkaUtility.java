package com.ticketsystem.kafka.annotation;

import org.springframework.context.annotation.Import;

import com.ticketsystem.kafka.utility.KafkaUtilityAutoConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(KafkaUtilityAutoConfiguration.class)
public @interface EnableKafkaUtility {
}
