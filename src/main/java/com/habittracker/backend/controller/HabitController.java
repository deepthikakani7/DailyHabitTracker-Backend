package com.habittracker.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habittracker.backend.dto.HabitRequest;
import com.habittracker.backend.entity.Habit;
import com.habittracker.backend.service.HabitService;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    public ResponseEntity<?> createHabit(
            @RequestBody HabitRequest request,
            Authentication authentication) {

        try {

            Habit habit = habitService.createHabit(
                    request,
                    authentication.getName()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(toResponse(habit));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getHabits(
            Authentication authentication) {

        try {

            List<Habit> habits =
                    habitService.getUserHabits(
                            authentication.getName()
                    );

            return ResponseEntity.ok(
                    habits.stream()
                            .map(this::toResponse)
                            .toList()
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHabit(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            Habit habit = habitService.getHabit(
                    id,
                    authentication.getName()
            );

            return ResponseEntity.ok(toResponse(habit));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHabit(
            @PathVariable Long id,
            @RequestBody HabitRequest request,
            Authentication authentication) {

        try {

            Habit habit = habitService.updateHabit(
                    id,
                    request,
                    authentication.getName()
            );

            return ResponseEntity.ok(toResponse(habit));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            habitService.deleteHabit(
                    id,
                    authentication.getName()
            );

            return ResponseEntity.ok(
                    "Habit deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    private HabitResponse toResponse(Habit habit) {

        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.isActive(),
                habit.getCreatedAt()
        );
    }

    public record HabitResponse(
            Long id,
            String name,
            String description,
            boolean active,
            java.time.LocalDateTime createdAt
    ) {
    }
}