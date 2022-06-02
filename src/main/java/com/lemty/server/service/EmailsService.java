package com.lemty.server.service;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Emails;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.EmailsRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailsService {
    private final EmailsRepository emailsRepository;
    private final CampaignRepository campaignRepository;

    public EmailsService(EmailsRepository emailsRepository, CampaignRepository campaignRepository) {
        this.emailsRepository = emailsRepository;
        this.campaignRepository = campaignRepository;
    }

    public Map<String, Integer> getCampaignCounts(String campaignId){
        Map<String, Integer> response = new HashMap<>();
        Integer todayCount = emailsRepository.findByCampaignIdAndStatus(campaignId, "TODAY").size();
        Integer upcomingCount = emailsRepository.findByCampaignIdAndStatus(campaignId, "UPCOMING").size();
        Integer sentCount = emailsRepository.findByCampaignIdAndStatus(campaignId, "SENT").size();

        response.put("today", todayCount);
        response.put("upcoming", upcomingCount);
        response.put("sent", sentCount);
        return response;
    }

    public List<Emails> getByStatusAndCampaign(String campaignId, String status){
        List<Emails> emails = emailsRepository.findByCampaignIdAndStatus(campaignId, status);
        return emails;
    }

    public Emails getEmailByProspectIdAndCampaignId(String campaignId, String prospectId){
        return emailsRepository.findByCampaignIdAndProspectId(campaignId, prospectId);
    }
}
