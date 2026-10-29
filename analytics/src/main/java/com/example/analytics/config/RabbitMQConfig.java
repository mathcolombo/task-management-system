package com.example.analytics.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.analytics}")
    private String analyticsQueue;

    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;

    @Bean
    public TopicExchange taskExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue analyticsQueue() {
        return new Queue(analyticsQueue, true);
    }

    @Bean
    public Binding analyticsBinding() {
        return BindingBuilder.bind(analyticsQueue())
                .to(taskExchange())
                .with(createdRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
