package com.lemty.server.jobs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.helpers.GmailHelper;
import com.lemty.server.jobPayload.MailRequest;
import com.lemty.server.repo.ProspectMetadataRepository;
import com.lemty.server.service.DeliveribilitySettingsService;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class PreviewMailJob extends QuartzJobBean{
    Logger logger = LoggerFactory.getLogger(MailJob.class);
    private final GmailHelper gmailHelper;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final Scheduler scheduler;
    private final DeliveribilitySettingsService deliveribilitySettingsService;
    public PreviewMailJob(GmailHelper gmailHelper, ProspectMetadataRepository prospectMetadataRepository, Scheduler scheduler, DeliveribilitySettingsService deliveribilitySettingsService){
        this.gmailHelper = gmailHelper;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.scheduler = scheduler;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        String from = jobDataMap.getString("from");
        String to = jobDataMap.getString("to");
        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String prospectId = jobDataMap.getString("prospectId");
        String campaignId = jobDataMap.getString("campaignId");
        String nextProspect = jobDataMap.getString("nextProspect");
        Integer stepNumber = (Integer) jobDataMap.get("stepNumber");
        Integer nextStep = (Integer) jobDataMap.get("nextStep");
        Integer mailNumber = (Integer) jobDataMap.get("mailNumber");
        String window = jobDataMap.getString("window");
        String days = jobDataMap.getString("days");
        String timezone = jobDataMap.getString("timezone");
        String userId = jobDataMap.getString("userId");
        Integer dayGap = (Integer) jobDataMap.get("dayGap");
        Integer minuteGap = (Integer) jobDataMap.get("minuteGap");
        Integer hourGap = (Integer) jobDataMap.get("hourGap");
        String firstProspect = jobDataMap.getString("firstProspect");

        sendMail(context, from, to, subject, body, prospectId, campaignId, stepNumber, mailNumber, nextProspect, window, days, timezone, userId, dayGap, minuteGap, hourGap, nextStep, firstProspect);
    }
    private void sendMail(JobExecutionContext context, String from, String to, String subject, String body, String prospectId, String campaignId, Integer stepNumber, Integer mailNumber, String nextProspect, String window, String days, String timezone, String userId, Integer dayGap, Integer minuteGap, Integer hourGap, Integer nextStep, String firstProspect) {
        ProspectMetadata metadata = prospectMetadataRepository.findByProspectIdAndCampaignId(prospectId, campaignId);

        DeliveribilitySettings deliveribilitySettings = deliveribilitySettingsService.getDeliveribilitySettings(userId);

        int minSeconds = deliveribilitySettings.getMinInterval();
        int maxSeconds = deliveribilitySettings.getMaxInterval();
        int seconds = deliveribilitySettings.getSeconds();

        MailRequest mailRequest = new MailRequest(from, subject, to, body);
        String threadId;
        if(stepNumber == 1){
            String newThreadId = gmailHelper.sendMessage(mailRequest);
            logger.info(String.valueOf(metadata));
            metadata.setThreadId(newThreadId);
            metadata.setLastCompletedStep(stepNumber);
            prospectMetadataRepository.save(metadata);
        }
        else{
            threadId = metadata.getThreadId();
            gmailHelper.sendMessageInThread(mailRequest, threadId);
        }

        try {
            if(deliveribilitySettings.getEmailInterval().equals("random")){
                Random r = new Random();
                int result = r.nextInt(maxSeconds - minSeconds) + minSeconds;
                Thread.sleep(result * 1000);
            }
            else{
                Thread.sleep(seconds * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(nextProspect != null){
            try {
                JobKey jobKey = new JobKey(nextProspect + "-" + stepNumber + "-" + campaignId, campaignId);
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                logger.info(String.valueOf(jobDetail.getKey()));

                Trigger trigger = buildTrigger(jobDetail, days, campaignId, timezone, window, Date.from(Instant.now()));
                scheduler.scheduleJob(trigger);
                scheduler.deleteJob(context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        else{
            try{
                if(nextStep != null){
                    ZoneId zoneId = ZoneId.of(timezone);
                    ZonedDateTime d = ZonedDateTime.now().withZoneSameInstant(zoneId);
                    String stringDateTime = String.format("%d-%02d-%02dT%02d:%02d:%02d", d.getYear(), d.getMonthValue(), (d.getDayOfMonth() + dayGap), (d.getHour() + hourGap), (d.getMinute() + minuteGap), d.getSecond());
                    LocalDateTime localDateTime = LocalDateTime.from(LocalDateTime.parse(stringDateTime).atZone(ZoneId.of(timezone)));
                    ZonedDateTime startDate = ZonedDateTime.of(localDateTime, ZoneId.of(timezone));
                    JobKey jobKey = new JobKey(firstProspect + "-" + nextStep + "-" + campaignId, campaignId);
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    Trigger trigger = buildTrigger(jobDetail, days, campaignId, timezone, window, Date.from(startDate.toInstant()));
                    scheduler.scheduleJob(trigger);
                }
                scheduler.deleteJob(context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

 	private Trigger buildTrigger(JobDetail jobDetail, String days, String campaignId, String timezone, String window, Date startDate){
 		return TriggerBuilder.newTrigger()
 			.forJob(jobDetail)
			.withIdentity(jobDetail.getKey().getName(), campaignId)
 			.withDescription("Mail Job")
            .startAt(startDate)
            .withSchedule(CronScheduleBuilder
                .cronSchedule("5 * " + window + "  ? * " + days)
                .inTimeZone(TimeZone.getTimeZone(timezone))
                .withMisfireHandlingInstructionFireAndProceed()
            )
 			.build();
 	}
}
