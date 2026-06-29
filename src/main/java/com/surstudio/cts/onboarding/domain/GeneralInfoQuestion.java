package com.surstudio.cts.onboarding.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "general_info_question")
public class GeneralInfoQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private GeneralInfoCategory category;

    @Column(nullable = false)
    private String question;

    @Column(name = "answer_type", nullable = false, length = 20)
    private String answerType = "SINGLE";

    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<GeneralInfoOption> answers = new ArrayList<>();

    public Long getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswerType() { return answerType; }
    public int getPosition() { return position; }
    public List<GeneralInfoOption> getAnswers() { return answers; }
}