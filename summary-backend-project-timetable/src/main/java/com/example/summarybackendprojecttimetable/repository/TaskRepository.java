package com.example.summarybackendprojecttimetable.repository;

import com.example.summarybackendprojecttimetable.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Transactional
    @Modifying
    @Query("update Task t " +
            "set t.isCompleted = true, t.completedTime = :date " +
            "where t.id = :id")
    void completeTask(@Param("id") Long id,
                      @Param("date") LocalDateTime completedTime);

    List<Task> findAllByUser_UserIdOrderByHaveToDoTime(Long userId);
}
