package com.lemty.server.repo;

import com.lemty.server.domain.ProspectMetadata;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProspectMetadataRepository extends JpaRepository<ProspectMetadata, String> {
    List<ProspectMetadata> findByCampaignId(String campaignId);

    ProspectMetadata findByProspectIdAndCampaignId(String prospectId, String campaignId);
    List<ProspectMetadata> findByCampaignIdAndStatus(String campaignId, String status);
    Page<ProspectMetadata> findByCampaignIdAndBouncedTrue(String campaignId, Pageable pageable);
    Page<ProspectMetadata> findByCampaignIdAndContactedFalse(String campaignId, Pageable pageable);
    Page<ProspectMetadata> findByCampaignIdAndRepliedTrue(String campaignId, Pageable pageable);
    Page<ProspectMetadata> findByCampaignIdAndRepliedFalse(String campaignId, Pageable pageable);
    Page<ProspectMetadata> findByCampaignIdAndUnsubscribedTrue(String campaignId, Pageable pageable);
    void deleteAllByCampaignId(String campaignId);
}
