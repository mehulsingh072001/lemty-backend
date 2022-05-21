package com.lemty.server.service;

import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.repo.DeliveribilitySettingsRepository;
import com.lemty.server.repo.UserRepo;

import org.springframework.stereotype.Service;

@Service
public class DeliveribilitySettingsService {
    private final DeliveribilitySettingsRepository deliveribilitySettingsRepository;
    private final UserRepo userRepo;

    public DeliveribilitySettingsService(DeliveribilitySettingsRepository deliveribilitySettingsRepository, UserRepo userRepo) {
        this.deliveribilitySettingsRepository = deliveribilitySettingsRepository;
        this.userRepo = userRepo;
    }

    //Get Settings
    public DeliveribilitySettings getDeliveribilitySettings(String userId) {
        return deliveribilitySettingsRepository.findByAppUserId(userId);
    }

    public DeliveribilitySettings createDeliveribilitySettings(DeliveribilitySettings settings, String userId){
        AppUser user = userRepo.findById(userId).get();
        settings.setAppUser(user);
        return deliveribilitySettingsRepository.saveAndFlush(settings);
    }
    
    public DeliveribilitySettings updateDeliveribilitySettings(DeliveribilitySettings settings, String settingsId){
        DeliveribilitySettings existingSettings = deliveribilitySettingsRepository.findById(settingsId).get();
        existingSettings.setId(existingSettings.getId());
        existingSettings.setCustomTrackingDomain(settings.getCustomTrackingDomain());
        existingSettings.setSeconds(settings.getSeconds());
        existingSettings.setEmailInterval(settings.getEmailInterval());
        existingSettings.setMinInterval(settings.getMinInterval());
        existingSettings.setMaxInterval(settings.getMaxInterval());
        
        return deliveribilitySettingsRepository.saveAndFlush(existingSettings);
    }
}
