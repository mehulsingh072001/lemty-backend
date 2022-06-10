package com.lemty.server.jobs;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.domain.Emails;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.helpers.GmailHelper;
import com.lemty.server.helpers.PlaceholderHelper;
import com.lemty.server.jobPayload.MailRequest;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.EmailsRepository;
import com.lemty.server.repo.ProspectMetadataRepository;
import com.lemty.server.repo.ProspectRepository;
import com.lemty.server.service.DeliveribilitySettingsService;
import com.lemty.server.service.EmailSignatureService;
import com.lemty.server.service.MailJobService;
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
    private final StepService stepService;
    private final CampaignRepository campaignRepository;
    private final ProspectRepository prospectRepository;
    private final EmailsRepository emailsRepository;
    private final MailJobService mailJobService;
    private final DeliveribilitySettingsService deliveribilitySettingsService;
    @Autowired
    private Environment env;

    public MailJob(GmailHelper gmailHelper, ProspectMetadataRepository prospectMetadataRepository, Scheduler scheduler, StepService stepService, CampaignRepository campaignRepository, ProspectRepository prospectRepository, PlaceholderHelper placeholderHelper, EmailSignatureService emailSignatureService, UnsubscribeService unsubscribeService, EmailsRepository emailsRepository, MailJobService mailJobService, DeliveribilitySettingsService deliveribilitySettingsService){
        this.gmailHelper = gmailHelper;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.scheduler = scheduler;
        this.stepService = stepService;
        this.campaignRepository = campaignRepository;
        this.prospectRepository = prospectRepository;
        this.emailsRepository = emailsRepository;
        this.mailJobService = mailJobService;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String prospectId = jobDataMap.getString("prospectId");
        String campaignId = jobDataMap.getString("campaignId");
        Integer stepIndex = (Integer) jobDataMap.get("stepIndex");
        Integer prospectIndex = (Integer) jobDataMap.get("prospectIndex");
        String userId = jobDataMap.getString("userId");
        Integer nextStepIndex = (Integer) jobDataMap.get("nextStepIndex");
        Integer afterNextStepIndex = (Integer) jobDataMap.get("afterNextStepIndex");
        String emailsId = jobDataMap.getString("emailsId");
        String nextProspectId = jobDataMap.getString("nextProspectId");

        sendMail(context, prospectId, campaignId, stepIndex, userId, nextStepIndex, afterNextStepIndex, emailsId, nextProspectId);
    }
    private void sendMail(JobExecutionContext context, String prospectId, String campaignId, Integer stepIndex, String userId, Integer nextStepIndex, Integer afterNextStepIndex, String emailsId, String nextProspectId) {
        //Get data from app
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        ProspectMetadata metadata = prospectMetadataRepository.findByProspectIdAndCampaignId(prospectId, campaignId);
        Prospect prospect = prospectRepository.findById(prospectId).get();
        Map<String, Object> step = steps.get(stepIndex);
        Emails email = emailsRepository.findById(emailsId).get();

        Integer stepNumber = (Integer) step.get("stepNumber").getClass().cast(step.get("stepNumber"));
        MailRequest mailRequest = new MailRequest(email.getFromEmail(), email.getSubject(), email.getToEmail(), email.getBody());
        String threadId;
        if(stepNumber == 1){
            Map<Object, Object> response = gmailHelper.sendMessage(mailRequest);
            String newThreadId = response.get("threadId").toString();
            metadata.setThreadId(newThreadId);
            metadata.setMsgId(response.get("messageId").toString());
            metadata.setLastCompletedStep(stepNumber);
            prospectMetadataRepository.save(metadata);
            metadata.setStatus("CONTACTED");
        }
        else{
            threadId = metadata.getThreadId();
            gmailHelper.sendMessageInThread(mailRequest, threadId, metadata.getMsgId());
            metadata.setLastCompletedStep(stepNumber);
            prospectMetadataRepository.save(metadata);
        }
        prospect.setStatus("CONTACTED");
        email.setStatus("SENT");
        ZonedDateTime sendTime = ZonedDateTime.now().withZoneSameLocal(ZoneId.of("Asia/Kolkata"));
        email.setSentDateTime(Date.from(sendTime.toInstant()));
        emailsRepository.save(email);

        List<String> prospectIds = new ArrayList<>();
        prospectIds.add(prospectId);

        if(nextStepIndex != 0){
            Map<String, Object> nextStep = steps.get(nextStepIndex);
            Integer dayGap = Integer.parseInt((String) nextStep.get("dayGap").getClass().cast(nextStep.get("dayGap")));
            Integer hourGap = (Integer) nextStep.get("hourGap").getClass().cast(nextStep.get("hourGap"));
            Integer minuteGap = (Integer) nextStep.get("minuteGap").getClass().cast(nextStep.get("minuteGap"));
            ZoneId zoneId = ZoneId.of(campaign.getTimezone());
            ZonedDateTime startDate2 = ZonedDateTime.now().withZoneSameInstant(zoneId).plusDays(dayGap).plusHours(hourGap).plusMinutes(minuteGap);

            mailJobService.runStep(prospectIds, campaignId, nextStepIndex, afterNextStepIndex, stepNumber, userId, startDate2);
        }

        Emails nextEmail = emailsRepository.findByCampaignIdAndProspectId(campaignId, nextProspectId);
        if(nextEmail != null){
            logger.info(String.valueOf(nextEmail));
            //Trigger mail job
            JobKey jobKey = new JobKey(nextEmail.getId() + "-" + campaignId, campaignId);
            logger.info(String.valueOf(jobKey));

            String window = String.valueOf(step.get("startHour")) + "-" + String.valueOf(step.get("endHour"));
            Object days = step.get("days");
            ArrayList list = (ArrayList) days.getClass().cast(days);
            StringJoiner joiner = new StringJoiner(",");
            for(int l = 0; l < list.size(); l++){
               joiner.add(list.get(l).toString());
            }
            String daysString = joiner.toString();
            DeliveribilitySettings deliveribilitySettings = deliveribilitySettingsService.getDeliveribilitySettings(userId);
            int minSeconds = deliveribilitySettings.getMinInterval();
            int maxSeconds = deliveribilitySettings.getMaxInterval();
            int seconds = deliveribilitySettings.getSeconds();
            ZoneId zoneId = ZoneId.of(campaign.getTimezone());

            //Interval between emails
            Integer interval;
            if(deliveribilitySettings.getEmailInterval().equals("random")){
                Random r = new Random();
                int result = r.nextInt(maxSeconds - minSeconds) + minSeconds;
                interval = result / 60;
            }
            else{
                interval = seconds / 60;
            }

            try {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                Trigger trigger = buildMailTrigger(jobDetail, daysString, campaignId, campaign.getTimezone(), window, interval);
                scheduler.scheduleJob(trigger);
                if(trigger.getNextFireTime().after(trigger.getStartTime())){
                    List<Emails> emails = emailsRepository.findByCampaignIdAndStatus(campaignId, "TODAY");
                    for(Emails email2 : emails){
                        email2.setStatus("UPCOMING");
                    }
                    emailsRepository.saveAll(emails);
                }
                logger.info(String.valueOf(trigger.getNextFireTime()));
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

    private Trigger buildMailTrigger(JobDetail jobDetail, String days, String campaignId, String timezone,  String window, Integer interval){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), campaignId)
                .withDescription("Mail Job")
                .withSchedule(CronScheduleBuilder
                        .cronSchedule( "0  1/" + interval + " " + window + "  ? * " + days)
                        .inTimeZone(TimeZone.getTimeZone(timezone))
                        .withMisfireHandlingInstructionFireAndProceed()
                )
                .build();
    }
}
