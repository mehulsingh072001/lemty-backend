package com.lemty.server.controller;

import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.Unsubscribe;
import com.lemty.server.service.UnsubscribeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/unsubscribe")
public class UnsubscribeController {
    private final UnsubscribeService unsubscribeService;

    public UnsubscribeController(UnsubscribeService unsubscribeService) {
        this.unsubscribeService = unsubscribeService;
    }

    @GetMapping
    public Unsubscribe getUnsubscribeBody(@RequestParam("userId") String userId) {
        return unsubscribeService.getUnsubscribe(userId);
    }

    @PostMapping
    public Unsubscribe addNewUnsubscribeBody(@RequestBody Unsubscribe unsubscribe, @RequestParam("userId") String userId){
        return unsubscribeService.addNewUnsubscribe(unsubscribe, userId);
    }

    @PutMapping
    public Unsubscribe updateUnsubscribeBody(@RequestBody Unsubscribe unsubscribe, @RequestParam("unsubId") String unsubId){
        return unsubscribeService.updateUnsubscribe(unsubscribe, unsubId);
    }

    @DeleteMapping
    public void deleteSignature(@RequestParam("unsubId") String unsubId){
        unsubscribeService.deleteUnsub(unsubId);
    }

    @GetMapping(path = "prospect/{prospectId}")
    public ResponseEntity<String> getCampaigns(@PathVariable("prospectId") String prospectId){
        unsubscribeService.unsubscribeProspect(prospectId);
        return new ResponseEntity<>("Prospect Unsubscribed", HttpStatus.ACCEPTED);
    }
}
