package com.surstudio.cts.attempt.api;

import com.surstudio.cts.attempt.application.AttemptService;
import com.surstudio.cts.attempt.dto.*;
import com.surstudio.cts.identity.domain.AppUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Attempts", description = "Start a test, submit answers, get server-side score")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/tests/{testId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CANDIDATE')")
    public StartAttemptResponse startAttempt(@PathVariable Long testId,
                                             @AuthenticationPrincipal AppUser user) {
        return attemptService.startAttempt(testId, user);
    }

    @PostMapping("/attempts/{attemptId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CANDIDATE')")
    public SubmitAnswerResponse submitAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitAnswerRequest request,
            @AuthenticationPrincipal AppUser user) {
        return attemptService.submitAnswer(attemptId, request, user);
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasRole('CANDIDATE')")
    public AttemptResultResponse submitAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AppUser user) {
        return attemptService.submitAttempt(attemptId, user);
    }

    @PostMapping("/attempts/{attemptId}/violations")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CANDIDATE')")
    public void recordViolation(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal AppUser user) {
        attemptService.recordViolation(attemptId, user);
    }
}
