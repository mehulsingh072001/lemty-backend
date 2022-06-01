package com.lemty.server.controller;

import com.lemty.server.domain.Emails;
import com.lemty.server.service.EmailsService;

import org.springframework.web.bind.annotation.GetMapping;
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
}
