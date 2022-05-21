package com.lemty.server.controller;

import java.util.List;

import com.lemty.server.domain.GmailCreds;
import com.lemty.server.service.GmailCredsService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/creds")
public class CredsController {
    private final GmailCredsService  gmailCredsService;

    public CredsController(GmailCredsService gmailCredsService){
        this.gmailCredsService = gmailCredsService;
    }

    @GetMapping
    public ResponseEntity<List<GmailCreds>> getAllCreds(@RequestParam("userId") String userId){
        List<GmailCreds> creds = gmailCredsService.getCredsFromUser(userId);
        return new ResponseEntity<>(creds, HttpStatus.ACCEPTED);
    }

    @PutMapping(path = "/name")
    public ResponseEntity<GmailCreds> updateDisplayName(@RequestBody GmailCreds gmailCreds, @RequestParam("credsId") String credsId){
        gmailCredsService.updateDisplayName(gmailCreds, credsId);
        return new ResponseEntity<>(gmailCreds, HttpStatus.CREATED);
    }

    @DeleteMapping
    public void deleteCreds(@RequestParam("credsId") String credsId){
        gmailCredsService.deleteCreds(credsId);
    }
}
