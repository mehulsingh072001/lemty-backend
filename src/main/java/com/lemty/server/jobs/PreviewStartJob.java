package com.lemty.server.jobs;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectPreview;
import com.lemty.server.jobPayload.MailRequest;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.ProspectRepository;
import com.lemty.server.service.ProspectService;
import com.lemty.server.service.StepService;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class PreviewStartJob extends QuartzJobBean {
    Logger logger = LoggerFactory.getLogger(PreviewStartJob.class);
    private final CampaignRepository campaignRepository;
    private final ProspectService prospectService;
    private final ProspectRepository prospectRepository;
    private final StepService stepService;
    private final Scheduler scheduler;

    public PreviewStartJob(CampaignRepository campaignRepository, Scheduler scheduler, ProspectService prospectService, ProspectRepository prospectRepository, StepService stepService){
        this.campaignRepository = campaignRepository;
        this.scheduler = scheduler;
        this.prospectService = prospectService;
        this.prospectRepository = prospectRepository;
        this.stepService = stepService;
    }

    @Autowired
    private Environment env;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        List<Map<String, Object>> prospectPreviews = (List<Map<String, Object>>) jobDataMap.get("prospectPreviews").getClass().cast(jobDataMap.get("prospectPreviews"));
        String userId = jobDataMap.getString("userId");
        String campaignId = jobDataMap.getString("campaignId");
        runPreviewStart(prospectPreviews, userId, campaignId);
    }

    private void runPreviewStart(List<Map<String, Object>> prospectPreviews, String userId, String campaignId){
        Campaign campaign = campaignRepository.findById(campaignId).get();
        campaign.setStatus("Active");
        campaignRepository.save(campaign);
        for(int i=0; i < prospectPreviews.size(); i++){
            String prospectId = (String) prospectPreviews.get(i).get("prospectId").getClass().cast(prospectPreviews.get(i).get("prospectId"));
            prospectService.addProspectToCampaign(prospectId, campaignId);
        }

        List<String> prospectIds = new ArrayList<>();

        for(int i=0; i < prospectPreviews.size(); i++){
            Boolean edited = (Boolean) prospectPreviews.get(i).get("edited").getClass().cast(prospectPreviews.get(i).get("edited"));
            String prospectId = (String) prospectPreviews.get(i).get("prospectId").getClass().cast(prospectPreviews.get(i).get("prospectId"));
            if(edited){
                Prospect prospect = prospectRepository.findById(prospectId).get();
                List<Map<String, Object>> previews = (List<Map<String, Object>>) prospectPreviews.get(i).get("previews").getClass().cast(prospectPreviews.get(i).get("previews"));
                String firstProspect = (String) prospectPreviews.get(0).get("prospectEmail").getClass().cast(prospectPreviews.get(0).get("prospectEmail"));
                String nextProspect;
                if((i + 1) < prospectPreviews.size()){
                    nextProspect = (String) prospectPreviews.get(i + 1).get("prospectEmail").getClass().cast(prospectPreviews.get(i + 1).get("prospectEmail"));
                }
                else{
                    nextProspect = null;
                }
                for(int j=0; j < previews.size(); j++){
                    String subject = previews.get(j).get("subject").toString();
                    String from = previews.get(j).get("from").toString();
                    String body = previews.get(j).get("body").toString();
                    String window = previews.get(j).get("window").toString();
                    String days = previews.get(j).get("days").toString();
                    Integer stepNumber = (Integer) previews.get(j).get("stepNumber").getClass().cast(previews.get(j).get("stepNumber"));
                    Integer mailNumber = (Integer) previews.get(j).get("mailNumber").getClass().cast(previews.get(j).get("mailNumber"));
                    String date = (String) previews.get(j).get("startDate").getClass().cast(previews.get(j).get("startDate"));
                    Integer dayGap = (Integer) previews.get(j).get("dayGap").getClass().cast(previews.get(j).get("dayGap"));
                    Integer hourGap = (Integer) previews.get(j).get("hourGap").getClass().cast(previews.get(j).get("hourGap"));
                    Integer minuteGap = (Integer) previews.get(j).get("minuteGap").getClass().cast(previews.get(j).get("minuteGap"));
                    Integer nextStep = null;
                    if((i + 1) < prospectPreviews.size()){
                        nextStep = (Integer) previews.get(j + 1).get("stepNumber").getClass().cast(previews.get(j + 1).get("stepNumber"));
                    }
                    else{
                        nextProspect = null;
                    }
                    ZonedDateTime startDate = ZonedDateTime.parse(date);

                    String openLink =  env.getProperty("track.url").toString() + "/getAttachment/" + prospectId + "/" + campaignId + "/" + (stepNumber - 1)  + "/" + mailNumber.toString();
                    body = body + "<img src=" + openLink + "alt='pixel'>";

                    MailRequest mailRequest = new MailRequest(from, subject, prospect.getProspectEmail(), body);
                    try {
                        JobDetail jobDetail = buildJobDetail(mailRequest, campaignId, prospectId, stepNumber, mailNumber, nextProspect, window, days, campaign.getTimezone(), userId, firstProspect, dayGap, hourGap, minuteGap, nextStep);
                        scheduler.addJob(jobDetail, true);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                prospectIds.add(prospectId);
            }
        }

        //Create step job
        if(!prospectIds.isEmpty()){

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
                JobDetail jobDetail = buildStepJobDetail(prospectIds, campaignId, stepIndex, nextStepIndex, stepNumber, userId);
                Trigger trigger = buildStepTrigger(jobDetail, campaignId);
                scheduler.scheduleJob(jobDetail, trigger);
            }
            catch(SchedulerException e){
                e.printStackTrace();
            }
        }

        String prospectEmail = (String) prospectPreviews.get(0).get("prospectEmail").getClass().cast(prospectPreviews.get(0).get("prospectEmail"));
        List<Map<String, Object>> previews = (List<Map<String, Object>>) prospectPreviews.get(0).get("previews").getClass().cast(prospectPreviews.get(0).get("previews"));
        Integer stepNumber = (Integer) previews.get(0).get("stepNumber").getClass().cast(previews.get(0).get("stepNumber"));
        JobKey jobKey = new JobKey(prospectEmail + "-" + stepNumber + "-" + campaignId, campaignId);
        JobDetail jobDetail = null;

        try {
            jobDetail = scheduler.getJobDetail(jobKey);

            String days = previews.get(0).get("days").toString();
            String window = previews.get(0).get("window").toString();

            String date = (String) previews.get(0).get("startDate").getClass().cast(previews.get(0).get("startDate"));
            ZonedDateTime startDate = ZonedDateTime.parse(date);

            Trigger trigger = buildTrigger(jobDetail, days, campaignId, campaign.getTimezone(), startDate, window);
            logger.info(String.valueOf(trigger.getStartTime()));
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private JobDetail buildJobDetail(MailRequest mailRequest, String campaignId, String prospectId, Integer stepNumber, Integer mailNumber, String nextProspect, String window, String days, String timezone, String userId, String firstProspect, Integer dayGap, Integer hourGap, Integer minuteGap, Integer nextStep){
         JobDataMap jobDataMap = new JobDataMap();
         jobDataMap.put("from", mailRequest.getFrom());
         jobDataMap.put("to", mailRequest.getTo());
         jobDataMap.put("subject", mailRequest.getSubject());
         jobDataMap.put("body", mailRequest.getBody());
         jobDataMap.put("campaignId", campaignId);
         jobDataMap.put("prospectId", prospectId);
         jobDataMap.put("stepNumber", stepNumber);
         jobDataMap.put("mailNumber", mailNumber);
         jobDataMap.put("nextProspect", nextProspect);
         jobDataMap.put("timezone", timezone);
         jobDataMap.put("window", window);
         jobDataMap.put("days", days);
         jobDataMap.put("userId", userId);
         jobDataMap.put("firstProspect", firstProspect);
         jobDataMap.put("dayGap", dayGap);
         jobDataMap.put("hourGap", hourGap);
         jobDataMap.put("minuteGap", minuteGap);

         return JobBuilder.newJob(PreviewMailJob.class)
            .withIdentity(mailRequest.getTo() + "-" + stepNumber + "-" + campaignId, campaignId)
            .withDescription("Mail Job")
            .storeDurably()
            .usingJobData(jobDataMap)
            .build();
    }

 	private Trigger buildTrigger(JobDetail jobDetail, String days, String campaignId, String timezone, ZonedDateTime startDate, String window){
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

    private JobDetail buildStepJobDetail(List<String> prospectIds, String campaignId, Integer stepIndex, Integer nextStepIndex, Integer stepNumber, String userId){
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

 	private Trigger buildStepTrigger(JobDetail jobDetail, String campaignId){
 		return TriggerBuilder.newTrigger()
 			.forJob(jobDetail)
			.withIdentity(jobDetail.getKey().getName(), campaignId)
 			.withDescription("Step Scheduler")
            .startAt(Date.from(Instant.now()))
 			.build();
 	}
}
