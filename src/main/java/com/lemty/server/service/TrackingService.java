package com.lemty.server.service;

import java.util.*;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Emails;
import com.lemty.server.domain.Engagement;
import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.EmailsRepository;
import com.lemty.server.repo.EngagementRepository;
import com.lemty.server.repo.ProspectMetadataRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TrackingService {
    Logger logger = LoggerFactory.getLogger(TrackingService.class);
    private final CampaignRepository campaignRepository;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final StepService stepService;
    private final EngagementRepository engagementRepository;
    private final EmailsRepository emailsRepository;

    public TrackingService(ProspectMetadataRepository prospectMetadataRepository, StepService stepService, CampaignRepository campaignRepository, EngagementRepository engagementRepository, EmailsRepository emailsRepository) {
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.stepService = stepService;
        this.campaignRepository = campaignRepository;
        this.engagementRepository = engagementRepository;
        this.emailsRepository = emailsRepository;
    }


    public void trackOpens(String emailId){
        Emails email = emailsRepository.findById(emailId).get();
        String prospectId = email.getProspect().getId();
        String campaignId = email.getCampaign().getId();
        Integer stepNumber = email.getStep();
        Integer mailNumber = email.getMail();

        //Set opens in prospect metadata
        ProspectMetadata metadata = prospectMetadataRepository.findByProspectIdAndCampaignId(prospectId, campaignId);

        List<Engagement> engagements = engagementRepository.findByProspectMetadataId(metadata.getId());
        if(engagements.isEmpty()){
            Engagement engagement = new Engagement();
            engagement.setOpens(engagement.getOpens() + 1);
            engagement.setStepNumber(stepNumber + 1);
            engagement.setProspectMetadata(metadata);
            engagements.add(engagement);
            engagementRepository.save(engagement);
            email.setEngagement(engagement);
            emailsRepository.save(email);
        }
        else{
            Engagement existingEngagement = engagementRepository.findByProspectMetadataIdAndStepNumber(metadata.getId(), stepNumber + 1);
            if(existingEngagement != null){
                if(stepNumber + 1 == existingEngagement.getStepNumber()){
                    existingEngagement.setOpens(existingEngagement.getOpens() + 1);
                    engagementRepository.save(existingEngagement);
                }
            }
            else{
                Engagement engagement = new Engagement();
                engagement.setOpens(engagement.getOpens() + 1);
                engagement.setStepNumber(stepNumber + 1);
                engagement.setProspectMetadata(metadata);
                engagements.add(engagement);
                engagementRepository.save(engagement);
                email.setEngagement(engagement);
                emailsRepository.save(email);
            }

        }
        prospectMetadataRepository.save(metadata);

       //Set opens in step
       List<Map<String, Object>> steps = Arrays.asList(stepService.getStepsFromCampaign(campaignId));
       Campaign campaign = campaignRepository.findById(campaignId).get();

       Map<String, Object> step = new HashMap<>(steps.get(stepNumber));
       if(step.get("opens") == null){
           step.put("opens", 1);
           steps.set(stepNumber, step);
       }
       else{
           Integer stepOpens = (Integer) step.get("opens").getClass().cast(step.get("opens"));
           step.put("opens", stepOpens + 1);
           steps.set(stepNumber, step);
       }

       List<Map<String, Object>> mails = stepService.getMailsFromSteps(campaignId, stepNumber);
       Map<String, Object> mail = mails.get(mailNumber);
       if(mail.get("opens") == null){
           mail.put("opens", 1);
           mails.set(mailNumber, mail);
           step.put("mails", mails);
           steps.set(stepNumber, step);
       }
       else{
           Integer opens = (Integer) mail.get("opens").getClass().cast(mail.get("opens"));
           mail.put("opens", opens + 1);
           mails.set(mailNumber, mail);
           step.put("mails", mails);
           steps.set(stepNumber, step);
           logger.info(String.valueOf(mails));
       }
       Map[] msteps = steps.toArray(new Map[0]);
       campaign.setSteps(msteps);
       if(campaign.getTotalOpens() == null){
           campaign.setTotalOpens(1);
       }
       else{
           campaign.setTotalOpens(campaign.getTotalOpens() + 1);
       }
       campaignRepository.save(campaign);
    }


    public void trackClicks(String emailId){
        Emails email = emailsRepository.findById(emailId).get();
        String prospectId = email.getProspect().getId();
        String campaignId = email.getCampaign().getId();
        Integer stepNumber = email.getStep();
        Integer mailNumber = email.getMail();

        //Set opens in prospect metadata
        ProspectMetadata metadata = prospectMetadataRepository.findByProspectIdAndCampaignId(prospectId, campaignId);
        List<Engagement> engagements = engagementRepository.findByProspectMetadataId(metadata.getId());
        if(engagements.isEmpty()){
            Engagement engagement = new Engagement();
            engagement.setClicks(engagement.getClicks() + 1);
            engagement.setStepNumber(stepNumber + 1);
            engagement.setProspectMetadata(metadata);
            engagements.add(engagement);
            engagementRepository.save(engagement);
            email.setEngagement(engagement);
            emailsRepository.save(email);
        }
        else{
            Engagement existingEngagement = engagementRepository.findByProspectMetadataIdAndStepNumber(metadata.getId(), stepNumber + 1);
            if(existingEngagement != null){
                if(stepNumber + 1 == existingEngagement.getStepNumber()){
                    existingEngagement.setClicks(existingEngagement.getClicks() + 1);
                    engagementRepository.save(existingEngagement);
                }
            }
            else{
                Engagement engagement = new Engagement();
                engagement.setClicks(engagement.getClicks() + 1);
                engagement.setStepNumber(stepNumber + 1);
                engagement.setProspectMetadata(metadata);
                engagements.add(engagement);
                engagementRepository.save(engagement);
                email.setEngagement(engagement);
                emailsRepository.save(email);
            }

        }
        prospectMetadataRepository.save(metadata);

       //Set opens in step
       List<Map<String, Object>> steps = Arrays.asList(stepService.getStepsFromCampaign(campaignId));
       Campaign campaign = campaignRepository.findById(campaignId).get();

       Map<String, Object> step = new HashMap<>(steps.get(stepNumber));
       if(step.get("clicks") == null){
           step.put("clicks", 1);
           steps.set(stepNumber, step);
       }
       else{
           Integer stepClicks = (Integer) step.get("clicks").getClass().cast(step.get("clicks"));
           step.put("clicks", stepClicks + 1);
           steps.set(stepNumber, step);
       }

       List<Map<String, Object>> mails = stepService.getMailsFromSteps(campaignId, stepNumber);
       Map<String, Object> mail = mails.get(mailNumber);
       if(mail.get("clicks") == null){
           mail.put("clicks", 1);
           mails.set(mailNumber, mail);
           step.put("clicks", mails);
           steps.set(stepNumber, step);
       }
       else{
           Integer clicks = (Integer) mail.get("clicks").getClass().cast(mail.get("clicks"));
           mail.put("clicks", clicks + 1);
           mails.set(mailNumber, mail);
           step.put("mails", mails);
           steps.set(stepNumber, step);
       }
       Map[] msteps = steps.toArray(new Map[0]);
       campaign.setSteps(msteps);
       if(campaign.getTotalOpens() == null){
           campaign.setTotalClicks(1);
       }
       else{
           campaign.setTotalClicks(campaign.getTotalOpens() + 1);
       }
       campaignRepository.save(campaign);
    }
}
