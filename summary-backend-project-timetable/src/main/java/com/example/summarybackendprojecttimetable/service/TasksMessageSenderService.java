package com.example.summarybackendprojecttimetable.service;

import com.example.summarybackendprojecttimetable.mapper.TaskMapper;
import com.example.summarybackendprojecttimetable.model.Task;
import com.example.summarybackendprojecttimetable.model.TaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TasksMessageSenderService {
    private TaskService taskService;
    private TaskMapper taskMapper;
    private RabbitTemplate rabbitTemplate;
    private ObjectMapper objectMapper;

    public TasksMessageSenderService(TaskService taskService, TaskMapper taskMapper, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.rabbitTemplate = rabbitTemplate;

        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void sendReminder(Task task) {
        try {
            TaskDto taskDto = taskMapper.taskToDto(task);
            String data = objectMapper.writeValueAsString(taskDto);
            rabbitTemplate.convertAndSend("task_reminders", data);
        } catch (JsonProcessingException exception) {
            log.warn(String.format("Exception in converting task (id: %d)", task.getId()), exception);
        }

        log.info(String.format("Task (id: %d) reminder sent", task.getId()));
    }

    public void sendStatistics() {
        Map<Long, List<TaskDto>> completedTasks = taskMapper.toDtoMap(taskService.getAllCompletedTasksPerDay());

        try {
            String data = objectMapper.writeValueAsString(completedTasks);
            rabbitTemplate.convertAndSend("task_statistics", data);
        } catch (JsonProcessingException exception) {
            log.warn("Exception in converting daily statistics", exception);
        }

        log.info("Statistics sent");
    }
}
