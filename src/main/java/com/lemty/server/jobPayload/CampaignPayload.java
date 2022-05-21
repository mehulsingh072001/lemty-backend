package com.lemty.server.jobPayload;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CampaignPayload {

    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private List<String> selectedProspects;
    private String selectedCampaign;
    private String startAt;

    public List<String> getSelectedProspects() {
        return selectedProspects;
    }

    public void setSelectedProspects(List<String> selectedProspects) {
        this.selectedProspects = selectedProspects;
    }

    public String getSelectedCampaign() {
        return selectedCampaign;
    }

    public void setSelectedCampaign(String selectedCampaign) {
        this.selectedCampaign = selectedCampaign;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    @Override
    public String toString() {
        return "CampaignPayload{" +
                "selectedProspects=" + selectedProspects +
                ", selectedCampaign='" + selectedCampaign + '\'' +
                ", startAt='" + startAt + '\'' +
                '}';
    }
}
