package com.example.summarybackendprojecttimetable.repository;

import com.example.summarybackendprojecttimetable.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserData, Long> {
    @Query("select u.userId from UserData u")
    List<Long> getAllUserIds();
}
