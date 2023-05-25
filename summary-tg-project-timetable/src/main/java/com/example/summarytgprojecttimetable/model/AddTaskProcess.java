package com.example.summarytgprojecttimetable.model;

import lombok.Data;

@Data
public class AddTaskProcess {
    private Long chatId;
    private boolean isTaskSaved;
    private boolean isTimeSaved;
    private TaskDto taskDto = new TaskDto();

    public AddTaskProcess(Long chatId) {
        this.chatId = chatId;
    }
}
