package com.lemty.server.controller;

import com.lemty.server.domain.IntentDetection;
import com.lemty.server.service.IntentDetectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/intent")
public class IntentDetectionController {
    @Autowired
    private IntentDetectionService intentDetectionService;

    @GetMapping
    public ResponseEntity<IntentDetection> getIntentDetection(@RequestParam("userId") String appUserId){
        IntentDetection intentDetection = intentDetectionService.getIntentDetection(appUserId);
        return new ResponseEntity<IntentDetection>(intentDetection, HttpStatus.ACCEPTED);
    }


    @PutMapping("/update")
    public ResponseEntity<IntentDetection> updateIntentDetection(@RequestBody IntentDetection intentDetection, @RequestParam("intentDetectionId") String intentDetectionId){
        IntentDetection response = intentDetectionService.updateIntentDetection(intentDetection, intentDetectionId);
        return new ResponseEntity<IntentDetection>(response, HttpStatus.ACCEPTED);
    }
}
