package com.lemty.server.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.domain.ProspectPreview;
import com.lemty.server.helpers.PlaceholderHelper;
import com.lemty.server.jobPayload.CampaignPayload;
import com.lemty.server.repo.ProspectMetadataRepository;
import com.lemty.server.service.PreviewJobService;
import com.lemty.server.service.StepJobService;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/job")
public class JobController {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private Scheduler scheduler;
    private StepJobService stepJobService;
    private PreviewJobService previewJobService;
    private ProspectMetadataRepository prospectMetadataRepository;
    private PlaceholderHelper placeholderHelper;

    public JobController(Scheduler scheduler, StepJobService stepJobService, PreviewJobService previewJobService, ProspectMetadataRepository prospectMetadataRepository, PlaceholderHelper placeholderHelper) {
        this.scheduler = scheduler;
        this.stepJobService = stepJobService;
        this.previewJobService = previewJobService;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.placeholderHelper = placeholderHelper;
    }

    @GetMapping(path = "all")
    public ResponseEntity<?> getAllJobs(@RequestParam("campaignId") String campaignId) {
        List<String> keys = new ArrayList<>();
        try {
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals(campaignId));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(keys, HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "fireTime")
    public String getAllJo(@RequestParam("time") long time) {
/*
        long bigintTime = Long.parseLong(String.valueOf(time));
        Instant instant = Instant.ofEpochSecond(bigintTime);
        return instant.toString();
*/
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }



    @PostMapping("/campaign/preview-start")
    public ResponseEntity<String> runPreviewAndStart(@RequestBody List<Map<String, Object>> prospectPreviews, @RequestParam("userId") String userId, @RequestParam("campaignId") String campaignId){
        previewJobService.previewAndStartJob(prospectPreviews, userId, campaignId);
        return new ResponseEntity<>("Data Accepted", HttpStatus.OK);
    }

    @PostMapping("/campaign/start")
    public ResponseEntity<String> runCampaign(@RequestBody CampaignPayload campaign, @RequestParam("userId") String userId){
        stepJobService.createCampaignJob(campaign, userId);
        return new ResponseEntity<>("Data Accepted", HttpStatus.OK);
    }
}
