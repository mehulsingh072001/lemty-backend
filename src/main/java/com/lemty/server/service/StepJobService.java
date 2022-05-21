package com.lemty.server.service;

import com.lemty.server.domain.Campaign;
import com.lemty.server.helpers.StartDateHelper;
import com.lemty.server.jobPayload.CampaignPayload;
import com.lemty.server.jobPayload.StepPayload;
import com.lemty.server.jobs.StepJob;
import com.lemty.server.repo.CampaignRepository;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class StepJobService {
    Logger logger = LoggerFactory.getLogger(StepJobService.class);
    private final Scheduler scheduler;
    private final StepService stepService;
    private final ProspectService prospectService;
    private final CampaignRepository campaignRepository;
    private final DeliveribilitySettingsService deliveribilitySettingsService;
    private final StartDateHelper startDateHelper;

    public StepJobService(Scheduler scheduler, StepService stepService, ProspectService prospectService, CampaignRepository campaignRepository, DeliveribilitySettingsService deliveribilitySettingsService, StartDateHelper startDateHelper) {
        this.scheduler = scheduler;
        this.stepService = stepService;
        this.prospectService = prospectService;
        this.campaignRepository = campaignRepository;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
        this.startDateHelper = startDateHelper;
    }

    public void createCampaignJob(CampaignPayload payload, String userId) {
        String campaignId = payload.getSelectedCampaign();
        Campaign campaign = campaignRepository.findById(campaignId).get();
        campaign.setStatus("Active");
        campaignRepository.save(campaign);
        prospectService.addMultipleProspectsToCampaign(payload.getSelectedProspects(), campaignId);

        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));

        Map<String, Object> initialStep = steps.get(0);
        Integer stepNumber = (Integer) initialStep.get("stepNumber").getClass().cast(initialStep.get("stepNumber"));
        Integer stepIndex = 0;
        Integer nextStepIndex;
        if((stepIndex + 1) == steps.size()){
            nextStepIndex = null;
        }
        else{
            nextStepIndex = stepIndex + 1;
        }

        try{
            JobDetail jobDetail = buildStepJobDetail(payload.getSelectedProspects(), campaignId, stepIndex, nextStepIndex, stepNumber, userId);
            Trigger trigger = buildTrigger(jobDetail, campaignId);
            scheduler.scheduleJob(jobDetail, trigger);
        }
        catch(SchedulerException e){
            e.printStackTrace();
        }

    }

    private JobDetail buildStepJobDetail(List<String> prospectIds, String campaignId, Integer stepIndex, Integer nextStepIndex, Integer stepNumber, String userId){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("prospectIds", prospectIds);
        jobDataMap.put("campaignId", campaignId);
        jobDataMap.put("nextStepIndex", nextStepIndex);
        jobDataMap.put("stepIndex", stepIndex);
        jobDataMap.put("userId", userId);
        return JobBuilder.newJob(StepJob.class)
             .withIdentity(UUID.randomUUID().toString(), stepIndex + "-" + campaignId)
             .withDescription("Run Step Job")
             .storeDurably()
             .usingJobData(jobDataMap)
             .build();
    }

 	private Trigger buildTrigger(JobDetail jobDetail, String campaignId){
 		return TriggerBuilder.newTrigger()
 			.forJob(jobDetail)
			.withIdentity(jobDetail.getKey().getName(), campaignId)
 			.withDescription("Step Scheduler")
            .startAt(Date.from(Instant.now()))
 			.build();
 	}
}
