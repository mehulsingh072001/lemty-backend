package com.lemty.server.service;

import java.util.List;

import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.Unsubscribe;
import com.lemty.server.repo.UnsubscribeRepository;

import com.lemty.server.repo.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class UnsubscribeService {
    private final UnsubscribeRepository unsubscribeRepository;
    private final ProspectService prospectService;
    private final UserRepo userRepo;

    @Autowired
    private Environment env;

    @Autowired
    public UnsubscribeService(UnsubscribeRepository unsubscribeRepository, UserRepo userRepo, ProspectService prospectService){
        this.unsubscribeRepository = unsubscribeRepository;
        this.userRepo = userRepo;
        this.prospectService = prospectService;
    }

    //Get Signature
    public Unsubscribe getUnsubscribe(String userId) {
        return unsubscribeRepository.findByAppUserId(userId);
    }

    //Create a signature
    public Unsubscribe addNewUnsubscribe(Unsubscribe unsubscribe, String userId){
        String s = StringUtils.substringBetween(unsubscribe.getBody(), "{{", "}}");
        String r = unsubscribe.getBody().replace("{{" + s + "}}", "<a href=" + env.getProperty("track.url") + "'/unsubscribeConfirmation/{{prospectId}}'>" + s +"</a>");
        unsubscribe.setBody(r);
        AppUser user = userRepo.findById(userId).get();
        unsubscribe.setAppUser(user);
        return unsubscribeRepository.save(unsubscribe);
    }

    //Update a signature
    public Unsubscribe updateUnsubscribe(Unsubscribe newUnsubscribe, String unsubId){
        String s = StringUtils.substringBetween(newUnsubscribe.getBody(), "{{", "}}");
        String r = newUnsubscribe.getBody().replace("{{" + s + "}}", "<a href=" + env.getProperty("track.url") + "'/unsubscribeConfirmation/{{prospectId}}'>" + s +"</a>");
        newUnsubscribe.setBody(r);
        Unsubscribe unsubscribe = unsubscribeRepository.findById(unsubId).get();
        unsubscribe.setBody(r);
        return unsubscribeRepository.save(unsubscribe);
    }

    //Delete unsub
    public void deleteUnsub(String unsubId){
        boolean exists = unsubscribeRepository.existsById(unsubId);
        if(!exists){
            throw new IllegalStateException(
                    "signature with id " + unsubId + " does not exists"
            );
        }
        unsubscribeRepository.deleteById(unsubId);
    }

    public void unsubscribeProspect(String prospectId){
        prospectService.unsubscribeProspect(prospectId);
    }
}
