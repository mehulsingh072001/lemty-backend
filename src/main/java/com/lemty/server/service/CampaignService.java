package com.lemty.server.service;

import java.util.*;

import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Emails;
import com.lemty.server.domain.Engagement;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.helpers.PlaceholderHelper;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.EmailsRepository;
import com.lemty.server.repo.EngagementRepository;
import com.lemty.server.repo.ProspectMetadataRepository;
import com.lemty.server.repo.ProspectRepository;
import com.lemty.server.repo.UserRepo;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CampaignService {
    Logger logger = LoggerFactory.getLogger(CampaignService.class);

    private final Scheduler scheduler;
    private final CampaignRepository campaignRepository;
    private final ProspectService prospectService;
    private final StepService stepService;
    private final UserRepo userRepo;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final ProspectRepository prospectRepository;
    private final EngagementRepository engagementRepository;
    private final EmailsRepository emailsRepository;
    private final PlaceholderHelper placeholderHelper;

    @Autowired
    public CampaignService(Scheduler scheduler, CampaignRepository campaignRepository, ProspectService prospectService, UserRepo userRepo, StepService stepService, ProspectMetadataRepository prospectMetadataRepository, ProspectRepository prospectRepository, EngagementRepository engagementRepository, EmailsRepository emailsRepository, PlaceholderHelper placeholderHelper){

        this.scheduler = scheduler;
        this.campaignRepository = campaignRepository;
        this.prospectService = prospectService;
        this.userRepo = userRepo;
        this.stepService = stepService;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.prospectRepository = prospectRepository;
        this.engagementRepository = engagementRepository;
        this.emailsRepository = emailsRepository;
        this.placeholderHelper = placeholderHelper;
    }

    //List all campaigns
    public List<Campaign> getCampaign(String userId){
        //AppUser appUser = userRepo.findById(userId).get();
        return campaignRepository.findByAppUserId(userId, Sort.by("createdAt"));
    }

    //List Single Campaign
    public Campaign getSingleCampaign(String campaignId){
        return campaignRepository.findById(campaignId).get();
    }

    //set total prospects
    public void setTotalProspects(String campaignId, Integer prospectCount){
        Campaign campaign = campaignRepository.findById(campaignId).get();
        campaign.setProspectCount(prospectCount);
    }

    //Add new Campaign
    public void addNewCampaign(Campaign  newCampaign, String userId){
        Campaign campaign = campaignRepository.findByName(newCampaign.getCampaign_name());
        if(campaign != null){
            throw new IllegalStateException("Campaign Exists");
        }
        AppUser appUser = userRepo.findById(userId).get();
        newCampaign.setAppUser(appUser);
        //List<Map<String, Object>> steps = newCampaign.getSteps();
        newCampaign.setStatus("Not Started");
        newCampaign.setProspectCount(0);
        campaignRepository.saveAndFlush(newCampaign);
        //stepService.addNewStep(steps, newCampaign.getId());
    }


    public void updateCampaignSettings(Campaign newCampaign, String campaignId){
        Campaign campaign = campaignRepository.findById(campaignId).get();

        campaign.setTimezone(newCampaign.getTimezone());
        campaign.setCampaignStop(newCampaign.getCampaignStop());
        campaign.setDailyLimit(newCampaign.getDailyLimit());

        campaignRepository.save(campaign);
    }

    public void updateCampaignName(Campaign newCampaign, String campaignId){
        Campaign campaign = campaignRepository.findById(campaignId).get();
        campaign.setCampaign_name(newCampaign.getCampaign_name());
        campaignRepository.save(campaign);
    }

    public void updateCampaignSteps(Campaign campaign, String campaignId){
        Campaign existingCampaign = campaignRepository.findById(campaignId).get();
        existingCampaign.setSteps(campaign.getSteps());
        campaignRepository.save(existingCampaign);
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        logger.info(String.valueOf(steps));
        for(int i=0; i < steps.size(); i++){
            Map<String, Object> step = steps.get(i);
            List<Emails> emails = emailsRepository.findByCampaignIdAndStep(campaignId, i);
            logger.info(String.valueOf(emails));
            List<Map<String, Object>> mails = stepService.getMailsFromSteps(campaignId, i);
            List<Emails> updatedEmails = new ArrayList<>();
            for(Emails email : emails){
                logger.info(String.valueOf(email));
                Prospect prospect = email.getProspect();
                String from = step.get("whichEmail").toString();
                String subject = (String) mails.get(email.getMail()).get("subject").getClass().cast(mails.get(i % mails.size()).get("subject"));
                String body = (String) mails.get(email.getMail()).get("body").getClass().cast(mails.get(i % mails.size()).get("body"));
                subject = placeholderHelper.fieldsReplacer(subject, prospect);
                body = placeholderHelper.fieldsReplacer(body, prospect);
                body = placeholderHelper.bodyLinkReplacer(body, email.getId());

                email.setFromEmail(from);
                email.setSubject(subject);
                email.setBody(body);
                updatedEmails.add(email);
            }
            emailsRepository.saveAll(updatedEmails);
        }
    }

    public void deleteCampaign(String campaignId){
        boolean exists = campaignRepository.existsById(campaignId);
        if(!exists){
            throw new IllegalStateException(
                    "campaign with id " + campaignId + " does not exists"
            );
        }
        List<Engagement> engagements = engagementRepository.findByCampaignId(campaignId);

        if(engagements.size() != 0){
            for(Engagement engagement : engagements){
                engagement.setProspectMetadata(null);
                engagement.setCampaign(null);
                engagementRepository.save(engagement);
            }
        }

        prospectService.removeProspectFromCampaign(campaignId);
        List<Emails> emails = emailsRepository.findByCampaignId(campaignId);
        for(int i=0; i < emails.size(); i++){
            Emails emails2 = emails.get(i);
            if(emails2.getStatus() == "SENT"){
                emails2.setCampaign(null);
            }
            else{
                emailsRepository.delete(emails2);
            }
        }

        logger.info("fdk");
        try {
            Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(campaignId));
            List<JobKey> jobKeys = new ArrayList<JobKey>(keys);
            logger.info(String.valueOf(jobKeys));
            scheduler.deleteJobs(jobKeys);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        Campaign campaign = campaignRepository.findById(campaignId).get();
        campaignRepository.delete(campaign);
    }

    public void pauseCampaign(String campaignId){
        try {
            Campaign campaign = campaignRepository.findById(campaignId).get();
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals(campaignId));
            campaign.setStatus("Paused");
            campaignRepository.save(campaign);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void resumeCampaign(String campaignId){
        try {
            scheduler.resumeJobs(GroupMatcher.jobGroupEquals(campaignId));
            Campaign campaign = campaignRepository.findById(campaignId).get();
            campaign.setStatus("Active");
            campaignRepository.save(campaign);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getProspectCountsStatus(String campaignId){
        Map<String, Integer> prospectCounts = new HashMap<>();
        Pageable paging = PageRequest.of(0, Integer.MAX_VALUE);

        Page<ProspectMetadata> pageProspects;
        pageProspects = prospectMetadataRepository.findByCampaignId(campaignId, paging);
        Page<ProspectMetadata> inCampaign= prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "IN_CAMPAIGN", paging);
        Page<ProspectMetadata> completedWithoutReply = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "COMPLETED_WITHOUT_REPLY", paging);
        Page<ProspectMetadata> unsubscribed = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "UNSUBSCRIBED",paging);
        Page<ProspectMetadata> bounced = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "BOUNCED", paging);
        Page<ProspectMetadata> replied = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "REPLIED", paging);
        Page<ProspectMetadata> stopped = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "STOPPED", paging);

        prospectCounts.put("all", pageProspects.getContent().size());
        prospectCounts.put("inCampaign", inCampaign.getContent().size());
        prospectCounts.put("completedWithoutReply", completedWithoutReply.getContent().size());
        prospectCounts.put("unsubscribed", unsubscribed.getContent().size());
        prospectCounts.put("bounced", bounced.getContent().size());
        prospectCounts.put("replied", replied.getContent().size());
        prospectCounts.put("stopped", stopped.getContent().size());

        return prospectCounts;
    }

    public Map<String, Object> generateCampaignProspects(String campaignId, int page, int size){
        List<Map<String, Object>> prospectDatas = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<ProspectMetadata> pageProspectMetadatas = prospectMetadataRepository.findByCampaignId(campaignId, paging);
        List<ProspectMetadata> prospectMetadatas = pageProspectMetadatas.getContent();

        for(int i=0; i < prospectMetadatas.size(); i++){
            ProspectMetadata prospectMetadata = prospectMetadatas.get(i);
            List<Engagement> engagements = engagementRepository.findByProspectMetadataId(prospectMetadata.getId());
            Map<String, Object> prospectData = new HashMap<>();
            prospectData.put("lastCompletedStep", prospectMetadata.getLastCompletedStep());
            prospectData.put("prospect", prospectMetadata.getProspect());
            prospectData.put("status", prospectMetadata.getStatus());
            prospectData.put("engagements", engagements);
            prospectDatas.add(prospectData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("prospectDatas", prospectDatas);
        response.put("currentPage", pageProspectMetadatas.getNumber());
        response.put("totalElements", pageProspectMetadatas.getTotalElements());
        response.put("totalPages", pageProspectMetadatas.getTotalPages());
        return response;
    }

    public Map<String, Object> generateCampaignProspectsByStatus(String campaignId, String status, int page, int size){
        List<Map<String, Object>> prospectDatas = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<ProspectMetadata> pageProspectMetadatas;
        List<ProspectMetadata> prospectMetadatas;
        if(Objects.equals(status, "ALL")){
            pageProspectMetadatas = prospectMetadataRepository.findByCampaignId(campaignId, paging);
            prospectMetadatas = pageProspectMetadatas.getContent();
        }
        else{
            pageProspectMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, status, paging);
            prospectMetadatas = pageProspectMetadatas.getContent();
        }

        for(int i=0; i < prospectMetadatas.size(); i++){
            ProspectMetadata prospectMetadata = prospectMetadatas.get(i);
            List<Engagement> engagements = engagementRepository.findByProspectMetadataId(prospectMetadata.getId());
            Map<String, Object> prospectData = new HashMap<>();
            prospectData.put("lastCompletedStep", prospectMetadata.getLastCompletedStep());
            prospectData.put("prospect", prospectMetadata.getProspect());
            prospectData.put("status", prospectMetadata.getStatus());
            prospectData.put("engagements", engagements);
            prospectDatas.add(prospectData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("prospectDatas", prospectDatas);
        response.put("currentPage", pageProspectMetadatas.getNumber());
        response.put("totalElements", pageProspectMetadatas.getTotalElements());
        response.put("totalPages", pageProspectMetadatas.getTotalPages());
        return response;
    }
}
