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
import java.util.Map;
import java.util.stream.Collectors;

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

        // Bulk fetch profiles — one query instead of N
        var userIds = results.stream()
                .map(r -> r.getAttempt().getUser().getId())
                .distinct()
                .toList();
        Map<Long, String> displayNameByUserId = profileRepository.findByUserIdIn(userIds)
                .stream()
                .filter(p -> p.getDisplayName() != null && !p.getDisplayName().isBlank())
                .collect(Collectors.toMap(p -> p.getUser().getId(), CandidateProfile::getDisplayName));

        return results.stream().map(r -> {
            var attempt = r.getAttempt();
            var user = attempt.getUser();
            var skillTest = attempt.getSkillTest();
            String displayName = displayNameByUserId.getOrDefault(user.getId(), user.getEmail());

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