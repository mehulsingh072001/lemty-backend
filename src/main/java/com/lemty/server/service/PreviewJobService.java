package com.lemty.server.service;

import com.lemty.server.domain.ProspectPreview;
import com.lemty.server.jobs.PreviewStartJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class PreviewJobService {
    Logger logger = LoggerFactory.getLogger(PreviewJobService.class);
    private final Scheduler scheduler;

    public PreviewJobService(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    public void previewAndStartJob(List<Map<String, Object>> prospectPreviews, String userId, String campaignId){
        try {
            JobDetail jobDetail = buildJobDetail(prospectPreviews, campaignId, userId);
            Trigger trigger = buildTrigger(jobDetail, campaignId);
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private JobDetail buildJobDetail(List<Map<String, Object>> prospectPreviews, String campaignId, String userId){
         JobDataMap jobDataMap = new JobDataMap();
         jobDataMap.put("prospectPreviews", prospectPreviews);
         jobDataMap.put("userId", userId);
         jobDataMap.put("campaignId", campaignId);
         return JobBuilder.newJob(PreviewStartJob.class)
             .withIdentity(UUID.randomUUID().toString(), campaignId)
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
            .startNow()
 			.build();
 	}
}
