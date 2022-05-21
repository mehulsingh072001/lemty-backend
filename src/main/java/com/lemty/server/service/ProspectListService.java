package com.lemty.server.service;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.ProspectList;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.ProspectListRepository;
import com.lemty.server.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProspectListService {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final ProspectListRepository prospectListRepository;
    private final UserRepo userRepo;
    private final CampaignRepository campaignRepository;

    @Autowired
    public ProspectListService(ProspectListRepository prospectListRepository, UserRepo userRepo, CampaignRepository campaignRepository){
        this.prospectListRepository = prospectListRepository;
        this.userRepo = userRepo;
        this.campaignRepository = campaignRepository;
    }

    //List all prospect lists in user
    @Transactional
    public List<ProspectList> getProspectLists(String userId) {
        //return prospectListRepository.findAll();
        return prospectListRepository.findByAppUserId(userId);
    }

    //List all prospect lists in campaign
    public ProspectList getListFromCampaign(String campaignId) {
        return prospectListRepository.findByCampaignId(campaignId);
    }

    //List single prospect list
    public ProspectList getSingleList(String listId) {
        return prospectListRepository.findById(listId).get();
    }

    //Add new ProspectList
    public void addNewProspectList(ProspectList prospectList, String userId){
        Optional<ProspectList> prospectListOptional = prospectListRepository.findProspectListByName(prospectList.getName());
        if(prospectListOptional.isPresent()){
            throw new IllegalStateException("prospect list exists");
        }
        AppUser appUser = userRepo.findById(userId).get();
        prospectList.setAppUser(appUser);
        prospectListRepository.save(prospectList);

    }

    //Update ProspectList
    public void updateProspectList(ProspectList prospectList, String prospectListId){
        ProspectList existingProspectList = prospectListRepository.findById(prospectListId).get();
        existingProspectList.setId(prospectList.getId());
        existingProspectList.setListName(prospectList.getName());
        prospectListRepository.save(existingProspectList);
    }

    //ProspectList to campaign
    public void addListToCampaign(String prospectListId, String campaignId){
        ProspectList existingProspectList = prospectListRepository.findById(prospectListId).get();
        Campaign campaign = campaignRepository.findById(campaignId).get();
        List<Campaign> campaigns = new ArrayList<>();
        campaigns.add(campaign);

        existingProspectList.setCampaign(campaigns);
        prospectListRepository.save(existingProspectList);
    }

    //Delete ProspectList
    public void deleteProspectList(String prospectListId){
        boolean exists = prospectListRepository.existsById(prospectListId);
        if(!exists){
            throw new IllegalStateException(
                    "list with id " + prospectListId + " does not exists"
            );
        }
        prospectListRepository.deleteById(prospectListId);
    }

}
