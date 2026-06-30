package com.surstudio.cts.identity.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.Collection;
import java.util.List;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    Optional<CandidateProfile> findByUserId(Long userId);
    List<CandidateProfile> findByUserIdIn(Collection<Long> userIds);
}