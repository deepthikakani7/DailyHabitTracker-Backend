package com.habittracker.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habittracker.backend.entity.Habit;

public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByUserId(Long userId);

    List<Habit> findByUserIdAndActiveTrue(Long userId);
}