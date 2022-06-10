package com.lemty.server.service;

import com.lemty.server.domain.*;
import com.lemty.server.helpers.GmailHelper;
import com.lemty.server.helpers.PlaceholderHelper;
import com.lemty.server.jobs.MailJob;
import com.lemty.server.repo.*;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class MailJobService {
    Logger logger = LoggerFactory.getLogger(MailJobService.class);
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
    private final EmailsRepository emailsRepository;
    private final UserRepo userRepo;
    private final EngagementRepository engagementRepository;
    @Autowired
    private Environment env;

    public MailJobService(ProspectService prospectService, GmailHelper gmailHelper, PlaceholderHelper placeholderHelper, DeliveribilitySettingsService deliveribilitySettingsService, CampaignRepository campaignRepository, EmailSignatureService emailSignatureService, UnsubscribeService unsubscribeService, ProspectMetadataRepository prospectMetadataRepository, Scheduler scheduler, StepService stepService, ProspectRepository prospectRepository, EmailsRepository emailsRepository, UserRepo userRepo, EngagementRepository engagementRepository) {
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
        this.emailsRepository = emailsRepository;
        this.userRepo = userRepo;
        this.engagementRepository = engagementRepository;
    }
    public void runStep(List<String> prospectIds, String campaignId, Integer stepIndex, Integer nextStepIndex, Integer stepNumber, String userId, ZonedDateTime startDate) {
        //Fetching all data needed
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        List<Map<String, Object>> mails = stepService.getMailsFromSteps(campaign.getId(), stepIndex);
        List<EmailSignature> signatures = emailSignatureService.getSignatures(userId);
        AppUser user = userRepo.findById(userId).get();
        Unsubscribe unsubscribe = unsubscribeService.getUnsubscribe(userId);
        Map<String, Object> step = steps.get(stepIndex);

        //Scheduling parameters
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

        //Sending next step to prospect
        Integer afterNextStepIndex = 0;
        if(nextStepIndex != 0){
            if((nextStepIndex + 1) == steps.size()){
                afterNextStepIndex = 0;
            }
            else{
                afterNextStepIndex = nextStepIndex + 1;
            }
        }

        List<Emails> initiEmails = new ArrayList<>();

        ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
        for(int i=0; i < prospectIds.size(); i++){
            //Mail Data
            Prospect prospect = prospectRepository.findById(prospectIds.get(i)).get();
            if(!prospect.getUnsubscribed()){
                String from = step.get("whichEmail").toString();
                String to = prospect.getProspectEmail();
                String subject = (String) mails.get(i % mails.size()).get("subject").getClass().cast(mails.get(i % mails.size()).get("subject"));
                String body = (String) mails.get(i % mails.size()).get("body").getClass().cast(mails.get(i % mails.size()).get("body"));
                subject = placeholderHelper.fieldsReplacer(subject, prospect);
                body = placeholderHelper.fieldsReplacer(body, prospect);
                body = placeholderHelper.bodyLinkReplacer(body, prospect.getId(), campaignId, stepIndex, (i % mails.size()));

                if(signatures.size() > 0){
                    body = placeholderHelper.signatureReplacer(body, signatures.get(0));
                }
                if(unsubscribe != null){
                    body = placeholderHelper.unsubLinkReplacer(body, prospect.getId(), unsubscribe);
                }
                Emails email = new Emails();
                email.setFromEmail(from);
                email.setToEmail(to);
                email.setSubject(subject);
                email.setAppUser(user);
                email.setCampaign(campaign);
                email.setStep(stepIndex);
                email.setProspect(prospect);
                email.setMail(i % mails.size());

                Engagement engagement = new Engagement();
                engagement.setOpens(engagement.getOpens() + 1);
                engagement.setStepNumber(stepNumber + 1);
                engagement.setCampaign(campaignRepository.getById(campaignId));
                engagementRepository.save(engagement);
                email.setEngagement(engagement);
                emailsRepository.save(email);

                String openLink =  env.getProperty("track.url").toString() + "/getAttachment/" + email.getId();
                body = body + "<img src='" + openLink + "' alt=''>";

                email.setBody(body);
                initiEmails.add(email);

                String nextProspectId = "";
                if((i + 1) == prospectIds.size()){
                    nextProspectId = "";
                }
                else{
                    nextProspectId = prospectIds.get(i + 1);
                }
                logger.info(nextProspectId);

                try {
                    JobDetail jobDetail = buildMailJobDetail(campaignId, prospect.getId(), stepIndex, userId, nextStepIndex, afterNextStepIndex, email.getId(), nextProspectId);
                    scheduler.addJob(jobDetail, true);

                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            }
        }
        emailsRepository.saveAll(initiEmails);

        //Trigger mail job
        JobKey jobKey = new JobKey(initiEmails.get(0).getId() + "-" + campaignId, campaignId);
        logger.info(String.valueOf(Date.from(startDate.toInstant().atZone(ZoneId.of(campaign.getTimezone())).toInstant())));
        logger.info(String.valueOf(Date.from(startDate.toInstant())));
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            logger.info(String.valueOf(jobDetail));
            Trigger trigger = buildMailTrigger(jobDetail, str, campaignId, campaign.getTimezone(), window, startDate);
            scheduler.scheduleJob(trigger);
            Date emailStartDateTime = trigger.getFireTimeAfter(Date.from(startDate.toInstant()));
            if(DateUtils.isSameDay(Date.from(currentZonedDateTime.toInstant()), emailStartDateTime)){
                List<Emails> emails = emailsRepository.findByCampaignIdAndStatus(campaignId, "TODAY");
                for(Emails email2 : emails){
                    email2.setStartTime(emailStartDateTime);
                    email2.setStatus("TODAY");
                }
                emailsRepository.saveAll(emails);
            }
            else{
                List<Emails> emails = emailsRepository.findByCampaignIdAndStatus(campaignId, "TODAY");
                for(Emails email2 : emails){
                    email2.setStatus("UPCOMING");
                }
                emailsRepository.saveAll(emails);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private JobDetail buildMailJobDetail(String campaignId, String prospectId, Integer stepIndex, String userId, Integer nextStepIndex, Integer afterNextStepIndex, String emailsId, String nextProspectId){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("campaignId", campaignId);
        jobDataMap.put("prospectId", prospectId);
        jobDataMap.put("stepIndex", stepIndex);
        jobDataMap.put("userId", userId);
        jobDataMap.put("nextStepIndex", nextStepIndex);
        jobDataMap.put("afterNextStepIndex", afterNextStepIndex);
        jobDataMap.put("emailsId", emailsId);
        jobDataMap.put("nextProspectId", nextProspectId);

        return JobBuilder.newJob(MailJob.class)
                .withIdentity(emailsId + "-" + campaignId, campaignId)
                .withDescription("Mail Job")
                .storeDurably()
                .usingJobData(jobDataMap)
                .build();
    }

    private Trigger buildMailTrigger(JobDetail jobDetail, String days, String campaignId, String timezone,  String window, ZonedDateTime startDate){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), campaignId)
                .withDescription("Mail Job")
                .startAt(Date.from(startDate.toInstant().atZone(ZoneId.of(timezone)).toInstant()))
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("5 * " + window + "  ? * " + days)
                        .inTimeZone(TimeZone.getTimeZone(timezone))
                        .withMisfireHandlingInstructionFireAndProceed()
                )
                .build();
    }

}
