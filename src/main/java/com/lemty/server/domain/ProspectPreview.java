package com.lemty.server.domain;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProspectPreview implements Serializable{

    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    private String prospectEmail;
    private List<Map<String, Object>> previews;
    private String campaignId;
    private String prospectId;
    private Boolean edited = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProspectEmail() {
        return prospectEmail;
    }

    public void setProspectEmail(String prospectEmail) {
        this.prospectEmail = prospectEmail;
    }

    public List<Map<String, Object>> getPreviews() {
        return previews;
    }

    public void setPreviews(List<Map<String, Object>> previews) {
        this.previews = previews;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getProspectId() {
        return prospectId;
    }

    public void setProspectId(String prospectId) {
        this.prospectId = prospectId;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }


    @Override
    public String toString() {
        return "ProspectPreview{" +
                "id='" + id + '\'' +
                ", prospectEmail='" + prospectEmail + '\'' +
                ", campaignId='" + campaignId + '\'' +
                '}';
    }
}
