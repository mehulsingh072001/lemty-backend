package com.lemty.server.service;

import java.util.*;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.repo.CampaignRepository;
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

import javax.transaction.Transactional;

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

    @Autowired
    public CampaignService(Scheduler scheduler, CampaignRepository campaignRepository, ProspectService prospectService, UserRepo userRepo, StepService stepService, ProspectMetadataRepository prospectMetadataRepository, ProspectRepository prospectRepository){
        this.scheduler = scheduler;
        this.campaignRepository = campaignRepository;
        this.prospectService = prospectService;
        this.userRepo = userRepo;
        this.stepService = stepService;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.prospectRepository = prospectRepository;
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

        campaign.setId(campaign.getId());
        campaign.setTimezone(newCampaign.getTimezone());
        campaign.setCampaignStop(newCampaign.getCampaignStop());
        campaign.setProspectCount(newCampaign.getProspectCount());
        campaign.setSteps(newCampaign.getSteps());

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
    }

    @Transactional
    public void deleteCampaign(String campaignId){
        boolean exists = campaignRepository.existsById(campaignId);
        if(!exists){
            throw new IllegalStateException(
                    "campaign with id " + campaignId + " does not exists"
            );
        }
        prospectService.removeProspectFromCampaign(campaignId);

        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        for(int i=0; i < steps.size(); i++){
            try {
                Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(i + "-" +campaignId));
                List<JobKey> jobKeys = new ArrayList<JobKey>(keys);
                scheduler.deleteJobs(jobKeys);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }

        campaignRepository.deleteById(campaignId);
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
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));

        for(int i=0; i < prospectMetadatas.size(); i++){
            ProspectMetadata prospectMetadata = prospectMetadatas.get(i);
            Map<String, Object> prospectData = new HashMap<>();
            prospectData.put("lastCompletedStep", prospectMetadata.getLastCompletedStep());
            prospectData.put("prospect", prospectMetadata.getProspect());
            prospectData.put("status", prospectMetadata.getStatus());
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
