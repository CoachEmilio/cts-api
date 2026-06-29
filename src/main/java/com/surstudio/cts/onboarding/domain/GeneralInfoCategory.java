package com.surstudio.cts.onboarding.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "general_info_category")
public class GeneralInfoCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<GeneralInfoQuestion> questions = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getPosition() { return position; }
    public List<GeneralInfoQuestion> getQuestions() { return questions; }
}