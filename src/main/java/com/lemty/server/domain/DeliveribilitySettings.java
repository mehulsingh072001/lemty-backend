package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "settings_deliveribility")
public class DeliveribilitySettings {
    @Id
    @Column(name = "id", length = 8, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String emailInterval;
    private String customTrackingDomain;
    private Integer seconds = 0;
    private Integer minInterval = 0;
    private Integer maxInterval = 0;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailInterval() {
        return emailInterval;
    }

    public void setEmailInterval(String emailInterval) {
        this.emailInterval = emailInterval;
    }

    public String getCustomTrackingDomain() {
        return customTrackingDomain;
    }

    public void setCustomTrackingDomain(String customTrackingDomain) {
        this.customTrackingDomain = customTrackingDomain;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    public Integer getMinInterval() {
        return minInterval;
    }

    public void setMinInterval(Integer minInterval) {
        this.minInterval = minInterval;
    }

    public Integer getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(Integer maxInterval) {
        this.maxInterval = maxInterval;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public String toString() {
        return "DeliveribilitySettings{" +
                "emailInterval='" + emailInterval + '\'' +
                ", customTrackingDomain='" + customTrackingDomain + '\'' +
                ", seconds=" + seconds +
                ", minInterval=" + minInterval +
                ", maxInterval=" + maxInterval +
                '}';
    }
}
