package com.lemty.server.service;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Emails;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.EmailsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailsService {
    Logger logger = LoggerFactory.getLogger(EmailsService.class);

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

    public List<Map<String, Object>> getSentEmailsCount(String userId, String startDate, String endDate){
        List<Map<String, Object>> response = new ArrayList<>();

        Date startDateTime = Date.from(ZonedDateTime.parse(startDate).toInstant());
        Date endDateTime = Date.from(ZonedDateTime.parse(endDate).toInstant());
        List<Emails> emails = emailsRepository.findByAppUserIdAndSentDateTimeBetween(userId, startDateTime, endDateTime);

        LocalDate localStartDate = startDateTime.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        LocalDate localendDate = endDateTime.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        List<LocalDate> localDates = localStartDate.datesUntil(localendDate).collect(Collectors.toList());

        for(LocalDate localDate : localDates){
            Map<String, Object> data = new HashMap<>();
            Integer count = 0;
            for(Emails email : emails){
                LocalDate sentDate = email.getSentDateTime().toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
                if(sentDate.isEqual(localDate)){
                    count++;
                }
            }
            data.put("date", localDate);
            data.put("prospects", count);
            response.add(data);
        }

        //Add last date data because above method does not get last date
        Map<String, Object> data = new HashMap<>();
        Integer count = 0;
        for(Emails email : emails){
            LocalDate sentDate = email.getSentDateTime().toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
            logger.info(String.valueOf(sentDate));
            if(sentDate.isEqual(localendDate)){
                count++;
            }
        }
        data.put("date", localendDate);
        data.put("prospects", count);
        response.add(data);

        return response;
    }

    public Map<String, Integer> getUserCounts(String userId){
        Map<String, Integer> response = new HashMap<>();
        Integer todayCount = emailsRepository.findByAppUserIdAndStatus(userId, "TODAY").size();
        Integer upcomingCount = emailsRepository.findByAppUserIdAndStatus(userId, "UPCOMING").size();
        Integer sentCount = emailsRepository.findByAppUserIdAndStatus(userId, "SENT").size();

        response.put("today", todayCount);
        response.put("upcoming", upcomingCount);
        response.put("sent", sentCount);
        return response;
    }

    public Integer appUserTodaySentCount(String userId, LocalDate today){
        Integer sentToday = 0;
        List<Emails> emails = emailsRepository.findByAppUserIdAndStatus(userId, "SENT");
        for(Emails email : emails){
            LocalDate sentDate = email.getSentDateTime().toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
            if(sentDate.isEqual(today)){
                sentToday = sentToday + 1;
            }
        }
        return sentToday;
    }

    public Integer campaignTodaySentCount(String campaignId, LocalDate today){
        Integer sentToday = 0;
        List<Emails> emails = emailsRepository.findByCampaignIdAndStatus(campaignId, "SENT");
        for(Emails email : emails){
            LocalDate sentDate = email.getSentDateTime().toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
            if(sentDate.isEqual(today)){
                sentToday = sentToday + 1;
            }
        }
        return sentToday;
    }
}
