package com.lemty.server.repo;

import java.util.Date;
import java.util.List;

import com.lemty.server.domain.Emails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailsRepository extends JpaRepository<Emails, String>{
    List<Emails> findByCampaignId(String campaignId);
    List<Emails> findByCampaignIdAndStatus(String campaignId, String status);
    Emails findByCampaignIdAndProspectId(String campaignId, String prospectId);
    List<Emails> findByAppUserIdAndSentDateTime(String appUserId, Date sentDateTime);
}
