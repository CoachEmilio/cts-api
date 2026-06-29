package com.surstudio.cts.attempt.application;

import com.surstudio.cts.assessment.domain.OptionRepository;
import com.surstudio.cts.assessment.domain.QuestionRepository;
import com.surstudio.cts.assessment.domain.SkillTestRepository;
import com.surstudio.cts.attempt.domain.*;
import com.surstudio.cts.attempt.dto.*;
import com.surstudio.cts.common.ConflictException;
import com.surstudio.cts.common.ResourceNotFoundException;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.scoring.ScoringStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@Transactional
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final ResultRepository resultRepository;
    private final SkillTestRepository skillTestRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;
    private final ScoringStrategy scoringStrategy;

    public AttemptService(AttemptRepository attemptRepository,
                          AnswerRepository answerRepository,
                          ResultRepository resultRepository,
                          SkillTestRepository skillTestRepository,
                          QuestionRepository questionRepository,
                          OptionRepository optionRepository,
                          UserRepository userRepository,
                          ScoringStrategy scoringStrategy) {
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.resultRepository = resultRepository;
        this.skillTestRepository = skillTestRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.scoringStrategy = scoringStrategy;
    }

    public StartAttemptResponse startAttempt(Long testId, AppUser user) {
        var test = skillTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("SkillTest not found: " + testId));
        if (!test.isActive()) {
            throw new ConflictException("Test " + testId + " is not active");
        }
        if (attemptRepository.existsByUserIdAndSkillTestIdAndStatus(user.getId(), testId, AttemptStatus.SUBMITTED)) {
            throw new ConflictException("You have already completed this test");
        }
        var attempt = new Attempt();
        attempt.setSkillTest(test);
        attempt.setUser(user);
        var deadline = Instant.now().plusSeconds((long) test.getDurationMinutes() * 60);
        attempt.setDeadline(deadline);
        var saved = attemptRepository.save(attempt);
        return new StartAttemptResponse(saved.getId(), test.getId(), saved.getStatus(), saved.getStartedAt(), saved.getDeadline());
    }

    public void recordViolation(Long attemptId, AppUser user) {
        var attempt = requireAttempt(attemptId);
        requireOwnership(attempt, user);
        requireInProgress(attempt);
        attempt.setViolationsCount(attempt.getViolationsCount() + 1);
    }

    public SubmitAnswerResponse submitAnswer(Long attemptId, SubmitAnswerRequest request, AppUser user) {
        var attempt = requireAttempt(attemptId);
        requireOwnership(attempt, user);
        requireInProgress(attempt);

        var question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + request.questionId()));
        if (!question.getSkillTest().getId().equals(attempt.getSkillTest().getId())) {
            throw new IllegalArgumentException("Question " + request.questionId() + " does not belong to this test");
        }

        var option = optionRepository.findById(request.optionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found: " + request.optionId()));
        if (!option.getQuestion().getId().equals(question.getId())) {
            throw new IllegalArgumentException("Option " + request.optionId() + " does not belong to question " + request.questionId());
        }

        // Upsert: allow changing answer before submitting
        var answer = answerRepository.findByAttemptAndQuestion(attempt, question)
                .orElseGet(() -> {
                    var a = new Answer();
                    a.setAttempt(attempt);
                    a.setQuestion(question);
                    return a;
                });
        answer.setOption(option);
        var saved = answerRepository.save(answer);
        return new SubmitAnswerResponse(saved.getId(), question.getId(), option.getId());
    }

    public AttemptResultResponse submitAttempt(Long attemptId, AppUser user) {
        var attempt = requireAttempt(attemptId);
        requireOwnership(attempt, user);
        requireInProgress(attempt);

        var answers = answerRepository.findByAttempt(attempt);
        int correctCount = (int) answers.stream()
                .filter(a -> a.getOption().isCorrect())
                .count();
        int totalCount = attempt.getSkillTest().getQuestions().size();
        var scorePct = BigDecimal.valueOf(scoringStrategy.score(correctCount, totalCount))
                .setScale(2, RoundingMode.HALF_UP);

        var result = new Result();
        result.setAttempt(attempt);
        result.setScorePct(scorePct);
        result.setCorrectCount(correctCount);
        result.setTotalCount(totalCount);
        resultRepository.save(result);

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(Instant.now());

        return new AttemptResultResponse(
                attempt.getId(), scorePct, correctCount, totalCount,
                AttemptStatus.SUBMITTED, attempt.getSubmittedAt()
        );
    }

    private Attempt requireAttempt(Long id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + id));
    }

    private void requireOwnership(Attempt attempt, AppUser user) {
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Attempt not found: " + attempt.getId());
        }
    }

    private void requireInProgress(Attempt attempt) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ConflictException("Attempt " + attempt.getId() + " is already submitted");
        }
    }
}
