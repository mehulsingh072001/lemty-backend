package com.lemty.server.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lemty.server.domain.Emails;
import com.lemty.server.service.EmailsService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/emails")
public class EmailsController {
    private final EmailsService emailsService;

    public EmailsController(EmailsService emailsService){
        this.emailsService = emailsService;
    }

    @GetMapping(path = "/single")
    public Emails getSingleEmails(@RequestParam("campaignId") String campaignId, @RequestParam String prospectId){
        return emailsService.getEmailByProspectIdAndCampaignId(campaignId, prospectId);
    }

    @GetMapping(path = "/campaign/getByStatus")
    public ResponseEntity<List<Emails>> getByStatus(@RequestParam("campaignId") String campaignId, @RequestParam("status") String status){
        List<Emails> response = emailsService.getByStatusAndCampaign(campaignId, status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/campaign/count")
    public ResponseEntity<Map<String, Integer>> getByStatus(@RequestParam("campaignId") String campaignId){
        Map<String, Integer> response = emailsService.getCampaignCounts(campaignId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/sent-count")
    public ResponseEntity<List<Map<String, Object>>> getSentEmailsCount(@RequestParam String userId, @RequestBody Map<String, Object> query){
        List<Map<String, Object>> response = emailsService.getSentEmailsCount(userId, query.get("startDate").toString(), query.get("endDate").toString());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
