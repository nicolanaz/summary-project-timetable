package com.example.summarytgprojecttimetable.service;

import com.example.summarytgprojecttimetable.model.TaskDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@EnableRabbit
@Slf4j
public class TaskMessageReceiverService {
    private PersonalTimetableBot bot;
    private ObjectMapper objectMapper;

    public TaskMessageReceiverService(PersonalTimetableBot bot) {
        this.bot = bot;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @RabbitListener(queues = "task_statistics")
    public void receiveStatistics(String statistics) {
        log.info("Statistics received");

        try {
            Map<Long, List<TaskDto>> completedTasks = objectMapper.readValue(statistics, new TypeReference<Map<Long, List<TaskDto>>>() {});

            for (Map.Entry<Long, List<TaskDto>> pair : completedTasks.entrySet()) {
                bot.sendStatistics(pair.getKey(), pair.getValue());
            }
        } catch (Exception e) {

        }
    }

    @RabbitListener(queues = "task_reminders")
    public void receiveReminders(String reminder) {
        log.info("Reminder received");

        try {
            TaskDto taskDto = objectMapper.readValue(reminder, TaskDto.class);
            bot.sendReminder(taskDto.getUser().getUserId(), taskDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
