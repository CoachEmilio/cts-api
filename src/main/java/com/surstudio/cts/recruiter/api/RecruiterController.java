package com.surstudio.cts.recruiter.api;

import com.surstudio.cts.assessment.domain.Skill;
import com.surstudio.cts.recruiter.application.RecruiterService;
import com.surstudio.cts.recruiter.dto.CandidateMatchDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recruiter")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterController {

    private final RecruiterService recruiterService;

    public RecruiterController(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
    }

    @GetMapping("/candidates")
    public List<CandidateMatchDto> searchCandidates(
            @RequestParam Skill skill,
            @RequestParam(defaultValue = "0") BigDecimal minScore) {
        return recruiterService.searchCandidates(skill, minScore);
    }
}