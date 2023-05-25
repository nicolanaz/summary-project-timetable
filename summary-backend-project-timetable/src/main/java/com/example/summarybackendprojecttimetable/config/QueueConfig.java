package com.example.summarybackendprojecttimetable.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    private RabbitTemplate rabbitTemplate;

    public QueueConfig(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public void queueDeclare() {
        rabbitTemplate.execute(channel ->
                channel.queueDeclare("task_statistics", true, false, false, null));
        rabbitTemplate.execute(channel ->
                channel.queueDeclare("task_reminders", true, false, false, null));
    }
}
