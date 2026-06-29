package com.surstudio.cts.onboarding.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GeneralInfoCategoryRepository extends JpaRepository<GeneralInfoCategory, Long> {

    @Query("SELECT c FROM GeneralInfoCategory c ORDER BY c.position ASC")
    List<GeneralInfoCategory> findAllOrdered();
}