package com.lemty.server.controller;

import java.util.List;
import java.util.Map;

import com.lemty.server.domain.Prospect;
import com.lemty.server.service.CampaignService;
import com.lemty.server.service.ProspectService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/prospects/prospect", produces = "application/json")
public class ProspectController{
    Logger logger = LoggerFactory.getLogger(ProspectController.class);
    private final ProspectService prospectService;
    private final CampaignService campaignService;

    public ProspectController(ProspectService prospectService, CampaignService campaignService){
        this.prospectService = prospectService;
        this.campaignService = campaignService;
    }

//     @GetMapping
//     public ResponseEntity<Map<String, Object>> getProspects(){
//         return prospectService.getProspects();
//     }

    @GetMapping(path = "/{listId}")
    public Map<String, Object> getProspectsFromList(
            @PathVariable("listId") String listId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        return prospectService.getProspectsFromList(listId, page, size);
    }

    @GetMapping(path = "/{listId}/count")
    public Map<String, Integer> prospectCountsList(@PathVariable("listId") String listId){
        return prospectService.getProspectCountsList(listId);
    }

    @GetMapping(path = "/{listId}/status")
    public ResponseEntity<Map<String, Object>> getProspectsListByStatus(
            @PathVariable("listId") String listId,
            @RequestParam(required = true) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        // Map<String, Object> response = ;
        try {
            Map<String, Object> response = prospectService.getProspectsListByStatus(listId, status, page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // return new ResponseEntity<>(prospectService.getProspectsFromUser(userId), HttpStatus.OK);
    }

    @GetMapping(path = "/user")
    public ResponseEntity<Map<String, Object>> getProspectsFromUser(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        // Map<String, Object> response = ;
        try {
            Map<String, Object> response = prospectService.getProspectsFromUser(userId, page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);

        }
        catch(Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // return new ResponseEntity<>(prospectService.getProspectsFromUser(userId), HttpStatus.OK);
    }

    @GetMapping(path = "/user/{userId}/total")
    public int totalProspectsUser(@PathVariable("userId") String userId){
        return prospectService.totalNumberofProspectsUser(userId);
    }

    @GetMapping(path = "/user/status")
    public ResponseEntity<Map<String, Object>> getProspectsByStatus(
            @RequestParam(required = true) String userId,
            @RequestParam(required = true) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        // Map<String, Object> response = ;
        try {
            Map<String, Object> response = prospectService.getProspectsByStatus(userId, status, page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // return new ResponseEntity<>(prospectService.getProspectsFromUser(userId), HttpStatus.OK);
    }


    @GetMapping(path = "/user/{userId}/count")
    public Map<String, Integer> prospectCounts(@PathVariable("userId") String userId){
        return prospectService.getProspectCountsUser(userId);
    }

    @GetMapping(path = "/not-in-campaign")
    public Map<String, Object> getProspectsNotInCampaign(
            @RequestParam("userId") String userId, 
            @RequestParam("campaignId") String campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        return prospectService.getProspectsNotInCampaign(userId, campaignId, page, size);
    }

    @GetMapping(path = "/campaign")
    public Map<String, Object> getProspectsFromCampaign(
            @RequestParam("campaignId") String campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        return prospectService.getProspectsFromCampaign(campaignId, page, size);
    }

    @GetMapping(path = "/campaign/total")
    public int totalProspectsCampaign(@RequestParam("campaignId") String campaignId){
        return prospectService.totalNumberofProspectsCampaign(campaignId);
    }

    @GetMapping(path = "/campaign/counts")
    public Map<String, Integer> getProspectCountsCampaign(@RequestParam("campaignId") String campaignId){
        return campaignService.getProspectCountsCampaign(campaignId);
    }

    @GetMapping(path = "/campaign/not-contacted")
    public ResponseEntity<Map<String, Object>> getNotContactedProspects(@RequestParam("campaignId") String campaignId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(campaignService.getNotContactedProspects(campaignId, page, size), HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/campaign/bounced")
    public ResponseEntity<Map<String, Object>> getBouncedProspects(@RequestParam("campaignId") String campaignId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(campaignService.getBouncedProspects(campaignId, page, size), HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/campaign/replied")
    public ResponseEntity<Map<String, Object>> getRepliedProspects(@RequestParam("campaignId") String campaignId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(campaignService.getRepliedProspects(campaignId, page, size), HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/campaign/not-replied")
    public ResponseEntity<Map<String, Object>> getNotRepliedProspects(@RequestParam("campaignId") String campaignId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(campaignService.getNotRepliedProspects(campaignId, page, size), HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/campaign/unsubscribed")
    public ResponseEntity<Map<String, Object>> getUnsubscribedProspects(@RequestParam("campaignId") String campaignId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return new ResponseEntity<>(campaignService.getUnsubsubscribedProspects(campaignId, page, size), HttpStatus.ACCEPTED);
    }

    @PostMapping
    public ResponseEntity<Prospect> addNewProspect(@RequestBody Prospect prospect, @RequestParam("listId") String listId, @RequestParam("userId") String userId){
        prospectService.addNewProspect(prospect, listId, userId);
        return new ResponseEntity<>(prospect, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{prospectId}")
    public ResponseEntity<Prospect> updateProspect(@RequestBody Prospect newProspect, @PathVariable("prospectId") String prospectId){
        prospectService.updateProspect(newProspect, prospectId);
        return new ResponseEntity<>(newProspect, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "{prospectId}")
    public void deleteProspect(@PathVariable("prospectId") String prospectId){
        prospectService.deleteProspect(prospectId);
    }

    @PostMapping("/deleteAll")
    public ResponseEntity<String> deleteMultipleProspect(@RequestBody List<String> prospectIds){
        prospectService.deleteMultipleProspects(prospectIds);
        return new ResponseEntity<>("Prospects Deleted", HttpStatus.ACCEPTED);
    }
}
