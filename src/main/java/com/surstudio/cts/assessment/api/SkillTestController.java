package com.surstudio.cts.assessment.api;

import com.surstudio.cts.assessment.application.SkillTestService;
import com.surstudio.cts.assessment.dto.*;
import com.surstudio.cts.identity.domain.AppUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Tests", description = "Skill test bank (ADMIN) and sanitized test view (CANDIDATE)")
public class SkillTestController {

    private final SkillTestService skillTestService;

    public SkillTestController(SkillTestService skillTestService) {
        this.skillTestService = skillTestService;
    }

    @PostMapping("/tests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SkillTestAdminResponse createTest(@Valid @RequestBody SkillTestRequest request) {
        return skillTestService.createTest(request);
    }

    @GetMapping("/tests/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public SkillTestAdminResponse getTestAdmin(@PathVariable Long id) {
        return skillTestService.getTestForAdmin(id);
    }

    @PutMapping("/tests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SkillTestAdminResponse updateTest(
            @PathVariable Long id,
            @Valid @RequestBody SkillTestRequest request) {
        return skillTestService.updateTest(id, request);
    }

    @PostMapping("/tests/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SkillTestAdminResponse.QuestionDto addQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        return skillTestService.addQuestion(id, request);
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SkillTestAdminResponse.QuestionDto updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        return skillTestService.updateQuestion(id, request);
    }

    @DeleteMapping("/tests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTest(@PathVariable Long id) {
        skillTestService.deleteTest(id);
    }

    @DeleteMapping("/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(@PathVariable Long id) {
        skillTestService.deleteQuestion(id);
    }

    @GetMapping("/tests")
    @PreAuthorize("isAuthenticated()")
    public List<SkillTestCandidateView> listTests(@AuthenticationPrincipal AppUser user) {
        return skillTestService.listActiveTests(user);
    }

    @GetMapping("/tests/{id}")
    @PreAuthorize("isAuthenticated()")
    public SkillTestCandidateView getTest(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        return skillTestService.getTestForCandidate(id, user);
    }
}