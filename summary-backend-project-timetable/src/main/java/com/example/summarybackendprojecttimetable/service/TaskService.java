package com.example.summarybackendprojecttimetable.service;

import com.example.summarybackendprojecttimetable.model.Task;
import com.example.summarybackendprojecttimetable.model.UserData;
import com.example.summarybackendprojecttimetable.repository.TaskRepository;
import com.example.summarybackendprojecttimetable.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TaskService {
    private TaskRepository taskRepository;
    private UserRepository userRepository;

    public String saveTask(Task task) {
        Long userId = task.getUser().getUserId();
        Optional<UserData> userData = userRepository.findById(userId);

        userData.ifPresent(task::setUser);

        taskRepository.save(task);

        log.info(String.format("Task saved (id: %d)", task.getId()));
        return "Task saved: " + task.getTask();
    }

    public String taskCompleted(Long id) {
        LocalDateTime localDateTime = LocalDateTime.now();
        taskRepository.completeTask(id, localDateTime);

        log.info(String.format("Task (id: %d) completed", id));

        Task task = taskRepository.findById(id).get();
        return String.format("Task (id: %d) completed ✅", task.getId());
    }

    public List<Task> getTasksForDay(Long userId) {
        List<Task> result = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (Task task : taskRepository.findAllByUser_UserIdOrderByHaveToDoTime(userId)) {
            if (task.getHaveToDoTime().toLocalDate().isEqual(today) && !task.isCompleted()) {
                result.add(task);
            }
        }

        return result;
    }

    public Map<Long, List<Task>> getAllCompletedTasksPerDay() {
        Map<Long, List<Task>> result = new HashMap<>();

        for (Long userId : userRepository.getAllUserIds()) {
            List<Task> completedTasks = taskRepository.findAllByUser_UserIdOrderByHaveToDoTime(userId)
                    .stream()
                    .filter(task -> task.isCompleted() && task.getCompletedTime().toLocalDate().isEqual(LocalDate.now()))
                    .collect(Collectors.toList());

            result.put(userId, completedTasks);
            taskRepository.deleteAll(completedTasks);
        }

        return result;
    }

}
