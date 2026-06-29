package com.surstudio.cts.onboarding.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "general_info_option")
public class GeneralInfoOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private GeneralInfoQuestion question;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private int position;

    public Long getId() { return id; }
    public String getAnswer() { return answer; }
    public int getPosition() { return position; }
}