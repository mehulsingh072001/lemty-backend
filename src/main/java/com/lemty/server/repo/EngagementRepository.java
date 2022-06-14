package com.lemty.server.repo;

import com.lemty.server.domain.Engagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngagementRepository extends JpaRepository<Engagement, String> {
    List<Engagement> findByProspectMetadataId(String id);
    List<Engagement> findByCampaignId(String id);
    Engagement findByProspectMetadataIdAndStepNumber(String id, Integer stepNumber);
    void deleteByCampaignId(String campaignId);
}
