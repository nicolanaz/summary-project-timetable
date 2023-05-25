package com.example.summarybackendprojecttimetable.controller;

import com.example.summarybackendprojecttimetable.mapper.TaskMapper;
import com.example.summarybackendprojecttimetable.model.Task;
import com.example.summarybackendprojecttimetable.model.TaskDto;
import com.example.summarybackendprojecttimetable.service.ScheduleService;
import com.example.summarybackendprojecttimetable.service.TaskService;
import lombok.AllArgsConstructor;
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
    public String addNewTask(@RequestBody TaskDto taskDto) {
        Task task = taskMapper.dtoToTask(taskDto);
        scheduleService.saveTaskReminder(task);
        return taskService.saveTask(task);
    }

    @GetMapping("/{userId}")
    public List<TaskDto> getTasksForDay(@PathVariable("userId") Long userId) {
        return taskMapper.toDtoList(taskService.getTasksForDay(userId));
    }

    @GetMapping("/completed")
    public String completeTask(@RequestParam("id") Long id) {
        return taskService.taskCompleted(id);
    }

}
