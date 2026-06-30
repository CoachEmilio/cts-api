package com.surstudio.cts.onboarding.application;

import com.surstudio.cts.common.ResourceNotFoundException;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.onboarding.domain.*;
import com.surstudio.cts.onboarding.dto.GeneralInfoResponse;
import com.surstudio.cts.onboarding.dto.SaveGeneralInfoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OnboardingService {

    private final GeneralInfoCategoryRepository categoryRepository;
    private final GeneralInfoQuestionRepository questionRepository;
    private final GeneralInfoOptionRepository optionRepository;
    private final UserGeneralInfoAnswerRepository userGeneralInfoAnswerRepository;

    public OnboardingService(GeneralInfoCategoryRepository categoryRepository,
                             GeneralInfoQuestionRepository questionRepository,
                             GeneralInfoOptionRepository optionRepository,
                             UserGeneralInfoAnswerRepository userGeneralInfoAnswerRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.userGeneralInfoAnswerRepository = userGeneralInfoAnswerRepository;
    }

    public List<GeneralInfoResponse> getGeneralInfo() {
        return categoryRepository.findAllOrdered().stream()
                .map(cat -> new GeneralInfoResponse(
                        cat.getId(),
                        cat.getName(),
                        cat.getQuestions().stream()
                                .map(q -> new GeneralInfoResponse.QuestionDto(
                                        q.getId(),
                                        q.getQuestion(),
                                        q.getAnswerType(),
                                        q.getAnswers().stream()
                                                .map(o -> new GeneralInfoResponse.OptionDto(o.getId(), o.getAnswer()))
                                                .toList()))
                                .toList()))
                .toList();
    }

    @Transactional
    public void saveGeneralInfo(AppUser user, SaveGeneralInfoRequest request) {
        userGeneralInfoAnswerRepository.deleteByUserId(user.getId());
        for (var entry : request.answers()) {
            var question = questionRepository.findById(entry.questionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + entry.questionId()));
            var option = optionRepository.findById(entry.answerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Option not found: " + entry.answerId()));
            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException(
                        "Option " + entry.answerId() + " does not belong to question " + entry.questionId());
            }
            var answer = new UserGeneralInfoAnswer();
            answer.setUser(user);
            answer.setQuestion(question);
            answer.setOption(option);
            userGeneralInfoAnswerRepository.save(answer);
        }
    }
}
