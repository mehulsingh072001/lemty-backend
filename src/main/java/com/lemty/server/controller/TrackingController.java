package com.lemty.server.controller;


import com.lemty.server.service.TrackingService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/opens/{emailId}")
    public ResponseEntity<String> setOpens(@PathVariable("emailId") String emailId) throws IOException {
        trackingService.trackOpens(emailId);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("/clicks/{emailId}")
    public void setClicks(@PathVariable("emailId") String emailId) throws IOException {
        trackingService.trackClicks(emailId);
    }
}