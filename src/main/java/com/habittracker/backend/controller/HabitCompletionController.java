package com.habittracker.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.habittracker.backend.dto.CompletionRequest;
import com.habittracker.backend.entity.HabitCompletion;
import com.habittracker.backend.service.HabitCompletionService;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class HabitCompletionController {

    private final HabitCompletionService completionService;

    public HabitCompletionController(
            HabitCompletionService completionService) {

        this.completionService = completionService;
    }

    @PutMapping("/{habitId}/completion")
    public ResponseEntity<?> markCompletion(
            @PathVariable Long habitId,
            @RequestBody CompletionRequest request,
            Authentication authentication) {

        try {

            HabitCompletion completion =
                    completionService.markCompletion(
                            habitId,
                            request,
                            authentication.getName()
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("id", completion.getId());
            response.put(
                    "habitId",
                    completion.getHabit().getId()
            );
            response.put(
                    "date",
                    completion.getCompletionDate()
            );
            response.put(
                    "completed",
                    completion.isCompleted()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{habitId}/streak")
    public ResponseEntity<?> getStreak(
            @PathVariable Long habitId,
            Authentication authentication) {

        try {

            String email = authentication.getName();

            int currentStreak =
                    completionService.getCurrentStreak(
                            habitId,
                            email
                    );

            int longestStreak =
                    completionService.getLongestStreak(
                            habitId,
                            email
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "currentStreak",
                    currentStreak
            );

            response.put(
                    "longestStreak",
                    longestStreak
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
    @GetMapping("/{habitId}/completion")
public ResponseEntity<?> getCompletionHistory(
        @PathVariable Long habitId,
        Authentication authentication) {

    try {

        return ResponseEntity.ok(
                completionService.getCompletionHistory(
                        habitId,
                        authentication.getName()
                )
        );

    } catch (RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}

@GetMapping("/{habitId}/completion/percentage")
public ResponseEntity<?> getCompletionPercentage(
        @PathVariable Long habitId,
        Authentication authentication) {

    try {

        double percentage =
                completionService.getCompletionPercentage(
                        habitId,
                        authentication.getName()
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("percentage", percentage);

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}

@GetMapping("/{habitId}/progress")
public ResponseEntity<?> getProgress(
        @PathVariable Long habitId,
        @RequestParam String period,
        Authentication authentication) {

    try {

        Map<String, Object> response =
                completionService.getProgress(
                        habitId,
                        authentication.getName(),
                        period
                );

        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(e.getMessage());
    }
}
}