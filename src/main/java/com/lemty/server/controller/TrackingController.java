package com.lemty.server.controller;


import com.lemty.server.service.TrackingService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(path = "api/collecting/")
public class TrackingController {
    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/opens/{prospectId}/{campaignId}/{stepNumber}/{mailNumber}")
    public void setOpens(@PathVariable("prospectId") String prospectId, @PathVariable("campaignId") String campaignId, @PathVariable("stepNumber") Integer stepNumber, @PathVariable("mailNumber") Integer mailNumber) throws IOException {
        trackingService.trackOpens(prospectId, campaignId, stepNumber, mailNumber);
    }

    @GetMapping("/clicks/{prospectId}/{campaignId}/{stepNumber}/{mailNumber}")
    public void setClicks(@PathVariable("prospectId") String prospectId, @PathVariable("campaignId") String campaignId, @PathVariable("stepNumber") Integer stepNumber, @PathVariable("mailNumber") Integer mailNumber) throws IOException {
        trackingService.trackClicks(prospectId, campaignId, stepNumber, mailNumber);
    }
}
