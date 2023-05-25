package com.example.summarytgprojecttimetable.service;

import com.example.summarytgprojecttimetable.model.TaskDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class TaskOutboundService {
    private RestTemplate restTemplate;

    public TaskOutboundService() {
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<String> saveNewTask(TaskDto taskDto) {
        HttpEntity<TaskDto> entity = new HttpEntity<>(taskDto, null);

        log.info(String.format("Sending request to save new task (chat id: %d)", taskDto.getUser().getUserId()));

        return restTemplate.exchange("http://localhost:8080/tasks", HttpMethod.POST, entity, String.class);
    }

    public ResponseEntity<List<TaskDto>> getTasksForDay(Long chatId) {
        String url = String.format("http://localhost:8080/tasks/%d", chatId);
        HttpEntity<List<TaskDto>> entity = new HttpEntity<>(null, null);

        log.info(String.format("Sending request to get tasks for day (chat id: %d)", chatId));

        return restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<TaskDto>>() {});
    }

    public ResponseEntity<String> completeTask(Long taskId) {
        String url = String.format("http://localhost:8080/tasks/completed?id=%d", taskId);
        HttpEntity<String> entity = new HttpEntity<>(null, null);

        log.info(String.format("Sending request to complete task task (chat id: %d)", taskId));

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}
