package com.lemty.server.controller;

import java.util.List;

import com.lemty.server.domain.ProspectList;
import com.lemty.server.service.ProspectListService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/prospects/prospect_lists")
public class ProspectListController{
    private final ProspectListService prospectListService;

    public ProspectListController(ProspectListService prospectListService){
        this.prospectListService = prospectListService;
    }

    @GetMapping("/list")
    public List<ProspectList> getProspectLists(@RequestParam(value="userId") String userId){
        return prospectListService.getProspectLists(userId);
    }

    @GetMapping("/list/campaign")
    public ProspectList getListFromCampaign(@RequestParam(value="campaignId") String campaignId){
        return prospectListService.getListFromCampaign(campaignId);
    }

    @GetMapping(path = "{prospectListId}")
    public ProspectList getSingleList(@PathVariable String prospectListId){
        return prospectListService.getSingleList(prospectListId);
    }

    @PostMapping(path = "{userId}")
    public ResponseEntity<ProspectList> addNewProspectList(@RequestBody ProspectList prospectList, @PathVariable("userId") String userId){
        prospectListService.addNewProspectList(prospectList, userId);
        return new ResponseEntity<>(prospectList, HttpStatus.CREATED);
    }

    @PutMapping(path = "{prospectListId}")
    public ResponseEntity<ProspectList> updateProspectList(@RequestBody ProspectList prospectList, @PathVariable("prospectListId") String prospectListId){
        prospectListService.updateProspectList(prospectList, prospectListId);
        return new ResponseEntity<>(prospectList, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ProspectList> addListToCampaign(@RequestParam("prospectListId") String prospectListId, @RequestParam("campaignId") String campaignId){
        prospectListService.addListToCampaign(prospectListId, campaignId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping(path = "{prospectListId}")
    public void deleteProspectList(@PathVariable("prospectListId") String prospectListId){
        prospectListService.deleteProspectList(prospectListId);
    }
}
