package com.lemty.server.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class IntentDetection {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private Integer openPoints = 2;
    private Integer clickPoints = 5;
    private Integer hotPoints = 6;
    @OneToOne
    private AppUser appUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getOpenPoints() {
        return openPoints;
    }

    public void setOpenPoints(Integer openPoints) {
        this.openPoints = openPoints;
    }

    public Integer getClickPoints() {
        return clickPoints;
    }

    public void setClickPoints(Integer clickPoints) {
        this.clickPoints = clickPoints;
    }

    public Integer getHotPoints() {
        return hotPoints;
    }

    public void setHotPoints(Integer hotPoints) {
        this.hotPoints = hotPoints;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public String toString() {
        return "IntentDetection{" +
                "id='" + id + '\'' +
                ", openPoints=" + openPoints +
                ", clickPoints=" + clickPoints +
                ", hotPoints=" + hotPoints +
                '}';
    }
}
