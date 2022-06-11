package com.lemty.server.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

@Entity
public class Engagement {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private Integer opens = 0;
    private Integer clicks = 0;
    private Integer replies = 0;
    private Integer stepNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "prospect_metadata_id")
    private ProspectMetadata prospectMetadata;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOpens() {
        return opens;
    }

    public void setOpens(Integer opens) {
        this.opens = opens;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public Integer getReplies() {
        return replies;
    }

    public void setReplies(Integer replies) {
        this.replies = replies;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public ProspectMetadata getProspectMetadata() {
        return prospectMetadata;
    }

    public void setProspectMetadata(ProspectMetadata prospectMetadata) {
        this.prospectMetadata = prospectMetadata;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    @Override
    public String toString() {
        return "Engagement{" +
                "id='" + id + '\'' +
                ", opens=" + opens +
                ", clicks=" + clicks +
                ", replies=" + replies +
                ", stepNumber=" + stepNumber +
                ", prospectMetadata=" + prospectMetadata +
                '}';
    }
}
