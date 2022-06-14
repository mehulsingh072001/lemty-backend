package com.lemty.server.repo;

import com.lemty.server.domain.IntentDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentDetectionRepository extends JpaRepository<IntentDetection, String> {
}
