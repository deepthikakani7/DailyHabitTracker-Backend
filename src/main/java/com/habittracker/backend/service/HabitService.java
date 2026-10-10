package com.habittracker.backend.service;

import com.habittracker.backend.dto.HabitRequest;
import com.habittracker.backend.entity.Habit;
import com.habittracker.backend.entity.User;
import com.habittracker.backend.repository.HabitRepository;
import com.habittracker.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public HabitService(
            HabitRepository habitRepository,
            UserRepository userRepository) {

        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    public Habit createHabit(
            HabitRequest request,
            String email) {

        User user = getUser(email);

        validateHabitName(request.getName());

        Habit habit = new Habit();

        habit.setName(request.getName().trim());
        habit.setDescription(request.getDescription());
        habit.setUser(user);
        habit.setActive(true);

        return habitRepository.save(habit);
    }

    public List<Habit> getUserHabits(String email) {

        User user = getUser(email);

        return habitRepository.findByUserId(user.getId());
    }

    public Habit getHabit(Long habitId, String email) {

        User user = getUser(email);

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() ->
                        new RuntimeException("Habit not found"));

        if (!habit.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return habit;
    }

    public Habit updateHabit(
            Long habitId,
            HabitRequest request,
            String email) {

        Habit habit = getHabit(habitId, email);

        validateHabitName(request.getName());

        habit.setName(request.getName().trim());
        habit.setDescription(request.getDescription());

        return habitRepository.save(habit);
    }

    public void deleteHabit(
            Long habitId,
            String email) {

        Habit habit = getHabit(habitId, email);

        habitRepository.delete(habit);
    }

    private void validateHabitName(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Habit name is required");
        }
    }
}