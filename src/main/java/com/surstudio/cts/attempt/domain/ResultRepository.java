package com.surstudio.cts.attempt.domain;

import com.surstudio.cts.assessment.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByAttempt(Attempt attempt);

    @Query("""
            SELECT r FROM Result r
            JOIN FETCH r.attempt a
            JOIN FETCH a.user u
            JOIN FETCH a.skillTest st
            WHERE st.skill = :skill
              AND r.scorePct >= :minScore
              AND a.status = :status
            ORDER BY r.scorePct DESC
            """)
    List<Result> findBySkillAndMinScore(
            @Param("skill") Skill skill,
            @Param("minScore") BigDecimal minScore,
            @Param("status") AttemptStatus status);
}