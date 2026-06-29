package com.surstudio.cts.recruiter.application;

import com.surstudio.cts.assessment.domain.Skill;
import com.surstudio.cts.attempt.domain.AttemptStatus;
import com.surstudio.cts.attempt.domain.Result;
import com.surstudio.cts.attempt.domain.ResultRepository;
import com.surstudio.cts.identity.domain.CandidateProfile;
import com.surstudio.cts.identity.domain.CandidateProfileRepository;
import com.surstudio.cts.recruiter.dto.CandidateMatchDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class RecruiterService {

    private final ResultRepository resultRepository;
    private final CandidateProfileRepository profileRepository;

    public RecruiterService(ResultRepository resultRepository,
                            CandidateProfileRepository profileRepository) {
        this.resultRepository = resultRepository;
        this.profileRepository = profileRepository;
    }

    public List<CandidateMatchDto> searchCandidates(Skill skill, BigDecimal minScore) {
        List<Result> results = resultRepository.findBySkillAndMinScore(
                skill, minScore, AttemptStatus.SUBMITTED);

        return results.stream().map(r -> {
            var attempt = r.getAttempt();
            var user = attempt.getUser();
            var skillTest = attempt.getSkillTest();

            String displayName = profileRepository.findByUserId(user.getId())
                    .map(CandidateProfile::getDisplayName)
                    .filter(Objects::nonNull)
                    .orElse(user.getEmail());

            return new CandidateMatchDto(
                    user.getId(),
                    displayName,
                    skillTest.getSkill(),
                    skillTest.getTitle(),
                    r.getScorePct(),
                    r.getCorrectCount(),
                    r.getTotalCount(),
                    attempt.getSubmittedAt()
            );
        }).toList();
    }
}