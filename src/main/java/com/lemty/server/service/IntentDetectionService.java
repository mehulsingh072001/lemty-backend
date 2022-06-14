package com.lemty.server.service;

import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.IntentDetection;
import com.lemty.server.domain.Prospect;
import com.lemty.server.repo.IntentDetectionRepository;
import com.lemty.server.repo.ProspectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntentDetectionService {
    @Autowired
    private IntentDetectionRepository intentDetectionRepository;
    @Autowired
    private ProspectRepository prospectRepository;

    public IntentDetection getIntentDetection(String appUserId){
        IntentDetection intentDetection = intentDetectionRepository.findByAppUserId(appUserId);
        return intentDetection;
    }

    public IntentDetection updateIntentDetection(IntentDetection newIntentDetection, String intentDetectionId){
        IntentDetection existinginDetection = intentDetectionRepository.findById(intentDetectionId).get();

        existinginDetection.setClickPoints(newIntentDetection.getClickPoints());
        existinginDetection.setOpenPoints(newIntentDetection.getOpenPoints());
        existinginDetection.setHotPoints(newIntentDetection.getHotPoints());
        intentDetectionRepository.save(existinginDetection);

        return existinginDetection;
    }

    public void detectOpenIntent(String prospectId){
        Prospect prospect = prospectRepository.findById(prospectId).get();
        AppUser appUserId = prospect.getAppUser(); 
        IntentDetection intentDetection = intentDetectionRepository.findByAppUserId(appUserId.getId());

        prospect.setPoints(prospect.getPoints() + intentDetection.getOpenPoints());
        if(prospect.getPoints() >= intentDetection.getHotPoints()){
            prospect.setHot(true);
        }
        prospectRepository.save(prospect);
    }

    public void detectClickIntent(String prospectId){
        Prospect prospect = prospectRepository.findById(prospectId).get();
        AppUser appUserId = prospect.getAppUser(); 
        IntentDetection intentDetection = intentDetectionRepository.findByAppUserId(appUserId.getId());

        prospect.setPoints(prospect.getPoints() + intentDetection.getClickPoints());
        if(prospect.getPoints() >= intentDetection.getHotPoints()){
            prospect.setHot(true);
        }
        prospectRepository.save(prospect);
    }
}
