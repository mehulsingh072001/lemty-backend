package com.lemty.server.service;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Mail;
import com.lemty.server.helpers.StartDateHelper;
import com.lemty.server.jobPayload.CampaignPayload;
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
    private final MailJobService mailJobService;

    public StepJobService(Scheduler scheduler, StepService stepService, ProspectService prospectService, CampaignRepository campaignRepository, DeliveribilitySettingsService deliveribilitySettingsService, StartDateHelper startDateHelper, MailJobService mailJobService) {
        this.scheduler = scheduler;
        this.stepService = stepService;
        this.prospectService = prospectService;
        this.campaignRepository = campaignRepository;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
        this.startDateHelper = startDateHelper;
        this.mailJobService = mailJobService;
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
            nextStepIndex = 0;
        }
        else{
            nextStepIndex = stepIndex + 1;
        }
        ZonedDateTime startDate = ZonedDateTime.now().withZoneSameLocal(ZoneId.of(campaign.getTimezone()));
        mailJobService.runStep(payload.getSelectedProspects(), campaignId, stepIndex, nextStepIndex, stepNumber, userId, startDate);
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
