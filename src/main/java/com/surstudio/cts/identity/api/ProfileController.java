package com.surstudio.cts.identity.api;

import com.surstudio.cts.identity.application.ProfileService;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.dto.ProfileRequest;
import com.surstudio.cts.identity.dto.ProfileResponse;
import com.surstudio.cts.identity.dto.SkillHistoryEntry;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
@PreAuthorize("hasRole('CANDIDATE')")
@Tag(name = "Profile", description = "Candidate profile and verified skill history")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@AuthenticationPrincipal AppUser user) {
        return profileService.getProfile(user);
    }

    @PutMapping("/profile")
    public ProfileResponse upsertProfile(@AuthenticationPrincipal AppUser user,
                                         @Valid @RequestBody ProfileRequest request) {
        return profileService.upsertProfile(user, request);
    }

    @GetMapping("/skill-history")
    public List<SkillHistoryEntry> getSkillHistory(@AuthenticationPrincipal AppUser user) {
        return profileService.getSkillHistory(user);
    }
}