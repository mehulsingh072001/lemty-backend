package com.lemty.server.repo;

import com.lemty.server.domain.ProspectMetadata;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProspectMetadataRepository extends JpaRepository<ProspectMetadata, String> {
    Page<ProspectMetadata> findByCampaignId(String campaignId, Pageable page);
    ProspectMetadata findByProspectIdAndCampaignId(String prospectId, String campaignId);
    Page<ProspectMetadata> findByCampaignIdAndStatusIs(String campaignId, String status, Pageable pageable);
    void deleteAllByCampaignId(String campaignId);
}
