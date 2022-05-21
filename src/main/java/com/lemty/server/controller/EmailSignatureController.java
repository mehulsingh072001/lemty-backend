package com.lemty.server.controller;

import java.util.List;

import com.lemty.server.domain.EmailSignature;
import com.lemty.server.service.EmailSignatureService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/signature")
public class EmailSignatureController {
    private final EmailSignatureService emailSignatureService;
    
    public EmailSignatureController(EmailSignatureService emailSignatureService){
        this.emailSignatureService = emailSignatureService;
    }

    @GetMapping
    public List<EmailSignature> getAllSignatures(@RequestParam("userId") String userId){
        return emailSignatureService.getSignatures(userId);
    }

    @PostMapping
    public ResponseEntity<EmailSignature> addNewSignature(@RequestBody EmailSignature emailSignature, @RequestParam("userId") String userId){
        emailSignatureService.addNewSignature(emailSignature, userId);
        return new ResponseEntity<>(emailSignature, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<EmailSignature> updateSignature(@RequestBody EmailSignature newSignature, @RequestParam("signatureId") String signatureId){
        emailSignatureService.updateSignature(newSignature, signatureId);
        return new ResponseEntity<>(newSignature, HttpStatus.CREATED);
    }

    @DeleteMapping
    public void deleteSignature(@RequestParam("signatureId") String signatureId){
        emailSignatureService.deleteSignature(signatureId);
    }
}