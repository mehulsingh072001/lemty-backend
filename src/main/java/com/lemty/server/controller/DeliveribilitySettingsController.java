package com.lemty.server.controller;

import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.service.DeliveribilitySettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/deliveribility")
public class DeliveribilitySettingsController {
    private final DeliveribilitySettingsService deliveribilitySettingsService;

    public DeliveribilitySettingsController(DeliveribilitySettingsService deliveribilitySettingsService) {
        this.deliveribilitySettingsService = deliveribilitySettingsService;
    }

    @GetMapping
    public ResponseEntity<DeliveribilitySettings> getDeliveribilitySettings(@RequestParam("userId") String userId){
        DeliveribilitySettings response = deliveribilitySettingsService.getDeliveribilitySettings(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<DeliveribilitySettings> updateDeliveribilitySettings(@RequestBody DeliveribilitySettings settings, @RequestParam("settingsId") String settingsId){
        DeliveribilitySettings response = deliveribilitySettingsService.updateDeliveribilitySettings(settings, settingsId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}