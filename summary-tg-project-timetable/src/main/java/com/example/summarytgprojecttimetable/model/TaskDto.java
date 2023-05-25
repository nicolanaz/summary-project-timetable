package com.example.summarytgprojecttimetable.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long id;
    private UserData user;
    private String task;
    private LocalDateTime haveToDoTime;
    private boolean isCompleted;
    private LocalDateTime completedTime;
}
