package com.surstudio.cts.attempt.application;

import com.surstudio.cts.assessment.domain.*;
import com.surstudio.cts.attempt.domain.*;
import com.surstudio.cts.attempt.dto.SubmitAnswerRequest;
import com.surstudio.cts.common.ConflictException;
import com.surstudio.cts.common.ResourceNotFoundException;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.scoring.FlatScoringStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock AttemptRepository attemptRepository;
    @Mock AnswerRepository answerRepository;
    @Mock ResultRepository resultRepository;
    @Mock SkillTestRepository skillTestRepository;
    @Mock QuestionRepository questionRepository;
    @Mock OptionRepository optionRepository;
    @Mock UserRepository userRepository;
    @Spy  FlatScoringStrategy scoringStrategy;
    @InjectMocks AttemptService service;

    @Test
    void startAttempt_createsAttemptForActiveTest() {
        var test = activeTest(1L);
        var saved = attempt(10L, test);
        when(skillTestRepository.findById(1L)).thenReturn(Optional.of(test));
        when(attemptRepository.save(any())).thenReturn(saved);

        var result = service.startAttempt(1L, new AppUser());

        assertThat(result.attemptId()).isEqualTo(10L);
        assertThat(result.testId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
    }

    @Test
    void startAttempt_throwsWhenTestNotFound() {
        when(skillTestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startAttempt(99L, new AppUser()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void startAttempt_throwsWhenTestInactive() {
        var test = activeTest(1L);
        test.setActive(false);
        when(skillTestRepository.findById(1L)).thenReturn(Optional.of(test));

        assertThatThrownBy(() -> service.startAttempt(1L, new AppUser()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void submitAnswer_savesNewAnswer() {
        var test = activeTest(1L);
        var att = attempt(10L, test);
        var question = question(4L, test);
        var option = option(14L, question, true);

        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));
        when(questionRepository.findById(4L)).thenReturn(Optional.of(question));
        when(optionRepository.findById(14L)).thenReturn(Optional.of(option));
        when(answerRepository.findByAttemptAndQuestion(att, question)).thenReturn(Optional.empty());
        when(answerRepository.save(any())).thenAnswer(inv -> {
            Answer a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "id", 100L);
            return a;
        });

        var result = service.submitAnswer(10L, new SubmitAnswerRequest(4L, 14L), user(1L));

        assertThat(result.answerId()).isEqualTo(100L);
        assertThat(result.questionId()).isEqualTo(4L);
        assertThat(result.optionId()).isEqualTo(14L);
    }

    @Test
    void submitAnswer_updatesExistingAnswer() {
        var test = activeTest(1L);
        var att = attempt(10L, test);
        var question = question(4L, test);
        var oldOption = option(13L, question, false);
        var newOption = option(14L, question, true);
        var existing = new Answer();
        existing.setAttempt(att);
        existing.setQuestion(question);
        existing.setOption(oldOption);
        ReflectionTestUtils.setField(existing, "id", 100L);

        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));
        when(questionRepository.findById(4L)).thenReturn(Optional.of(question));
        when(optionRepository.findById(14L)).thenReturn(Optional.of(newOption));
        when(answerRepository.findByAttemptAndQuestion(att, question)).thenReturn(Optional.of(existing));
        when(answerRepository.save(any())).thenReturn(existing);

        var result = service.submitAnswer(10L, new SubmitAnswerRequest(4L, 14L), user(1L));

        assertThat(result.optionId()).isEqualTo(14L);
        verify(answerRepository).save(existing);
    }

    @Test
    void submitAnswer_throwsWhenAttemptAlreadySubmitted() {
        var test = activeTest(1L);
        var att = attempt(10L, test);
        att.setStatus(AttemptStatus.SUBMITTED);
        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));

        assertThatThrownBy(() -> service.submitAnswer(10L, new SubmitAnswerRequest(4L, 14L), user(1L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void submitAnswer_throwsWhenQuestionNotInTest() {
        var test = activeTest(1L);
        var otherTest = activeTest(2L);
        var att = attempt(10L, test);
        var questionFromOtherTest = question(4L, otherTest);

        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));
        when(questionRepository.findById(4L)).thenReturn(Optional.of(questionFromOtherTest));

        assertThatThrownBy(() -> service.submitAnswer(10L, new SubmitAnswerRequest(4L, 14L), user(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to this test");
    }

    @Test
    void submitAnswer_throwsWhenUserIsNotOwner() {
        var att = attempt(10L, activeTest(1L));
        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));

        // att owner = user(1L), requester = user(2L)
        assertThatThrownBy(() -> service.submitAnswer(10L, new SubmitAnswerRequest(4L, 14L), user(2L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attempt not found");
    }

    @Test
    void submitAttempt_calculatesScoreServerSide() {
        var test = activeTest(1L);
        var question1 = question(4L, test);
        var question2 = question(5L, test);
        test.getQuestions().add(question1);
        test.getQuestions().add(question2);

        var att = attempt(10L, test);
        var answer1 = answerWith(att, question1, option(14L, question1, true));
        var answer2 = answerWith(att, question2, option(15L, question2, false));

        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));
        when(answerRepository.findByAttempt(att)).thenReturn(List.of(answer1, answer2));
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.submitAttempt(10L, user(1L));

        assertThat(result.scorePct().doubleValue()).isEqualTo(50.0);
        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.status()).isEqualTo(AttemptStatus.SUBMITTED);
        assertThat(result.submittedAt()).isNotNull();
        verify(resultRepository).save(any(Result.class));
    }

    @Test
    void submitAttempt_throwsWhenAlreadySubmitted() {
        var test = activeTest(1L);
        var att = attempt(10L, test);
        att.setStatus(AttemptStatus.SUBMITTED);
        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));

        assertThatThrownBy(() -> service.submitAttempt(10L, user(1L)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void submitAttempt_throwsWhenUserIsNotOwner() {
        var att = attempt(10L, activeTest(1L));
        when(attemptRepository.findById(10L)).thenReturn(Optional.of(att));

        // att owner = user(1L), requester = user(2L)
        assertThatThrownBy(() -> service.submitAttempt(10L, user(2L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attempt not found");
    }

    // --- helpers ---

    private SkillTest activeTest(Long id) {
        var t = new SkillTest();
        ReflectionTestUtils.setField(t, "id", id);
        t.setSkill(Skill.ACROBACIA);
        t.setTitle("Test");
        t.setActive(true);
        return t;
    }

    private Attempt attempt(Long id, SkillTest test) {
        var a = new Attempt();
        ReflectionTestUtils.setField(a, "id", id);
        a.setSkillTest(test);
        a.setUser(user(1L));
        return a;
    }

    private AppUser user(Long id) {
        var u = new AppUser();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private Question question(Long id, SkillTest test) {
        var q = new Question();
        ReflectionTestUtils.setField(q, "id", id);
        q.setSkillTest(test);
        q.setText("Q?");
        return q;
    }

    private Option option(Long id, Question question, boolean correct) {
        var o = new Option();
        ReflectionTestUtils.setField(o, "id", id);
        o.setQuestion(question);
        o.setText("Option");
        o.setCorrect(correct);
        return o;
    }

    private Answer answerWith(Attempt attempt, Question question, Option option) {
        var a = new Answer();
        a.setAttempt(attempt);
        a.setQuestion(question);
        a.setOption(option);
        return a;
    }
}
