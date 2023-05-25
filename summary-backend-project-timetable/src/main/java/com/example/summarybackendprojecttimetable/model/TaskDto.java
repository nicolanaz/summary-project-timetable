package com.example.summarybackendprojecttimetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private UserData user;
    private String task;
    private LocalDateTime haveToDoTime;
    private boolean isCompleted;
    private LocalDateTime completedTime;

    public TaskDto(String task, LocalDateTime haveToDoTime) {
        this.task = task;
        this.haveToDoTime = haveToDoTime;
    }
}
