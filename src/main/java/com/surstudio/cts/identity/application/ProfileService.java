package com.surstudio.cts.identity.application;

import com.surstudio.cts.attempt.domain.AttemptRepository;
import com.surstudio.cts.attempt.domain.AttemptStatus;
import com.surstudio.cts.attempt.domain.ResultRepository;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.CandidateProfile;
import com.surstudio.cts.identity.domain.CandidateProfileRepository;
import com.surstudio.cts.identity.dto.ProfileRequest;
import com.surstudio.cts.identity.dto.ProfileResponse;
import com.surstudio.cts.identity.dto.SkillHistoryEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProfileService {

    private final CandidateProfileRepository profileRepository;
    private final AttemptRepository attemptRepository;
    private final ResultRepository resultRepository;

    public ProfileService(CandidateProfileRepository profileRepository,
                          AttemptRepository attemptRepository,
                          ResultRepository resultRepository) {
        this.profileRepository = profileRepository;
        this.attemptRepository = attemptRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(AppUser user) {
        var profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> emptyProfile(user));
        return toResponse(profile, user);
    }

    public ProfileResponse upsertProfile(AppUser user, ProfileRequest request) {
        var profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    var p = new CandidateProfile();
                    p.setUser(user);
                    return p;
                });
        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        var saved = profileRepository.save(profile);
        return toResponse(saved, user);
    }

    @Transactional(readOnly = true)
    public List<SkillHistoryEntry> getSkillHistory(AppUser user) {
        return resultRepository.findSubmittedByUserId(user.getId(), AttemptStatus.SUBMITTED)
                .stream()
                .map(result -> {
                    var attempt = result.getAttempt();
                    var test = attempt.getSkillTest();
                    return new SkillHistoryEntry(
                            attempt.getId(),
                            test.getSkill(),
                            test.getTitle(),
                            result.getScorePct(),
                            result.getCorrectCount(),
                            result.getTotalCount(),
                            attempt.getSubmittedAt()
                    );
                })
                .toList();
    }

    private CandidateProfile emptyProfile(AppUser user) {
        var p = new CandidateProfile();
        p.setUser(user);
        return p;
    }

    private ProfileResponse toResponse(CandidateProfile profile, AppUser user) {
        return new ProfileResponse(
                profile.getId(),
                user.getEmail(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getUpdatedAt()
        );
    }
}