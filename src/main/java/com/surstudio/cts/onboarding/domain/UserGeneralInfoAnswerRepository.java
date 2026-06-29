package com.surstudio.cts.onboarding.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGeneralInfoAnswerRepository extends JpaRepository<UserGeneralInfoAnswer, Long> {
    void deleteByUserId(Long userId);
}