package com.lemty.server.helpers;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.domain.EmailSignature;
import com.lemty.server.domain.Prospect;
import com.lemty.server.domain.ProspectPreview;
import com.lemty.server.domain.Unsubscribe;
import com.lemty.server.jobPayload.CampaignPayload;
import com.lemty.server.repo.CampaignRepository;
import com.lemty.server.repo.ProspectRepository;
import com.lemty.server.service.DeliveribilitySettingsService;
import com.lemty.server.service.EmailSignatureService;
import com.lemty.server.service.StepService;
import com.lemty.server.service.UnsubscribeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

@Component
public class PreviewGeneraterHelper {
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final CampaignRepository campaignRepository;
    private final StepService stepService;
    private final ProspectRepository prospectRepository;
    private final PlaceholderHelper placeholderHelper;
    private final EmailSignatureService emailSignatureService;
    private final UnsubscribeService unsubscribeService;
    private final StartDateHelper startDateHelper;
    private final DeliveribilitySettingsService deliveribilitySettingsService;

    public PreviewGeneraterHelper(CampaignRepository campaignRepository, StepService stepService, ProspectRepository prospectRepository, PlaceholderHelper placeholderHelper, EmailSignatureService emailSignatureService, UnsubscribeService unsubscribeService, StartDateHelper startDateHelper, DeliveribilitySettingsService deliveribilitySettingsService) {
        this.campaignRepository = campaignRepository;
        this.stepService = stepService;
        this.prospectRepository = prospectRepository;
        this.placeholderHelper = placeholderHelper;
        this.emailSignatureService = emailSignatureService;
        this.unsubscribeService = unsubscribeService;
        this.startDateHelper = startDateHelper;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
    }

    public List<ProspectPreview> generate(CampaignPayload campaign, String userId){
        List<Map<String, Object>> steps = List.of(stepService.getStepsFromCampaign(campaign.getSelectedCampaign()));
        List<String> prospects = campaign.getSelectedProspects();
        Campaign campaign2 = campaignRepository.findById(campaign.getSelectedCampaign()).get();

        DeliveribilitySettings deliveribilitySettings = deliveribilitySettingsService.getDeliveribilitySettings(userId);
        int minSeconds = deliveribilitySettings.getMinInterval();
        int maxSeconds = deliveribilitySettings.getMaxInterval();
        int seconds = deliveribilitySettings.getSeconds();

        List<ProspectPreview> prospectPreviews = new ArrayList<>();

      // Map<String, Map<String, Map<String, Object>>> previewArray = new HashMap<>();

        List<EmailSignature> signatures = emailSignatureService.getSignatures(userId);
        Unsubscribe unsubscribe = unsubscribeService.getUnsubscribe(userId);

      for(int i=0; i < prospects.size(); i++){
        ProspectPreview prospectPreview = new ProspectPreview();

         Prospect prospect = prospectRepository.findById(prospects.get(i)).get();
         prospectPreview.setProspectEmail(prospect.getProspectEmail());
         prospectPreview.setProspectId(prospect.getId());
         prospectPreview.setCampaignId(campaign.getSelectedCampaign());
         List<Map<String, Object>> previews = new ArrayList<>();

         for (int j=0; j < steps.size(); j++){
            Map<String, Object> mailObject = new HashMap<>();
            List<Map<String, Object>> mails = stepService.getMailsFromSteps(campaign.getSelectedCampaign(), j);

            Integer mailNumber = i % mails.size();

            String subject = (String) mails.get(i % mails.size()).get("subject").getClass().cast(mails.get(i % mails.size()).get("subject"));
            String body = (String) mails.get(i % mails.size()).get("body").getClass().cast(mails.get(i % mails.size()).get("body"));
            subject = placeholderHelper.fieldsReplacer(subject, prospect);
            body = placeholderHelper.fieldsReplacer(body, prospect);

            //Get start date
             Integer dayGap = Integer.valueOf(String.valueOf(steps.get(j).get("dayGap")));
             Integer hourGap = Integer.valueOf(String.valueOf(steps.get(j).get("hourGap")));
             Integer minuteGap = Integer.valueOf(String.valueOf(steps.get(j).get("minuteGap")));
             String timezone = campaign2.getTimezone();
             ZonedDateTime startDate = startDateHelper.dateParser(campaign, timezone, dayGap, hourGap, minuteGap);

             if(signatures.size() > 0){
                 body = placeholderHelper.signatureReplacer(body, signatures.get(0));
             }
             if(unsubscribe != null){
                 body = placeholderHelper.unsubLinkReplacer(body, prospect.getId(), unsubscribe);
             }

             String window = String.valueOf(steps.get(j).get("startHour")) + "-" + String.valueOf(steps.get(j).get("endHour"));

              Object days = steps.get(j).get("days");
              ArrayList list = (ArrayList) days.getClass().cast(days);
              StringJoiner joiner = new StringJoiner(",");
              for(int l = 0; l < list.size(); l++){
                  joiner.add(list.get(l).toString());
              }
              String str = joiner.toString();

              mailObject.put("from", steps.get(j).get("whichEmail"));
              mailObject.put("to", prospectPreview.getProspectEmail());
              mailObject.put("subject", subject);
              mailObject.put("body", body);
              mailObject.put("stepNumber", steps.get(j).get("stepNumber"));
              mailObject.put("mailNumber", mailNumber);
              mailObject.put("startDate", startDate);
              mailObject.put("window", window);
              mailObject.put("days", str);
              mailObject.put("dayGap", days);
              mailObject.put("hourGap", hourGap);
              mailObject.put("minuteGap", minuteGap);
              mailObject.put("edited", false);

              previews.add(mailObject);
         }
        prospectPreview.setPreviews(previews);
        prospectPreviews.add(prospectPreview);
      }

      return prospectPreviews;
   }
}
