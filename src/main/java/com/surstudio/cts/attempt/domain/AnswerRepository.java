package com.surstudio.cts.attempt.domain;

import com.surstudio.cts.assessment.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByAttemptAndQuestion(Attempt attempt, Question question);

    List<Answer> findByAttempt(Attempt attempt);
}
