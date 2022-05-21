package com.lemty.server.controller;

import com.lemty.server.domain.Mail;
import com.lemty.server.service.MailService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/mail")
public class MailController {
    private final MailService mailService;

    public MailController(MailService mailService){
        this.mailService = mailService;
    }

    @GetMapping(path = "/{stepId}")
    public Mail getFromStep(@PathVariable("stepId") String stepId){
        return mailService.getFromStep(stepId);
    }

/*
    @PostMapping(path = "/{stepId}")
    public ResponseEntity<Mail> addNewMail(@RequestBody Mail mail, @PathVariable("stepId") String stepId){
        mailService.addNewMail(mail, stepId);
        return new ResponseEntity<>(mail, HttpStatus.CREATED);
    }
*/

    @DeleteMapping(path = "/{mailId}")
    public void deleteMail(@PathVariable("mailId") String mailId){
        mailService.deleteMail(mailId);
    }
}


