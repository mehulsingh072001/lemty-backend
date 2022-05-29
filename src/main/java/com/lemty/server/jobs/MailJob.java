package com.lemty.server.jobs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

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
import com.lemty.server.service.StepService;
import com.lemty.server.service.UnsubscribeService;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MailJob extends QuartzJobBean{
    Logger logger = LoggerFactory.getLogger(MailJob.class);
    private final GmailHelper gmailHelper;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final Scheduler scheduler;
    private final DeliveribilitySettingsService deliveribilitySettingsService;
    private final StepService stepService;
    private final CampaignRepository campaignRepository;
    private final ProspectRepository prospectRepository;
    private final PlaceholderHelper placeholderHelper;
    private final EmailSignatureService emailSignatureService;
    private final UnsubscribeService unsubscribeService;
    @Autowired
    private Environment env;

    public MailJob(GmailHelper gmailHelper, ProspectMetadataRepository prospectMetadataRepository, Scheduler scheduler, DeliveribilitySettingsService deliveribilitySettingsService, StepService stepService, CampaignRepository campaignRepository, ProspectRepository prospectRepository, PlaceholderHelper placeholderHelper, EmailSignatureService emailSignatureService, UnsubscribeService unsubscribeService){
        this.gmailHelper = gmailHelper;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.scheduler = scheduler;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
        this.stepService = stepService;
        this.campaignRepository = campaignRepository;
        this.prospectRepository = prospectRepository;
        this.placeholderHelper = placeholderHelper;
        this.emailSignatureService = emailSignatureService;
        this.unsubscribeService = unsubscribeService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String prospectId = jobDataMap.getString("prospectId");
        String campaignId = jobDataMap.getString("campaignId");
        Integer stepIndex = (Integer) jobDataMap.get("stepIndex");
        String nextProspect = jobDataMap.getString("nextProspect");
        Integer prospectIndex = (Integer) jobDataMap.get("prospectIndex");
        String userId = jobDataMap.getString("userId");
        Integer nextStepIndex = (Integer) jobDataMap.get("nextStepIndex");
        Integer afterNextStepIndex = (Integer) jobDataMap.get("afterNextStepIndex");

        sendMail(context, prospectId, campaignId, stepIndex, nextProspect, userId, prospectIndex, nextStepIndex, afterNextStepIndex);
    }
    private void sendMail(JobExecutionContext context, String prospectId, String campaignId, Integer stepIndex, String nextProspect, String userId, Integer prospectIndex, Integer nextStepIndex, Integer afterNextStepIndex) {
        //Get data from app
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        ProspectMetadata metadata = prospectMetadataRepository.findByProspectIdAndCampaignId(prospectId, campaignId);
        Prospect prospect = prospectRepository.findById(prospectId).get();
        List<Map<String, Object>> mails = stepService.getMailsFromSteps(campaign.getId(), stepIndex);
        Map<String, Object> step = steps.get(stepIndex);
        List<EmailSignature> signatures = emailSignatureService.getSignatures(userId);
        Unsubscribe unsubscribe = unsubscribeService.getUnsubscribe(userId);

        //Get deliveribilitySettings
        DeliveribilitySettings deliveribilitySettings = deliveribilitySettingsService.getDeliveribilitySettings(userId);
        int minSeconds = deliveribilitySettings.getMinInterval();
        int maxSeconds = deliveribilitySettings.getMaxInterval();
        int seconds = deliveribilitySettings.getSeconds();

        //Mail Data
        String from = step.get("whichEmail").toString();
        String to = prospect.getProspectEmail();

        String subject = (String) mails.get(prospectIndex % mails.size()).get("subject").getClass().cast(mails.get(prospectIndex % mails.size()).get("subject"));
        String body = (String) mails.get(prospectIndex % mails.size()).get("body").getClass().cast(mails.get(prospectIndex % mails.size()).get("body"));
        subject = placeholderHelper.fieldsReplacer(subject, prospect);
        body = placeholderHelper.fieldsReplacer(body, prospect);
        body = placeholderHelper.bodyLinkReplacer(body, prospectId, campaignId, stepIndex, (prospectIndex % mails.size()));

        if(signatures.size() > 0){
            body = placeholderHelper.signatureReplacer(body, signatures.get(0));
        }
        if(unsubscribe != null){
            body = placeholderHelper.unsubLinkReplacer(body, prospect.getId(), unsubscribe);
        }

        String openLink =  env.getProperty("track.url").toString() + "/getAttachment/" + prospectId + "/" + campaignId + "/" + stepIndex  + "/" + (prospectIndex % mails.size());
        body = body + "<img src='" + openLink + "' alt='pixel'>";

        Integer stepNumber = (Integer) step.get("stepNumber").getClass().cast(step.get("stepNumber"));
        String window = String.valueOf(step.get("startHour")) + "-" + String.valueOf(step.get("endHour"));

        Object days = step.get("days");
        ArrayList list = (ArrayList) days.getClass().cast(days);
        StringJoiner joiner = new StringJoiner(",");
        for(int l = 0; l < list.size(); l++){
           joiner.add(list.get(l).toString());
        }
        String daysString = joiner.toString();
        MailRequest mailRequest = new MailRequest(from, subject, to, body);
        String threadId;
        if(stepNumber == 1){
            String newThreadId = gmailHelper.sendMessage(mailRequest);
            metadata.setThreadId(newThreadId);
            metadata.setLastCompletedStep(stepNumber);
            prospectMetadataRepository.save(metadata);
            metadata.setStatus("CONTACTED");
        }
        else{
            threadId = metadata.getThreadId();
            gmailHelper.sendMessageInThread(mailRequest, threadId);
            metadata.setLastCompletedStep(stepNumber);
            prospectMetadataRepository.save(metadata);
        }

        // if(nextProspect != null){
        //     try {
        //         JobKey jobKey = new JobKey(nextProspect + "-" + stepNumber + "-" + campaignId, stepIndex + "-" +campaignId);
        //         logger.info(String.valueOf(jobKey));
        //         JobDetail jobDetail = scheduler.getJobDetail(jobKey);

        //         Trigger trigger = buildTrigger(jobDetail, daysString, campaignId, campaign.getTimezone(), window, Date.from(Instant.now()), interval);
        //         scheduler.scheduleJob(trigger);
        //     } catch (SchedulerException e) {
        //         e.printStackTrace();
        //     }
        // }
        List<String> prospectIds = new ArrayList<>();
        prospectIds.add(prospectId);
        if(nextStepIndex != 0){
            Map<String, Object> nextStep = steps.get(nextStepIndex);

            Integer dayGap = (Integer) nextStep.get("dayGap").getClass().cast(nextStep.get("dayGap"));
            Integer hourGap = (Integer) nextStep.get("hourGap").getClass().cast(nextStep.get("hourGap"));
            Integer minuteGap = (Integer) nextStep.get("minuteGap").getClass().cast(nextStep.get("minuteGap"));

            ZoneId zoneId = ZoneId.of(campaign.getTimezone());
            ZonedDateTime d = ZonedDateTime.now().withZoneSameInstant(zoneId);
            String stringDateTime = String.format("%d-%02d-%02dT%02d:%02d:%02d", d.getYear(), d.getMonthValue(), (d.getDayOfMonth() + dayGap), (d.getHour() + hourGap), (d.getMinute() + minuteGap), d.getSecond());
            LocalDateTime localDateTime = LocalDateTime.from(LocalDateTime.parse(stringDateTime).atZone(zoneId));
            ZonedDateTime startDate2 = ZonedDateTime.of(localDateTime, zoneId);

            JobDetail jobDetail = buildStepJobDetail(prospectIds, campaignId, nextStepIndex, afterNextStepIndex, userId);
            Trigger trigger = buildStepTrigger(jobDetail, campaignId, startDate2);
            try {
                scheduler.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        try {
            scheduler.deleteJob(context.getJobDetail().getKey());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private JobDetail buildStepJobDetail(List<String> prospectIds, String campaignId, Integer stepIndex, Integer nextStepIndex, String userId){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("prospectIds", prospectIds);
        jobDataMap.put("campaignId", campaignId);
        jobDataMap.put("nextStepIndex", nextStepIndex);
        jobDataMap.put("stepIndex", stepIndex);
        jobDataMap.put("userId", userId);
        return JobBuilder.newJob(StepJob.class)
                .withIdentity(UUID.randomUUID().toString(), campaignId)
                .withDescription("Run Step Job")
                .storeDurably()
                .usingJobData(jobDataMap)
                .build();
    }

    private Trigger buildStepTrigger(JobDetail jobDetail, String campaignId, ZonedDateTime startDate){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), campaignId)
                .withDescription("Step Scheduler")
                .startAt(Date.from(startDate.toInstant()))
                .build();
    }
}
