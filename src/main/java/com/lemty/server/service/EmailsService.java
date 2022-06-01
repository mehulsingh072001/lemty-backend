package com.lemty.server.service;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Emails;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.EmailsRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailsService {
    private final EmailsRepository emailsRepository;
    private final CampaignRepository campaignRepository;

    public EmailsService(EmailsRepository emailsRepository, CampaignRepository campaignRepository) {
        this.emailsRepository = emailsRepository;
        this.campaignRepository = campaignRepository;
    }

    public List<Emails> todayEmails(String campaignId) {
        List<Emails> emails = emailsRepository.findByCampaignId(campaignId);
        List<Emails> todayEmails = new ArrayList<>();
        Campaign campaign = campaignRepository.findById(campaignId).get();
        for(int i=0; i < emails.size(); i++){
            Emails email = emails.get(i);
            ZonedDateTime currentDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(campaign.getTimezone()));
            if(currentDateTime.getDayOfMonth() == email.getStartTime().getDayOfMonth()){
                todayEmails.add(email);
            }
        }
        return todayEmails;
    }

    public List<Emails> upcomingEmails(String campaignId){
        List<Emails> emails = emailsRepository.findByCampaignId(campaignId);
        List<Emails> upcomingEmails = new ArrayList<>();
        Campaign campaign = campaignRepository.findById(campaignId).get();
        for (Emails email : emails) {
            ZonedDateTime currentDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(campaign.getTimezone()));
            if (currentDateTime.getDayOfMonth() == email.getStartTime().getDayOfMonth()) {
                upcomingEmails.add(email);
            }
        }
        return upcomingEmails;
    }

    public Emails getEmailByProspectIdAndCampaignId(String campaignId, String prospectId){
        return emailsRepository.findByCampaignIdAndProspectId(campaignId, prospectId);
    }
}
