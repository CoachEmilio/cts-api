package com.surstudio.cts.attempt.domain;

import com.surstudio.cts.assessment.domain.SkillTest;
import com.surstudio.cts.identity.domain.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "attempt")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_test_id", nullable = false)
    private SkillTest skillTest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "deadline")
    private Instant deadline;

    @Column(name = "violations_count", nullable = false)
    private int violationsCount = 0;

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public SkillTest getSkillTest() { return skillTest; }
    public void setSkillTest(SkillTest skillTest) { this.skillTest = skillTest; }
    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }
    public int getViolationsCount() { return violationsCount; }
    public void setViolationsCount(int violationsCount) { this.violationsCount = violationsCount; }
}
