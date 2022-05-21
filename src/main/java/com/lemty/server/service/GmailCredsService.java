package com.lemty.server.service;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.GmailCreds;
import com.lemty.server.repo.GmailCredsRepo;
import com.lemty.server.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class GmailCredsService {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final GmailCredsRepo gmailCredsRepo;
    private final UserRepo userRepo;

    @Autowired
    public GmailCredsService(GmailCredsRepo gmailCredsRepo, UserRepo userRepo) {
        this.gmailCredsRepo = gmailCredsRepo;
        this.userRepo = userRepo;
    }

    //List creds of user
    public List<GmailCreds> getCredsFromUser(String userId){
        return gmailCredsRepo.findByAppUserId(userId);
    }

    //List creds of user
    public GmailCreds getCredsByEmail(String emailId){
        return gmailCredsRepo.findByEmail(emailId);
    }

    //Add new creds
    public void addNewCreds(GmailCreds gmailCreds, String userId){
        AppUser appUser = userRepo.findById(userId).get();
        gmailCreds.setAppUser(appUser);
        gmailCredsRepo.saveAndFlush(gmailCreds);
    }

    // set display name
    public void updateDisplayName(GmailCreds newGmailCreds, String credsId) {
        gmailCredsRepo.findById(credsId)
            .map(gmailCreds -> {
                gmailCreds.setDisplayName(newGmailCreds.getDisplayName());
                return gmailCredsRepo.saveAndFlush(gmailCreds);
            });
    }

    //update creds
    public void updateCreds(GmailCreds newCreds, String credsId){
        gmailCredsRepo.findById(credsId)
            .map(gmailCreds -> {
                if(newCreds.getEmail() == null){
                    gmailCreds.setEmail(newCreds.getEmail());
                }
                if(newCreds.getRefreshToken() == null){
                    gmailCreds.setRefreshToken(newCreds.getRefreshToken());
                }
                return gmailCredsRepo.saveAndFlush(gmailCreds);
            });
        // .orElseGet(() -> {
        //     newCreds.setId(credsId);
        //     return gmailCredsRepo.saveAndFlush(newCreds);
        // });
    }

    public void deleteCreds(String credsId){
        boolean exists = gmailCredsRepo.existsById(credsId);
        if(!exists){
            throw new IllegalStateException(
                    "creds with id " + credsId + " does not exists"
            );
        }
        gmailCredsRepo.deleteById(credsId);
    }
}
