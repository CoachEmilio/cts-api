package com.surstudio.cts.onboarding.domain;

import com.surstudio.cts.identity.domain.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "user_general_info_answer",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
public class UserGeneralInfoAnswer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private GeneralInfoQuestion question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private GeneralInfoOption option;

    public Long getId() { return id; }
    public void setUser(AppUser user) { this.user = user; }
    public void setQuestion(GeneralInfoQuestion question) { this.question = question; }
    public void setOption(GeneralInfoOption option) { this.option = option; }
}
