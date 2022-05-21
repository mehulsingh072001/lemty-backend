package com.lemty.server.jobPayload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lemty.server.domain.Campaign;
import com.lemty.server.domain.DeliveribilitySettings;

import java.util.List;
import java.util.Map;

public class StepPayload {
    private String from;
    private List<Map<String, Object>> mails;
    private String campaignId;
    private Integer stepNumber;
    private String userId;

    public StepPayload(String from, List<Map<String, Object>> mails, String campaignId, String userId, Integer stepNumber) {
        this.from = from;
        this.mails = mails;
        this.campaignId = campaignId;
        this.userId = userId;
        this.stepNumber = stepNumber;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMails(List<Map<String, Object>> mails) {
        this.mails = mails;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Map<String, Object>> getMails() {
        return mails;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    @Override
    public String toString() {
        return "StepPayload{" +
                "from='" + from + '\'' +
                ", mails=" + mails +
                ", campaignId='" + campaignId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
