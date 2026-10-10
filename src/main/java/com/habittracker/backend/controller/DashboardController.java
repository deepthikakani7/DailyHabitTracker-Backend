package com.habittracker.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.habittracker.backend.entity.Habit;
import com.habittracker.backend.entity.HabitCompletion;
import com.habittracker.backend.service.HabitCompletionService;
import com.habittracker.backend.service.HabitService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class DashboardController {

    private final HabitService habitService;
    private final HabitCompletionService completionService;

    public DashboardController(
            HabitService habitService,
            HabitCompletionService completionService) {

        this.habitService = habitService;
        this.completionService = completionService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(
            Authentication authentication) {

        try {

            String email = authentication.getName();

            List<Habit> habits =
                    habitService.getUserHabits(email);

            int totalHabits = habits.size();

            int activeHabits = 0;
            int completedToday = 0;

            int bestCurrentStreak = 0;
            int bestLongestStreak = 0;

            double totalCompletionPercentage = 0.0;
            double totalDailyPercentage = 0.0;
            double totalWeeklyPercentage = 0.0;
            double totalMonthlyPercentage = 0.0;

            int calculatedHabits = 0;

            for (Habit habit : habits) {

                if (!habit.isActive()) {
                    continue;
                }

                activeHabits++;

                Long habitId = habit.getId();

                // ---------------------------------------------
                // COMPLETION HISTORY
                // ---------------------------------------------

                List<HabitCompletion> history =
                        completionService
                                .getCompletionHistory(
                                        habitId,
                                        email
                                );

                boolean todayCompleted =
                        history.stream()
                                .anyMatch(completion ->
                                        completion
                                                .getCompletionDate()
                                                .equals(
                                                        java.time.LocalDate
                                                                .now()
                                                )
                                                && completion.isCompleted()
                                );

                if (todayCompleted) {
                    completedToday++;
                }

                // ---------------------------------------------
                // CURRENT STREAK
                // ---------------------------------------------

                int currentStreak =
                        completionService.getCurrentStreak(
                                habitId,
                                email
                        );

                bestCurrentStreak =
                        Math.max(
                                bestCurrentStreak,
                                currentStreak
                        );

                // ---------------------------------------------
                // LONGEST STREAK
                // ---------------------------------------------

                int longestStreak =
                        completionService.getLongestStreak(
                                habitId,
                                email
                        );

                bestLongestStreak =
                        Math.max(
                                bestLongestStreak,
                                longestStreak
                        );

                // ---------------------------------------------
                // OVERALL COMPLETION %
                // ---------------------------------------------

                double completionPercentage =
                        completionService
                                .getCompletionPercentage(
                                        habitId,
                                        email
                                );

                totalCompletionPercentage +=
                        completionPercentage;

                // ---------------------------------------------
                // DAILY PROGRESS
                // ---------------------------------------------

                Map<String, Object> daily =
                        completionService.getProgress(
                                habitId,
                                email,
                                "daily"
                        );

                totalDailyPercentage +=
                        ((Number) daily.get("percentage"))
                                .doubleValue();

                // ---------------------------------------------
                // WEEKLY PROGRESS
                // ---------------------------------------------

                Map<String, Object> weekly =
                        completionService.getProgress(
                                habitId,
                                email,
                                "weekly"
                        );

                totalWeeklyPercentage +=
                        ((Number) weekly.get("percentage"))
                                .doubleValue();

                // ---------------------------------------------
                // MONTHLY PROGRESS
                // ---------------------------------------------

                Map<String, Object> monthly =
                        completionService.getProgress(
                                habitId,
                                email,
                                "monthly"
                        );

                totalMonthlyPercentage +=
                        ((Number) monthly.get("percentage"))
                                .doubleValue();

                calculatedHabits++;
            }

            // ---------------------------------------------
            // CALCULATE AVERAGES
            // ---------------------------------------------

            double overallPercentage = 0.0;
            double dailyPercentage = 0.0;
            double weeklyPercentage = 0.0;
            double monthlyPercentage = 0.0;

            if (calculatedHabits > 0) {

                overallPercentage =
                        round(
                                totalCompletionPercentage
                                        / calculatedHabits
                        );

                dailyPercentage =
                        round(
                                totalDailyPercentage
                                        / calculatedHabits
                        );

                weeklyPercentage =
                        round(
                                totalWeeklyPercentage
                                        / calculatedHabits
                        );

                monthlyPercentage =
                        round(
                                totalMonthlyPercentage
                                        / calculatedHabits
                        );
            }

            // ---------------------------------------------
            // RESPONSE
            // ---------------------------------------------

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "totalHabits",
                    totalHabits
            );

            response.put(
                    "activeHabits",
                    activeHabits
            );

            response.put(
                    "completedToday",
                    completedToday
            );

            response.put(
                    "currentStreak",
                    bestCurrentStreak
            );

            response.put(
                    "longestStreak",
                    bestLongestStreak
            );

            response.put(
                    "completionPercentage",
                    overallPercentage
            );

            response.put(
                    "dailyProgress",
                    dailyPercentage
            );

            response.put(
                    "weeklyProgress",
                    weeklyPercentage
            );

            response.put(
                    "monthlyProgress",
                    monthlyPercentage
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}