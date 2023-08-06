package com.example.summarybackendprojecttimetable.controller;

import com.example.summarybackendprojecttimetable.mapper.TaskMapper;
import com.example.summarybackendprojecttimetable.model.Task;
import com.example.summarybackendprojecttimetable.model.TaskDto;
import com.example.summarybackendprojecttimetable.service.ScheduleService;
import com.example.summarybackendprojecttimetable.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {
    private TaskService taskService;
    private ScheduleService scheduleService;
    private TaskMapper taskMapper;

    @PostMapping
    public ResponseEntity<String> addNewTask(@RequestBody TaskDto taskDto) {
        Task task = taskMapper.dtoToTask(taskDto);
        scheduleService.saveTaskReminder(task);
        return new ResponseEntity<>(taskService.saveTask(task), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksForDay(@PathVariable("userId") Long userId) {
        List<TaskDto> result = taskMapper.toDtoList(taskService.getTasksForDay(userId));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/completed")
    public ResponseEntity<String> completeTask(@RequestParam("id") Long id) {
        return new ResponseEntity<>(taskService.taskCompleted(id), HttpStatus.OK);
    }
}
