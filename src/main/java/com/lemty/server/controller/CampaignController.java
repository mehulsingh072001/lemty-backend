package com.lemty.server.controller;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.ProspectPreview;
import com.lemty.server.helpers.PreviewGeneraterHelper;
import com.lemty.server.jobPayload.CampaignPayload;
import com.lemty.server.service.CampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/campaigns")
public class CampaignController{
    Logger logger = LoggerFactory.getLogger(CampaignController.class);
    private final CampaignService campaignService;
    private final PreviewGeneraterHelper previewGeneraterHelper;

    public CampaignController(CampaignService campaignService, PreviewGeneraterHelper previewGeneraterHelper) {
        this.campaignService = campaignService;
        this.previewGeneraterHelper = previewGeneraterHelper;
    }

    @GetMapping(path = "/{userId}")
    public List<Campaign> all(@PathVariable("userId") String userId){
        return campaignService.getCampaign(userId);
    }

    @GetMapping(path = "/single/{campaignId}")
    public Campaign single(@PathVariable("campaignId") String campaignId){
        return campaignService.getSingleCampaign(campaignId);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Campaign> addNewCampaign(@RequestBody Campaign newcampaign, @PathVariable("userId") String userId){
        campaignService.addNewCampaign(newcampaign, userId);
        return new ResponseEntity<>(newcampaign, HttpStatus.CREATED);
    }

    @PostMapping("/generateProspectPreview")
    public ResponseEntity<List<ProspectPreview>> generateProspectPreview(@RequestBody CampaignPayload campaignPayload, @RequestParam("userId") String userId){
        List<ProspectPreview> response = previewGeneraterHelper.generate(campaignPayload, userId);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PutMapping("update/{campaignId}")
    public ResponseEntity<Campaign> updateCampaign(@RequestBody Campaign newCampaign, @PathVariable("campaignId") String campaignId){
        campaignService.updateCampaignSettings(newCampaign, campaignId);
        return new ResponseEntity<>(newCampaign, HttpStatus.ACCEPTED);
    }

    @PutMapping(path = "/name")
    public ResponseEntity<Campaign> updateCampaignName(@RequestBody Campaign newCampaign, @RequestParam("campaignId") String campaignId ){
        campaignService.updateCampaignName(newCampaign, campaignId);
        return new ResponseEntity<>(newCampaign, HttpStatus.CREATED);
    }

    @PutMapping(path = "/steps")
    public ResponseEntity<Campaign> updateCampaignSteps(@RequestBody Campaign newCampaign, @RequestParam("campaignId") String campaignId ){
        campaignService.updateCampaignSteps(newCampaign, campaignId);
        return new ResponseEntity<>(newCampaign, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{campaignId}")
    public void deleteCampaign(@PathVariable("campaignId") String campaignId){
        campaignService.deleteCampaign(campaignId);
    }

    @GetMapping(path = "/pause")
    public void pauseCampaign(@RequestParam("campaignId") String campaignId){
        campaignService.pauseCampaign(campaignId);
    }

    @GetMapping(path = "/resume")
    public void resumeCampaign(@RequestParam("campaignId") String campaignId){
        campaignService.resumeCampaign(campaignId);
    }
}
