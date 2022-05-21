package com.lemty.server.controller;

import com.lemty.server.domain.Step;
import com.lemty.server.service.StepService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/steps")
public class StepController{
    private final StepService stepService;

    public StepController(StepService stepService) {

        this.stepService = stepService;
    }

    @GetMapping
    public List<Step> all(){
        return stepService.getAll();
    }

    @GetMapping(path = "/{campaignId}")
    public Map[] getFromCampaign(@PathVariable("campaignId") String campaignId){
        return stepService.getStepsFromCampaign(campaignId);
    }

    @GetMapping(path = "mails/{campaignId}/{index}")
    public List<Map<String, Object>> getMailsFromSteps(@PathVariable("campaignId") String campaignId, @PathVariable("index") int index){
        return stepService.getMailsFromSteps(campaignId, index);
    }


    // @PostMapping(path = "/{campaignId}")
    // public ResponseEntity<Step> addNewStep(@RequestBody Step newStep, @PathVariable("campaignId") String campaignId){
    //     stepService.addNewStep(newStep, campaignId);
    //     return new ResponseEntity<>(newStep, HttpStatus.CREATED);
    // }

    @PutMapping(path = "/{stepId}")
    public ResponseEntity<Step> replaceStep(@RequestBody Step newStep, @PathVariable("stepId") String stepId){
        stepService.updateStep(newStep, stepId);
        return new ResponseEntity<>(newStep, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{stepId}")
    public void deleteProspectList(@PathVariable("stepId") String stepId){
        stepService.deleteStep(stepId);
    }
}

