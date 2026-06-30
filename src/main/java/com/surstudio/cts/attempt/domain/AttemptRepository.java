package com.surstudio.cts.attempt.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByUserIdAndStatus(Long userId, AttemptStatus status);
    boolean existsByUserIdAndSkillTestIdAndStatus(Long userId, Long skillTestId, AttemptStatus status);
    boolean existsByUserIdAndSkillTestId(Long userId, Long skillTestId);

    @Query("SELECT a.skillTest.id FROM Attempt a WHERE a.user.id = :userId AND a.status = :status")
    Set<Long> findTestIdsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AttemptStatus status);
}