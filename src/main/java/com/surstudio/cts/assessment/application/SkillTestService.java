package com.surstudio.cts.assessment.application;

import com.surstudio.cts.assessment.domain.*;
import com.surstudio.cts.assessment.dto.*;
import com.surstudio.cts.attempt.domain.AttemptRepository;
import com.surstudio.cts.attempt.domain.AttemptStatus;
import com.surstudio.cts.common.ResourceNotFoundException;
import com.surstudio.cts.identity.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillTestService {

    private final SkillTestRepository skillTestRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;

    public SkillTestService(SkillTestRepository skillTestRepository,
                            QuestionRepository questionRepository,
                            AttemptRepository attemptRepository) {
        this.skillTestRepository = skillTestRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    public SkillTestAdminResponse createTest(SkillTestRequest request) {
        var test = new SkillTest();
        test.setSkill(request.skill());
        test.setTitle(request.title());
        if (request.active() != null) test.setActive(request.active());
        if (request.durationMinutes() != null) test.setDurationMinutes(request.durationMinutes());
        return toAdminResponse(skillTestRepository.save(test));
    }

    public SkillTestAdminResponse updateTest(Long id, SkillTestRequest request) {
        var test = requireTest(id);
        test.setSkill(request.skill());
        test.setTitle(request.title());
        if (request.active() != null) test.setActive(request.active());
        if (request.durationMinutes() != null) test.setDurationMinutes(request.durationMinutes());
        return toAdminResponse(test);
    }

    public SkillTestAdminResponse.QuestionDto addQuestion(Long testId, QuestionRequest request) {
        var test = requireTest(testId);
        var question = new Question();
        question.setSkillTest(test);
        question.setText(request.text());
        question.setPosition(request.position());
        request.options().forEach(o -> {
            var option = new Option();
            option.setQuestion(question);
            option.setText(o.text());
            option.setCorrect(o.correct());
            option.setPosition(o.position());
            question.getOptions().add(option);
        });
        return toQuestionDto(questionRepository.save(question));
    }

    public SkillTestAdminResponse.QuestionDto updateQuestion(Long questionId, QuestionRequest request) {
        var question = requireQuestion(questionId);
        question.setText(request.text());
        question.setPosition(request.position());
        question.getOptions().clear();
        request.options().forEach(o -> {
            var option = new Option();
            option.setQuestion(question);
            option.setText(o.text());
            option.setCorrect(o.correct());
            option.setPosition(o.position());
            question.getOptions().add(option);
        });
        return toQuestionDto(questionRepository.saveAndFlush(question));
    }

    public void deleteQuestion(Long questionId) {
        questionRepository.delete(requireQuestion(questionId));
    }

    @Transactional(readOnly = true)
    public List<SkillTestCandidateView> listActiveTests(AppUser user) {
        return skillTestRepository.findByActiveTrue().stream()
                .map(t -> toCandidateView(t, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillTestCandidateView getTestForCandidate(Long id, AppUser user) {
        var test = skillTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SkillTest not found: " + id));
        return toCandidateView(test, user);
    }

    private SkillTest requireTest(Long id) {
        return skillTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SkillTest not found: " + id));
    }

    private Question requireQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    private SkillTestAdminResponse toAdminResponse(SkillTest test) {
        return new SkillTestAdminResponse(
                test.getId(), test.getSkill(), test.getTitle(), test.isActive(),
                test.getDurationMinutes(),
                test.getQuestions().stream().map(this::toQuestionDto).toList()
        );
    }

    private SkillTestAdminResponse.QuestionDto toQuestionDto(Question q) {
        return new SkillTestAdminResponse.QuestionDto(
                q.getId(), q.getText(), q.getPosition(),
                q.getOptions().stream()
                        .map(o -> new SkillTestAdminResponse.OptionDto(o.getId(), o.getText(), o.isCorrect(), o.getPosition()))
                        .toList()
        );
    }

    private SkillTestCandidateView toCandidateView(SkillTest test, AppUser user) {
        boolean completed = user != null && attemptRepository
                .existsByUserIdAndSkillTestIdAndStatus(user.getId(), test.getId(), AttemptStatus.SUBMITTED);
        return new SkillTestCandidateView(
                test.getId(), test.getSkill(), test.getTitle(), test.getDurationMinutes(), completed,
                test.getQuestions().stream()
                        .map(q -> new SkillTestCandidateView.QuestionDto(
                                q.getId(), q.getText(), q.getPosition(),
                                q.getOptions().stream()
                                        .map(o -> new SkillTestCandidateView.OptionDto(o.getId(), o.getText(), o.getPosition()))
                                        .toList()
                        ))
                        .toList()
        );
    }
}