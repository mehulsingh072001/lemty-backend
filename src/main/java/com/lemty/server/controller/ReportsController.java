package com.lemty.server.controller;

import com.lemty.server.helpers.ReportsHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/reports")
public class ReportsController {
    private final ReportsHelper reportsHelper;

    public ReportsController(ReportsHelper reportsHelper) {
        this.reportsHelper = reportsHelper;
    }

    @GetMapping("steps")
    public List<Map<String, Object>> stepMetrics(@RequestParam("campaignId") String campaignId){
        return reportsHelper.stepMetrics(campaignId);
    }

    @GetMapping("overview")
    public Map<String, Object> campaignOverview(@RequestParam("campaignId") String campaignId){
        return reportsHelper.campaignOverview(campaignId);
    }
}
