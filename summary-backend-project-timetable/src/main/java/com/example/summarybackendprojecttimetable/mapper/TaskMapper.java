package com.example.summarybackendprojecttimetable.mapper;

import com.example.summarybackendprojecttimetable.model.Task;
import com.example.summarybackendprojecttimetable.model.TaskDto;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    Task dtoToTask(TaskDto taskDto);

    TaskDto taskToDto(Task task);

    List<TaskDto> toDtoList(List<Task> taskList);

    List<Task> toTaskList(List<TaskDto> taskDtoList);

    default Map<Long, List<TaskDto>> toDtoMap(Map<Long, List<Task>> taskMap) {
        return taskMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toDtoList(entry.getValue())
                ));

    }

    default Map<Long, List<Task>> toTaskMap(Map<Long, List<TaskDto>> taskDtoMap) {
        return taskDtoMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toTaskList(entry.getValue())
                ));
    }
}
