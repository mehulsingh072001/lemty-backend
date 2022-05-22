package com.lemty.server.jobs;

import java.time.Instant;
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

        sendMail(context, prospectId, campaignId, stepIndex, nextProspect, userId, prospectIndex);
    }
    private void sendMail(JobExecutionContext context, String prospectId, String campaignId, Integer stepIndex, String nextProspect, String userId, Integer prospectIndex) {
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
        body = body + "<img src=" + openLink + "alt='pixel'>";

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

        String interval;

        if(deliveribilitySettings.getEmailInterval().equals("random")){
            Random r = new Random();
            int result = r.nextInt(maxSeconds - minSeconds) + minSeconds;
            interval = String.valueOf(result);
        }
        else{
            interval = String.valueOf(seconds / 60);
        }

        if(nextProspect != null){
            try {
                JobKey jobKey = new JobKey(nextProspect + "-" + stepNumber + "-" + campaignId, stepIndex + "-" +campaignId);
                logger.info(String.valueOf(jobKey));
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                Trigger trigger = buildTrigger(jobDetail, daysString, campaignId, campaign.getTimezone(), window, Date.from(Instant.now()), interval);
                scheduler.scheduleJob(trigger);
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

 	private Trigger buildTrigger(JobDetail jobDetail, String days, String campaignId, String timezone, String window, Date startDate, String interval){
 		return TriggerBuilder.newTrigger()
 			.forJob(jobDetail)
			.withIdentity(jobDetail.getKey().getName(), campaignId)
 			.withDescription("Mail Job")
            .startAt(startDate)
            .withSchedule(CronScheduleBuilder
                .cronSchedule("0 0/" +  (interval) + " " + (window) + "  ? * " + days)
                .inTimeZone(TimeZone.getTimeZone(timezone))
                .withMisfireHandlingInstructionFireAndProceed()
            )
 			.build();
 	}
}
