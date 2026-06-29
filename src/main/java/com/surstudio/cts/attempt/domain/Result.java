package com.surstudio.cts.attempt.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "result")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private Attempt attempt;

    @Column(name = "score_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal scorePct;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public Attempt getAttempt() { return attempt; }
    public void setAttempt(Attempt attempt) { this.attempt = attempt; }
    public BigDecimal getScorePct() { return scorePct; }
    public void setScorePct(BigDecimal scorePct) { this.scorePct = scorePct; }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public Instant getCreatedAt() { return createdAt; }
}
