package com.lemty.server.repo;

import com.lemty.server.domain.DeliveribilitySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveribilitySettingsRepository extends JpaRepository<DeliveribilitySettings, String> {
    DeliveribilitySettings findByAppUserId(String userId);
}
