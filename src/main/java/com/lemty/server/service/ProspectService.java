package com.lemty.server.service;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.*;
import com.lemty.server.repo.*;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProspectService {
    Logger logger = LoggerFactory.getLogger(ProspectService.class);
    private final ProspectRepository prospectRepository;
    private final ProspectListRepository prospectListRepository;
    private final CampaignRepository campaignRepository;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final StepService stepService;
    private final UserRepo userRepo;
    private Scheduler scheduler;

    @Autowired
    public ProspectService(ProspectRepository prospectRepository, ProspectListRepository prospectListRepository, UserRepo userRepo, CampaignRepository campaignRepository, ProspectMetadataRepository prospectMetadataRepository, StepService stepService){
        this.prospectRepository = prospectRepository;
        this.prospectListRepository = prospectListRepository;
        this.campaignRepository = campaignRepository;
        this.userRepo = userRepo;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.stepService = stepService;
    }

    //List all prospects
    public List<Prospect> getProspects() {
        return prospectRepository.findAll();
    }

    //List prospects from prospect list
    public Map<String, Object> getProspectsFromList(String listId, int page, int size){
        List<Prospect> prospects = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByProspectListId(listId, paging);
        prospects = pageProspects.getContent();
        Map<String, Object> response = new HashMap<>();
        response.put("prospects", prospects);
        response.put("currentPage", pageProspects.getNumber());
        response.put("totalElements", pageProspects.getTotalElements());
        response.put("totalPages", pageProspects.getTotalPages());
        return response;
    }
    //List prospects from userId list
    public Map<String, Object> getProspectsFromUser(String userId, int page, int size){
        List<Prospect> prospects = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByAppUserId(userId, paging);
        prospects = pageProspects.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("prospects", prospects);
        response.put("currentPage", pageProspects.getNumber());
        response.put("totalElements", pageProspects.getTotalElements());
        response.put("totalPages", pageProspects.getTotalPages());

        return response;
    }

    public Map<String, Object> getProspectsByStatus(String userId, String status, int page, int size){
        List<Prospect> prospects = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByAppUserIdAndStatusIs(userId, status, paging);
        prospects = pageProspects.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("prospects", prospects);
        response.put("currentPage", pageProspects.getNumber());
        response.put("totalElements", pageProspects.getTotalElements());
        response.put("totalPages", pageProspects.getTotalPages());

        return response;
    }

    public Map<String, Object> getProspectsListByStatus(String listId, String status, int page, int size){
        List<Prospect> prospects = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByProspectListIdAndStatus(listId, status, paging);
        prospects = pageProspects.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("prospects", prospects);
        response.put("currentPage", pageProspects.getNumber());
        response.put("totalElements", pageProspects.getTotalElements());
        response.put("totalPages", pageProspects.getTotalPages());

        return response;
    }

    //list prospects from campaign
    public Map<String, Object> getProspectsFromCampaign(String campaignId, int page, int size){
        List<Prospect> prospects = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByCampaignsId(campaignId, paging);
        prospects = pageProspects.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("prospects", prospects);
        response.put("currentPage", pageProspects.getNumber());
        response.put("totalElements", pageProspects.getTotalElements());
        response.put("totalPages", pageProspects.getTotalPages());

        return response;
    }

   public int totalNumberofProspectsUser (String userId) {
        Pageable paging = PageRequest.of(Integer.MAX_VALUE, Integer.MAX_VALUE);
        List<Prospect> prospects = new ArrayList<>();

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByAppUserId(userId, paging);
        prospects = pageProspects.getContent();
        return prospects.size();
    }

    // list prospects not in particular campaign
    public Map<String, Object> getProspectsNotInCampaign(String userId, String campaignId, int page, int size){
        List<Prospect> prospectsNotInCampaign = new ArrayList<>();
        Campaign campaign = campaignRepository.findById(campaignId).get();

        Pageable paging = PageRequest.of(page, size);
        List<Prospect> allProspects = new ArrayList<>();

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByAppUserId(userId, paging);
        allProspects = pageProspects.getContent();

        logger.info(String.valueOf(pageProspects.getTotalElements()));

         for(int i=0; i < allProspects.size(); i++){
             Prospect prospect = allProspects.get(i);

             if(prospect.getCampaigns().isEmpty() && !prospect.getUnsubscribed()){
                 prospectsNotInCampaign.add(prospect);
             }

             for(int j=0; j < prospect.getCampaigns().size(); j++){
                if(!Objects.equals(prospect.getCampaigns().get(j).getId(), campaignId) && !prospect.getUnsubscribed()){
                    prospectsNotInCampaign.add(prospect);
                }
            }
         }
         Map<String, Object> data = new HashMap<>();
         data.put("prospects", prospectsNotInCampaign);
         data.put("totalElements", pageProspects.getTotalElements());
         data.put("totalPages", pageProspects.getTotalPages());
         return data;
    }

    public void addProspectToList(String prospectId, String listId){
        ProspectList prospectList = prospectListRepository.findById(listId).get();
        Prospect prospect = prospectRepository.findById(prospectId).get();
        prospect.setProspectList(prospectList);
        prospectRepository.save(prospect);
    }

    public void addProspectToCampaign(String prospectId, String campaignId){
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Campaign> campaigns = new ArrayList<>();
        campaigns.add(campaign);

        Prospect prospect = prospectRepository.findById(prospectId).get();
        prospect.setCampaigns(campaigns);
        ProspectMetadata existing = prospectMetadataRepository.findByProspectIdAndCampaignId(prospectId, campaignId);
        
        if(existing == null){
            List<ProspectMetadata> metadatas = prospect.getProspectMetadatas();
            ProspectMetadata metadata = new ProspectMetadata();
            metadata.setCampaignId(campaignId);
            metadata.setStatus("IN_CAMPAIGN");
            metadata.setProspect(prospect);
            prospectMetadataRepository.save(metadata);
            metadatas.add(metadata);
            prospect.setProspectMetadatas(metadatas);
        }
        else{
            logger.info(String.valueOf(existing));
        }

        prospectRepository.saveAndFlush(prospect);

        campaign.setProspectCount(totalNumberofProspectsCampaign(campaignId));
        campaignRepository.save(campaign);
    }

    public void addMultipleProspectsToCampaign(List<String> prospectId, String campaignId){
        List<Prospect> prospects = new ArrayList<>();
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Campaign> campaigns = new ArrayList<>();
        campaigns.add(campaign);
        for(int i=0; i < prospectId.size(); i++){
            Prospect prospect = prospectRepository.findById(prospectId.get(i)).get();

            prospect.setCampaigns(campaigns);

            ProspectMetadata metadata = new ProspectMetadata();
            metadata.setCampaignId(campaignId);
            metadata.setProspect(prospect);
            metadata.setStatus("IN_CAMPAIGN");
            metadata.setProspect(prospect);

            prospectMetadataRepository.save(metadata);
            prospects.add(prospect);
        }
        prospectRepository.saveAllAndFlush(prospects);

        campaign.setProspectCount(totalNumberofProspectsCampaign(campaignId));
        campaignRepository.save(campaign);
    }


    public void removeProspectFromCampaign(String campaignId){
        Campaign campaign = campaignRepository.findById(campaignId).get();
        Pageable paging = PageRequest.of(0, Integer.MAX_VALUE);

        List<Prospect> prospects = new ArrayList<>();
        Page<Prospect> prospect = prospectRepository.findByCampaignsId(campaignId, paging);
        prospects = prospect.getContent();

        for(int i=0; i < prospects.size(); i++){
            Prospect prospect2 = prospects.get(i);
            ProspectMetadata metadata = prospectMetadataRepository.findByProspectIdAndCampaignId(prospect2.getId(), campaignId);
            metadata.setProspect(null);
            prospectMetadataRepository.save(metadata);
            prospect2.setProspectMetadatas(null);
            prospectRepository.save(prospect2);
            prospectMetadataRepository.deleteById(metadata.getId());
        }

        campaign.setProspect(null);
        campaignRepository.save(campaign);
    }

    public int totalNumberofProspectsCampaign (String campaignId) {
        Pageable paging = PageRequest.of(0, Integer.MAX_VALUE);
        List<Prospect> prospects = new ArrayList<>();

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByCampaignsId(campaignId, paging);
        prospects = pageProspects.getContent();
        return prospects.size();
    }

    //Add new Prospect
    public void addNewProspect(Prospect prospect, String listId, String userId){
        // Optional<Prospect> prospectOptional = prospectRepository.findById(prospect.getId());
        AppUser appUser = userRepo.findById(userId).get();
        prospect.setAppUser(appUser);
        prospect.setStatus("not-contacted");
        prospectRepository.saveAndFlush(prospect);
        if(listId != ""){
            addProspectToList(prospect.getId(), listId);
        }
    }

    //Add multiple Prospect
    public void addMultipleProspect(List<Prospect> prospect, String listId, String userId){
        // Optional<Prospect> prospectOptional = prospectRepository.findById(prospect.getId());
        //ProspectList prospectList = prospectListRepository.findById(listId).get();
        AppUser appUser = userRepo.findById(userId).get();
        for(int i=0; i < prospect.size(); i++){
            prospect.get(i).setAppUser(appUser);
            prospect.get(i).setStatus("not-contacted");
        }
        prospectRepository.saveAll(prospect);
        for(int i=0; i < prospect.size(); i++){
            if(listId != ""){
                addProspectToList(prospect.get(i).getId(), listId);
            }
        }
    }

    public void unsubscribeProspect(String prospectId){
        Prospect prospect = prospectRepository.findById(prospectId).get();
        prospect.setCampaigns(null);
        prospect.setUnsubscribed(true);
        prospect.setStatus("unsubscribed");
        prospectRepository.save(prospect);
    }

    //Update Prospect
    public void updateProspect(Prospect newProspect, String prospectId){
        prospectRepository.findById(prospectId)
                .map(prospect -> {
                    prospect.setFirstName(newProspect.getFirstName());
                    prospect.setLastName(newProspect.getLastName());
                    prospect.setProspectEmail(newProspect.getProspectEmail());
                    prospect.setProspectCompany(newProspect.getProspectCompany());
                    prospect.setProspectMobileNumber(newProspect.getProspectMobileNumber());
                    prospect.setProspectAccount(newProspect.getProspectAccount());
                    prospect.setProspectCompanyEmail(newProspect.getProspectCompanyEmail());
                    prospect.setProspectDepartment(newProspect.getProspectDepartment());
                    prospect.setProspectTitle(newProspect.getProspectTitle());
                    prospect.setProspectCompanyDomain(newProspect.getProspectCompanyDomain());
                    prospect.setProspectLinkedinUrl(newProspect.getProspectLinkedinUrl());
                    prospect.setProspectTwitterUrl(newProspect.getProspectTwitterUrl());
                    prospect.setProspectLocation(newProspect.getProspectLocation());
                    return prospectRepository.save(prospect);
                })
                .orElseGet(() -> {
                    newProspect.setId(prospectId);
                    return prospectRepository.save(newProspect);
                });
    }

    //Delete Prospect
    public void deleteProspect(String prospectId){
        boolean exists = prospectRepository.existsById(prospectId);
        if(!exists){
            throw new IllegalStateException(
                    "prospect with id " + prospectId + " does not exists"
            );
        }
        Prospect prospect = prospectRepository.findById(prospectId).get();
        prospect.setCampaigns(null);
        prospectRepository.save(prospect);
        List<Campaign> campaigns = prospect.getCampaigns();
        for(int j=0; j < campaigns.size(); j++){
            campaigns.get(j).setProspectCount(totalNumberofProspectsCampaign(campaigns.get(j).getId()));
            campaignRepository.save(campaigns.get(j));
        }
        prospectRepository.deleteById(prospectId);
    }

    //Delete Multiple Prospect
    public void deleteMultipleProspects(List<String> prospectId){
        for(int i=0; i < prospectId.size(); i++){
            Prospect prospect = prospectRepository.findById(prospectId.get(i)).get();
            List<Campaign> campaigns = prospect.getCampaigns();
            if(prospect.getCampaigns() != null){
                for(int j=0; j < campaigns.size(); j++){
                    Campaign campaign = campaigns.get(j);
                    campaign.setProspectCount(totalNumberofProspectsCampaign(campaign.getId()));
                    List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaign.getId()));

                    for(int l=0; l < steps.size(); l++){
                        Integer stepNumber = (Integer) steps.get(l).get("stepNumber").getClass().cast(steps.get(l).get("stepNumber"));
                        JobKey jobKey = new JobKey(prospect.getProspectEmail() + "-" + stepNumber + "-" + campaign.getId(), campaign.getId());
                        try {
                            scheduler.deleteJob(jobKey);
                        } catch (SchedulerException e) {
                            e.printStackTrace();
                        }
                    }
                    campaignRepository.save(campaigns.get(j));
                }
                prospect.setCampaigns(null);
            }
            prospectRepository.save(prospect);
        }
        prospectRepository.deleteAllById(prospectId);
    }

    //Get campaigns from prospect
    public List<Campaign> getCampaignFromProspect(String prospectId){
        Prospect prospect = prospectRepository.findById(prospectId).get();
        return prospect.getCampaigns();
    }

    public Map<String, Integer> getProspectCountsUser(String userId){
        Map<String, Integer> prospectCounts = new HashMap<>();
        Pageable paging = PageRequest.of(0, Integer.MAX_VALUE);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByAppUserId(userId, paging);
        Page<Prospect> bounced = prospectRepository.findByAppUserIdAndStatusIs(userId, "bounced", paging);
        Page<Prospect> unsubscribed = prospectRepository.findByAppUserIdAndStatusIs(userId, "unsubscribed",paging);
        Page<Prospect> notContacted = prospectRepository.findByAppUserIdAndStatusIs(userId, "not contacted", paging);
        Page<Prospect> replied = prospectRepository.findByAppUserIdAndStatusIs(userId, "replied", paging);
        Page<Prospect> notReplied = prospectRepository.findByAppUserIdAndStatusIs(userId, "not replied", paging);

        prospectCounts.put("all", pageProspects.getContent().size());
        prospectCounts.put("bounced", bounced.getContent().size());
        prospectCounts.put("unsubscribed", unsubscribed.getContent().size());
        prospectCounts.put("not_contacted", notContacted.getContent().size());
        prospectCounts.put("replied", replied.getContent().size());
        prospectCounts.put("not_replied", notReplied.getContent().size());

        return prospectCounts;
    }

    public Map<String, Integer> getProspectCountsList(String listId){
        Map<String, Integer> prospectCounts = new HashMap<>();
        Pageable paging = PageRequest.of(0, Integer.MAX_VALUE);

        Page<Prospect> pageProspects;
        pageProspects = prospectRepository.findByProspectListId(listId, paging);
        Page<Prospect> bounced = prospectRepository.findByProspectListIdAndStatus(listId, "bounced", paging);
        Page<Prospect> unsubscribed = prospectRepository.findByAppUserIdAndStatusIs(listId, "unsubscribed",paging);
        Page<Prospect> notContacted = prospectRepository.findByAppUserIdAndStatusIs(listId, "not-contacted", paging);
        Page<Prospect> replied = prospectRepository.findByProspectListIdAndStatus(listId, "replied", paging);
        Page<Prospect> notReplied = prospectRepository.findByProspectListIdAndStatus(listId, "not-replied", paging);

        prospectCounts.put("all", pageProspects.getContent().size());
        prospectCounts.put("bounced", bounced.getContent().size());
        prospectCounts.put("unsubscribed", unsubscribed.getContent().size());
        prospectCounts.put("not_contacted", notContacted.getContent().size());
        prospectCounts.put("replied", replied.getContent().size());
        prospectCounts.put("not_replied", notReplied.getContent().size());

        return prospectCounts;
    }

    //Search
    public List<Prospect> searchByKeyword(String keyword) {
        return prospectRepository.findByKeyword(keyword.toLowerCase());
    }
}
