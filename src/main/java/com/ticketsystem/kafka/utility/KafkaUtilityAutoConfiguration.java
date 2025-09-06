package com.ticketsystem.kafka.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketsystem.kafka.handler.EventDispatcher;
import com.ticketsystem.kafka.runner.KafkaJobRunner;
import com.ticketsystem.kafka.service.KafkaUtilityService;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConditionalOnClass({ StreamBridge.class, KafkaUtilityService.class })
@ConditionalOnProperty(prefix = "kafka.utility", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@EnableConfigurationProperties(KafkaUtilityProperties.class)
@ComponentScan(basePackages = "com.kafka.utility")
public class KafkaUtilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper kafkaUtilityObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public Map<String, SseEmitter> kafkaUtilitySseEmitters() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTemplate kafkaUtilityRetryTemplate(KafkaUtilityProperties properties) {
        RetryTemplate template = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(properties.getRetry().getBackoffPeriod());
        template.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(properties.getRetry().getMaxAttempts());
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventDispatcher kafkaUtilityEventDispatcher() {
        return new EventDispatcher();
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaJobRunner kafkaUtilityJobRunner() {
        return new KafkaJobRunner();
    }

    // Base KafkaUtilityService bean - can be extended
    @Bean
    @ConditionalOnMissingBean
    public KafkaUtilityService<?> kafkaUtilityService(
            StreamBridge streamBridge,
            KafkaJobRunner jobRunner,
            RetryTemplate retryTemplate,
            EventDispatcher dispatcher,
            Map<String, SseEmitter> emitters) {
        return new KafkaUtilityService<>(streamBridge, jobRunner, retryTemplate, dispatcher, emitters);
    }
}
