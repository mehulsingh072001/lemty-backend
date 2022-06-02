package com.lemty.server.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Emails {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
    private Integer step;
    private String threadId;
    private Date startTime;
    private String status = "TODAY";

    @JsonIgnore
    @ManyToOne(targetEntity = Campaign.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospect_id")
    private Prospect prospect;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Engagement> engagements = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public List<Engagement> getEngagements() {
        return engagements;
    }

    public void setEngagements(List<Engagement> engagements) {
        this.engagements = engagements;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Prospect getProspect() {
        return prospect;
    }

    public void setProspect(Prospect prospect) {
        this.prospect = prospect;
    }

    @Override
    public String toString() {
        return "Emails{" +
                "id='" + id + '\'' +
                ", from='" + fromEmail + '\'' +
                ", to='" + toEmail + '\'' +
                ", body='" + body + '\'' +
                ", step=" + step +
                ", campaign=" + campaign +
                ", appUser=" + appUser +
                '}';
    }
}
