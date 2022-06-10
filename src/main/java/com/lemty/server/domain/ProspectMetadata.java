package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class ProspectMetadata implements Serializable {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String campaignId;
    // private String prospectId;
    private Integer lastCompletedStep;
    private String threadId;
    private String msgId;
    private Integer opens = 0;
    private Integer clicks = 0;
    private Integer replies = 0;
    private String status = "In Campaign";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "prospect_id")
    private Prospect prospect;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Engagement> engagements = new ArrayList<>();

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

    public List<Engagement> getEngagements() {
        return engagements;
    }

    public void setEngagements(List<Engagement> engagements) {
        this.engagements = engagements;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
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
