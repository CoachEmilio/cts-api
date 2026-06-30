package com.surstudio.cts.onboarding.api;

import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.onboarding.application.OnboardingService;
import com.surstudio.cts.onboarding.dto.GeneralInfoResponse;
import com.surstudio.cts.onboarding.dto.SaveGeneralInfoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/general-information")
public class GeneralInfoController {

    private final OnboardingService onboardingService;

    public GeneralInfoController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping
    public List<GeneralInfoResponse> getOptions() {
        return onboardingService.getGeneralInfo();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CANDIDATE')")
    public void saveAnswers(@AuthenticationPrincipal AppUser user,
                             @RequestBody SaveGeneralInfoRequest request) {
        onboardingService.saveGeneralInfo(user, request);
    }
}