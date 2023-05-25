package com.example.summarybackendprojecttimetable.service;

import com.example.summarybackendprojecttimetable.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.TreeSet;


@Service
@Slf4j
public class ScheduleService {
    private TasksMessageSenderService tasksMessageSenderService;
    private volatile TreeSet<Task> allTasks;

    public ScheduleService(TasksMessageSenderService tasksMessageSenderService) {
        this.tasksMessageSenderService = tasksMessageSenderService;
        this.allTasks = new TreeSet<>(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o2.getHaveToDoTime().compareTo(o1.getHaveToDoTime());
            }
        });
    }

    @PostConstruct
    public void startMonitoring() {
        Thread tasksTimeChecker = new Thread(new TasksTimeChecker());
        tasksTimeChecker.start();

        log.info("Task time monitoring started");
    }

    public void saveTaskReminder(Task task) {
        allTasks.add(task);
    }

    @Scheduled(cron = "0 30 23 * * ?")
    public void sendStatistics() {
        log.info("Sending statistics...");

        tasksMessageSenderService.sendStatistics();
    }

    private class TasksTimeChecker implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                while (allTasks.isEmpty());

                Task task = allTasks.last();
                if (LocalDateTime.now().isEqual(task.getHaveToDoTime())) {
                    log.info(String.format("Sending task (id: %d) reminder...", task.getId()));

                    tasksMessageSenderService.sendReminder(task);
                    allTasks.pollLast();
                }
            }
        }
    }
}
