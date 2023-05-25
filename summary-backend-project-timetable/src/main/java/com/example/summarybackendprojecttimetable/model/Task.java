package com.example.summarybackendprojecttimetable.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private UserData user;
    private String task;
    private LocalDateTime haveToDoTime;
    private boolean isCompleted;
    private LocalDateTime completedTime;

    public Task(String task, String haveToDoTime) {
        this.task = task;
        this.haveToDoTime = LocalDateTime.parse(haveToDoTime);
    }
}
