package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class ProspectMetadata {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String campaignId;
    // private String prospectId;
    private String status;
    private Integer lastCompletedStep;
    private String threadId;
    private Integer opens = 0;
    private Integer clicks = 0;
    private Integer replies = 0;
    private Boolean contacted = false;
    private Boolean bounced = false;
    private Boolean replied = false;
    private Boolean unsubscribed = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "prospect_id")
    private Prospect prospect;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCampaignId() {
        return campaignId;
    }

    // public String getProspectId() {
    //     return prospectId;
    // }

    // public void setProspectId(String prospectId) {
    //     this.prospectId = prospectId;
    // }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Prospect getProspect() {
        return prospect;
    }

    public void setProspect(Prospect prospect) {
        this.prospect = prospect;
    }

    public Integer getLastCompletedStep() {
        return lastCompletedStep;
    }

    public void setLastCompletedStep(Integer lastCompletedStep) {
        this.lastCompletedStep = lastCompletedStep;
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

    public Boolean getContacted() {
        return contacted;
    }

    public void setContacted(Boolean contacted) {
        this.contacted = contacted;
    }

    public Boolean getBounced() {
        return bounced;
    }

    public void setBounced(Boolean bounced) {
        this.bounced = bounced;
    }

    public Boolean getReplied() {
        return replied;
    }

    public void setReplied(Boolean replied) {
        this.replied = replied;
    }

    public Boolean getUnsubscribed() {
        return unsubscribed;
    }

    public void setUnsubscribed(Boolean unsubscribed) {
        this.unsubscribed = unsubscribed;
    }

    @Override
    public String toString() {
        return "CampaignMetadata{" +
                "id='" + id + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", status='" + status + '\'' +
                ", threadId='" + threadId + '\'' +
                '}';
    }
}
