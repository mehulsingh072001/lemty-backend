package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "campaigns", schema = "public")
public class Campaign{
    @Id
    @Column(name = "id", length = 8, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    @Column(name = "campaign_name")
    private String name;
    private String timezone;
    private Boolean	campaignStop;
    private Integer totalOpens = 0;
    private Integer totalClicks = 0;
    private Integer totalReplies = 0;
    private Integer prospectCount;
    private Integer dailyLimit;
    private String completed;
    private String status;
    private Map[] steps;

    @Column(updatable = false)
    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "prospect_list_id")
    private ProspectList prospectLists;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "campaign_prospects", joinColumns = @JoinColumn(name = "campaign_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "prospect_id", referencedColumnName = "id")
    )
    private List<Prospect> prospect = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCampaign_name() {
        return name;
    }

    public void setCampaign_name(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getCampaignStop() {
        return campaignStop;
    }

    public void setCampaignStop(Boolean campaignStop) {
        this.campaignStop = campaignStop;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getProspectCount() {
        return prospectCount;
    }

    public void setProspectCount(Integer prospectCount) {
        this.prospectCount = prospectCount;
    }

    public List<Prospect> getProspect() {
        return prospect;
    }

    public void setProspect(List<Prospect> prospect) {
        this.prospect = prospect;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public ProspectList getProspectLists() {
        return prospectLists;
    }

    public void setProspectLists(ProspectList prospectLists) {
        this.prospectLists = prospectLists;
    }

    public Map[] getSteps() {
        return steps;
    }

    public void setSteps(Map[] steps) {
        this.steps = steps;
    }

    public Integer getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Integer dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalOpens(){
        return totalOpens;
    }

    public void setTotalOpens(Integer totalOpens){
        this.totalOpens = totalOpens;
    }


    public Integer getTotalClicks(){
        return totalOpens;
    }

    public void setTotalClicks(Integer totalClicks){
        this.totalClicks = totalClicks;
    }

    public Integer getTotalReplies(){
        return totalOpens;
    }

    public void setTotalReplies(Integer totalReplies){
        this.totalReplies = totalReplies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
     
    @Override
    public String toString() {
        return "Campaign{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", timezone='" + timezone + '\'' +
                ", prospectCount=" + prospectCount +
                ", prospectLists=" + prospectLists +
                ", prospect=" + prospect +
                ", steps=" + steps +
                ", appUser=" + appUser +
                '}';
    }
}

