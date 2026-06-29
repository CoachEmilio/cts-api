package com.surstudio.cts.attempt.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserIdAndStatus(Long userId, AttemptStatus status);
    boolean existsByUserIdAndSkillTestIdAndStatus(Long userId, Long skillTestId, AttemptStatus status);
}