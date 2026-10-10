package com.habittracker.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habittracker.backend.entity.HabitCompletion;

public interface HabitCompletionRepository
        extends JpaRepository<HabitCompletion, Long> {

    Optional<HabitCompletion> findByHabitIdAndCompletionDate(
            Long habitId,
            LocalDate completionDate
    );

    List<HabitCompletion> findByHabitIdOrderByCompletionDateDesc(
            Long habitId
    );

    List<HabitCompletion> findByHabitIdAndCompletionDateBetween(
            Long habitId,
            LocalDate startDate,
            LocalDate endDate
    );
}