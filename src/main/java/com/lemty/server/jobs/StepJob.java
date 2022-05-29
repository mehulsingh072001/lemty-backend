package com.lemty.server.jobs;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.UUID;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.domain.EmailSignature;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.domain.Unsubscribe;
import com.lemty.server.helpers.GmailHelper;
import com.lemty.server.helpers.PlaceholderHelper;
import com.lemty.server.jobPayload.MailRequest;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.ProspectMetadataRepository;
import com.lemty.server.repo.ProspectRepository;
import com.lemty.server.service.DeliveribilitySettingsService;
import com.lemty.server.service.EmailSignatureService;
import com.lemty.server.service.ProspectService;
import com.lemty.server.service.StepService;
import com.lemty.server.service.UnsubscribeService;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

// @PersistJobDataAfterExecution
public class StepJob extends QuartzJobBean {
    Logger logger = LoggerFactory.getLogger(StepJob.class);
    private final GmailHelper gmailHelper;
    private final PlaceholderHelper placeholderHelper;
    private final CampaignRepository campaignRepository;
    private final DeliveribilitySettingsService deliveribilitySettingsService; 
    private final EmailSignatureService emailSignatureService;
    private final UnsubscribeService unsubscribeService;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final Scheduler scheduler;
    private final ProspectRepository prospectRepository;
    private final StepService stepService;

    @Autowired
    private Environment env;

    public StepJob(ProspectService prospectService, GmailHelper gmailHelper, PlaceholderHelper placeholderHelper, DeliveribilitySettingsService deliveribilitySettingsService, CampaignRepository campaignRepository, EmailSignatureService emailSignatureService, UnsubscribeService unsubscribeService, ProspectMetadataRepository prospectMetadataRepository, Scheduler scheduler, StepService stepService, ProspectRepository prospectRepository) {
        this.gmailHelper = gmailHelper;
        this.placeholderHelper = placeholderHelper;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
        this.campaignRepository = campaignRepository;
        this.emailSignatureService = emailSignatureService;
        this.unsubscribeService = unsubscribeService;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.scheduler = scheduler;
        this.stepService = stepService;
        this.prospectRepository = prospectRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        List<String> prospects = (List<String>) jobDataMap.get("prospectIds");
        String campaignId = jobDataMap.getString("campaignId");
        Integer stepIndex = (Integer) jobDataMap.get("stepIndex");
        Integer nextStepIndex = (Integer) jobDataMap.get("nextStepIndex");
        String userId = jobDataMap.getString("userId");
        runStep(prospects, campaignId, userId, stepIndex, nextStepIndex, context);
    }

    //Does all the work
    private void runStep(List<String> prospectIds, String campaignId, String userId, Integer stepIndex, Integer nextStepIndex, JobExecutionContext context){
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        Map<String, Object> step = steps.get(stepIndex);
        // String date = (String) step.get("startDate").getClass().cast(step.get("startDate"));
         // ZonedDateTime startDate = startDateHelper.dateParser(campaign, campaign.getTimezone(), dayGap, hourGap, minuteGap);
        DeliveribilitySettings deliveribilitySettings = deliveribilitySettingsService.getDeliveribilitySettings(userId);
        int minSeconds = deliveribilitySettings.getMinInterval();
        int maxSeconds = deliveribilitySettings.getMaxInterval();
        int seconds = deliveribilitySettings.getSeconds();
        ZoneId zoneId = ZoneId.of(campaign.getTimezone());

        String window = String.valueOf(step.get("startHour")) + "-" + String.valueOf(step.get("endHour"));

        Object days = step.get("days");
        ArrayList list = (ArrayList) days.getClass().cast(days);
        StringJoiner joiner = new StringJoiner(",");
        for(int l = 0; l < list.size(); l++){
           joiner.add(list.get(l).toString());
        }
        String str = joiner.toString();

        Integer afterNextStepIndex = 0;
        if(nextStepIndex != 0){
            if((nextStepIndex + 1) == steps.size()){
                afterNextStepIndex = 0;
            }
            else{
                afterNextStepIndex = nextStepIndex + 1;
            }
        }

        ZonedDateTime currentZonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(campaign.getTimezone()));

        for(int i=0; i < prospectIds.size(); i++){
            Prospect prospect = prospectRepository.findById(prospectIds.get(i)).get();

            Integer interval;

            if(deliveribilitySettings.getEmailInterval().equals("random")){
                Random r = new Random();
                int result = r.nextInt(maxSeconds - minSeconds) + minSeconds;
                interval = result;
            }
            else{
                interval = seconds;
            }

            String nextProspect;
            if((i + 1) < prospectIds.size()){
                nextProspect = prospectIds.get(i + 1);
            }
            else{
                nextProspect = "";
            }

            try {
                JobDetail jobDetail = buildMailJobDetail(campaignId, prospect.getId(), stepIndex, nextProspect, userId, i, nextStepIndex, afterNextStepIndex);
                Trigger trigger = buildMailTrigger(jobDetail, str, campaignId, campaign.getTimezone(), window, currentZonedDateTime);
                scheduler.scheduleJob(jobDetail, trigger);
                scheduler.deleteJob(context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
            currentZonedDateTime = currentZonedDateTime.plusSeconds(interval);
        }
    }

    private JobDetail buildMailJobDetail(String campaignId, String prospectId, Integer stepIndex, String nextProspect, String userId, Integer prospectIndex, Integer nextStepIndex, Integer afterNextStepIndex){
         JobDataMap jobDataMap = new JobDataMap();
         jobDataMap.put("campaignId", campaignId);
         jobDataMap.put("prospectId", prospectId);
         jobDataMap.put("stepIndex", stepIndex);
         jobDataMap.put("nextProspect", nextProspect);
         jobDataMap.put("userId", userId);
         jobDataMap.put("prospectIndex", prospectIndex);
         jobDataMap.put("nextStepIndex", nextStepIndex);
         jobDataMap.put("afterNextStepIndex", afterNextStepIndex);

        return JobBuilder.newJob(MailJob.class)
            .withIdentity(prospectId + "-" + (stepIndex + 1) + "-" + campaignId, campaignId)
            .withDescription("Mail Job")
            .storeDurably()
            .usingJobData(jobDataMap)
            .build();
    }

 	private Trigger buildMailTrigger(JobDetail jobDetail, String days, String campaignId, String timezone,  String window, ZonedDateTime startDate){
 		return TriggerBuilder.newTrigger()
 			.forJob(jobDetail)
			.withIdentity(jobDetail.getKey().getName(), campaignId)
            .startAt(Date.from(startDate.toInstant()))
 			.withDescription("Mail Job")
            .withSchedule(CronScheduleBuilder
                .cronSchedule("5 * " + window + "  ? * " + days)
                .inTimeZone(TimeZone.getTimeZone(timezone))
                .withMisfireHandlingInstructionFireAndProceed()
            )
 			.build();
 	}
}
