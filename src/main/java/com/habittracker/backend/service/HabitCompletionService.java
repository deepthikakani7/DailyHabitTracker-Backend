package com.habittracker.backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.habittracker.backend.dto.CompletionRequest;
import com.habittracker.backend.entity.Habit;
import com.habittracker.backend.entity.HabitCompletion;
import com.habittracker.backend.repository.HabitCompletionRepository;
import com.habittracker.backend.repository.HabitRepository;
import com.habittracker.backend.repository.UserRepository;

@Service
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public HabitCompletionService(
            HabitCompletionRepository completionRepository,
            HabitRepository habitRepository,
            UserRepository userRepository) {

        this.completionRepository = completionRepository;
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // GET USER HABIT
    // =========================================================

    private Habit getUserHabit(
            Long habitId,
            String email) {

        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() ->
                        new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return habit;
    }

    // =========================================================
    // MARK HABIT COMPLETION
    // =========================================================

    public HabitCompletion markCompletion(
            Long habitId,
            CompletionRequest request,
            String email) {

        Habit habit = getUserHabit(habitId, email);

        if (request.getDate() == null) {
            throw new RuntimeException("Date is required");
        }

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new RuntimeException(
                    "Future dates cannot be marked"
            );
        }

        HabitCompletion completion =
                completionRepository
                        .findByHabitIdAndCompletionDate(
                                habitId,
                                request.getDate()
                        )
                        .orElseGet(() -> {

                            HabitCompletion newCompletion =
                                    new HabitCompletion();

                            newCompletion.setHabit(habit);

                            newCompletion.setCompletionDate(
                                    request.getDate()
                            );

                            return newCompletion;
                        });

        completion.setCompleted(
                request.isCompleted()
        );

        return completionRepository.save(completion);
    }

    // =========================================================
    // CURRENT STREAK
    // =========================================================

    public int getCurrentStreak(
            Long habitId,
            String email) {

        getUserHabit(habitId, email);

        List<HabitCompletion> records =
                completionRepository
                        .findByHabitIdOrderByCompletionDateDesc(
                                habitId
                        );

        if (records.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();

        /*
         * Check whether the habit was completed today.
         */
        boolean completedToday =
                records.stream()
                        .anyMatch(record ->
                                record.getCompletionDate()
                                        .equals(today)
                                        && record.isCompleted()
                        );

        /*
         * If today is not completed, start checking
         * from yesterday.
         */
        LocalDate expectedDate =
                completedToday
                        ? today
                        : today.minusDays(1);

        int streak = 0;

        for (HabitCompletion record : records) {

            if (!record.isCompleted()) {
                continue;
            }

            LocalDate date =
                    record.getCompletionDate();

            if (date.equals(expectedDate)) {

                streak++;

                expectedDate =
                        expectedDate.minusDays(1);

            } else if (date.isBefore(expectedDate)) {

                break;
            }
        }

        return streak;
    }

    // =========================================================
    // LONGEST STREAK
    // =========================================================

    public int getLongestStreak(
            Long habitId,
            String email) {

        getUserHabit(habitId, email);

        List<HabitCompletion> records =
                completionRepository
                        .findByHabitIdOrderByCompletionDateDesc(
                                habitId
                        );

        int longest = 0;
        int current = 0;

        LocalDate previousDate = null;

        for (HabitCompletion record : records) {

            if (!record.isCompleted()) {

                current = 0;
                previousDate = null;

                continue;
            }

            LocalDate currentDate =
                    record.getCompletionDate();

            if (previousDate == null ||
                    currentDate.plusDays(1)
                            .equals(previousDate)) {

                current++;

            } else {

                current = 1;
            }

            longest =
                    Math.max(longest, current);

            previousDate = currentDate;
        }

        return longest;
    }

    // =========================================================
    // COMPLETION HISTORY
    // =========================================================

    public List<HabitCompletion> getCompletionHistory(
            Long habitId,
            String email) {

        getUserHabit(habitId, email);

        return completionRepository
                .findByHabitIdOrderByCompletionDateDesc(
                        habitId
                );
    }

    // =========================================================
    // COMPLETION PERCENTAGE
    // =========================================================

    public double getCompletionPercentage(
            Long habitId,
            String email) {

        getUserHabit(habitId, email);

        List<HabitCompletion> records =
                completionRepository
                        .findByHabitIdOrderByCompletionDateDesc(
                                habitId
                        );

        if (records.isEmpty()) {
            return 0.0;
        }

        long completedCount =
                records.stream()
                        .filter(
                                HabitCompletion::isCompleted
                        )
                        .count();

        return Math.round(
                ((double) completedCount
                        / records.size()) * 10000
        ) / 100.0;
    }

    // =========================================================
    // DAILY / WEEKLY / MONTHLY PROGRESS
    // =========================================================

    public Map<String, Object> getProgress(
            Long habitId,
            String email,
            String period) {

        Habit habit =
                getUserHabit(habitId, email);

        LocalDate today =
                LocalDate.now();

        LocalDate startDate;
        LocalDate endDate;

        switch (period.toLowerCase()) {

            // -------------------------------------------------
            // DAILY
            // -------------------------------------------------

            case "daily":

                startDate = today;
                endDate = today;

                break;

            // -------------------------------------------------
            // WEEKLY
            // -------------------------------------------------

            case "weekly":

                startDate =
                        today.with(
                                TemporalAdjusters
                                        .previousOrSame(
                                                DayOfWeek.MONDAY
                                        )
                        );

                /*
                 * Important:
                 * Don't count future days of the week.
                 */
                endDate = today;

                break;

            // -------------------------------------------------
            // MONTHLY
            // -------------------------------------------------

            case "monthly":

                startDate =
                        today.with(
                                TemporalAdjusters
                                        .firstDayOfMonth()
                        );

                /*
                 * Important:
                 * Don't count future days of the month.
                 */
                endDate = today;

                break;

            // -------------------------------------------------
            // INVALID PERIOD
            // -------------------------------------------------

            default:

                throw new RuntimeException(
                        "Period must be daily, weekly, or monthly"
                );
        }

        // =====================================================
        // DON'T COUNT DAYS BEFORE HABIT WAS CREATED
        // =====================================================

        LocalDate habitStartDate =
                habit.getCreatedAt().toLocalDate();

        if (habitStartDate.isAfter(startDate)) {
            startDate = habitStartDate;
        }

        // =====================================================
        // EMPTY PERIOD
        // =====================================================

        if (startDate.isAfter(endDate)) {

            Map<String, Object> emptyResponse =
                    new HashMap<>();

            emptyResponse.put(
                    "period",
                    period.toLowerCase()
            );

            emptyResponse.put(
                    "startDate",
                    startDate
            );

            emptyResponse.put(
                    "endDate",
                    endDate
            );

            emptyResponse.put(
                    "totalDays",
                    0
            );

            emptyResponse.put(
                    "completedDays",
                    0
            );

            emptyResponse.put(
                    "percentage",
                    0.0
            );

            return emptyResponse;
        }

        // =====================================================
        // GET COMPLETION RECORDS
        // =====================================================

        List<HabitCompletion> records =
                completionRepository
                        .findByHabitIdAndCompletionDateBetween(
                                habitId,
                                startDate,
                                endDate
                        );

        // =====================================================
        // COUNT COMPLETED DAYS
        // =====================================================

        long completedDays =
                records.stream()
                        .filter(
                                HabitCompletion::isCompleted
                        )
                        .count();

        // =====================================================
        // TOTAL DAYS
        // =====================================================

        long totalDays =
                java.time.temporal.ChronoUnit.DAYS
                        .between(
                                startDate,
                                endDate
                        ) + 1;

        // =====================================================
        // CALCULATE PERCENTAGE
        // =====================================================

        double percentage =
                totalDays == 0
                        ? 0.0
                        : Math.round(
                                ((double) completedDays
                                        / totalDays)
                                        * 10000
                        ) / 100.0;

        // =====================================================
        // RESPONSE
        // =====================================================

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "period",
                period.toLowerCase()
        );

        response.put(
                "startDate",
                startDate
        );

        response.put(
                "endDate",
                endDate
        );

        response.put(
                "totalDays",
                totalDays
        );

        response.put(
                "completedDays",
                completedDays
        );

        response.put(
                "percentage",
                percentage
        );

        return response;
    }
}