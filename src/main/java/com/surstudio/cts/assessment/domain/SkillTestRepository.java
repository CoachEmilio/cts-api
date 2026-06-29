package com.surstudio.cts.assessment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillTestRepository extends JpaRepository<SkillTest, Long> {

    List<SkillTest> findByActiveTrue();
}
