package com.lemty.server.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lemty.server.domain.ProspectMetadata;
import com.lemty.server.repo.ProspectMetadataRepository;
import com.lemty.server.service.ProspectService;
import com.lemty.server.service.StepService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ReportsHelper {
    Logger logger = LoggerFactory.getLogger(ReportsHelper.class);
    private final StepService stepService;
    private final ProspectMetadataRepository prospectMetadataRepository;
    private final ProspectService prospectService;

    public ReportsHelper(StepService stepService, ProspectMetadataRepository prospectMetadataRepository, ProspectService prospectService){
        this.stepService = stepService;
        this.prospectMetadataRepository = prospectMetadataRepository;
        this.prospectService = prospectService;
    }

    public List<Map<String, Object>> stepMetrics(String campaignId){
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaignId));
        List<Map<String, Object>> allMetrics = new ArrayList<>();

        for(int i=0; i < steps.size(); i++){
            Map<String, Object> step = steps.get(i);
            Integer emails = stepService.getMailsFromSteps(campaignId, i).size();

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("emails", emails);
            metrics.put("stepNumber", step.get("stepNumber"));
            metrics.put("opens", step.get("opens"));
            metrics.put("clicks", step.get("clicks"));
            metrics.put("replies", step.get("replies"));
            allMetrics.add(metrics);
        }

        return allMetrics;
    }

    public Map<String, Object> campaignOverview(String campaignId){
        Pageable paging = PageRequest.of(0, Integer.MAX_VALUE);

        List<ProspectMetadata> inCampaignMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "IN_CAMPAIGN", paging).getContent();
        List<ProspectMetadata> completedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "COMPLETED_WITHOUT_REPLY", paging).getContent();
        List<ProspectMetadata> stoppedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "STOPPED", paging).getContent();
        List<ProspectMetadata> openedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "OPENED", paging).getContent();
        List<ProspectMetadata> repliedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "REPLIED", paging).getContent();
        List<ProspectMetadata> clickedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "CLICKED", paging).getContent();

        List<ProspectMetadata> bouncedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "BOUNCED", paging).getContent();
        List<ProspectMetadata> unsubscribedMetadatas = prospectMetadataRepository.findByCampaignIdAndStatusIs(campaignId, "UNSUBSCRIBED", paging).getContent();
        Integer totalProspects = prospectService.totalNumberofProspectsCampaign(campaignId);

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalProspects", totalProspects);
        overview.put("inCampaign", inCampaignMetadatas.size());
        overview.put("completedNoReply", completedMetadatas.size());
        overview.put("stopped", stoppedMetadatas.size());
        overview.put("unsubscribed", unsubscribedMetadatas.size());
        overview.put("bounced", bouncedMetadatas.size());
        overview.put("opened", openedMetadatas.size());
        overview.put("replied", repliedMetadatas.size());
        overview.put("clicked", clickedMetadatas.size());

        return overview;
    }
}
