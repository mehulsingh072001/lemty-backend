package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

import javax.persistence.*;

@Entity
@Table(name = "PROSPECT_LIST", schema = "public")
public class ProspectList {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)

    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @OneToMany(fetch = FetchType.LAZY, targetEntity = Campaign.class)
    private List<Campaign> campaign = new ArrayList<>();

    @OneToMany(mappedBy = "prospectList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Prospect> prospects = new ArrayList<>();

    public ProspectList(){}

    public ProspectList(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setListName(String name){
        this.name = name;
    }

    public List<Prospect> getProspects() {
        return prospects;
    }
    public void setProspects(List<Prospect> prospects) {
        this.prospects = prospects;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public List<Campaign> getCampaign() {
        return campaign;
    }

    public void setCampaign(List<Campaign> campaign) {
        this.campaign = campaign;
    }

    @Override
    public String toString() {
        return "ProspectList{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", prospects=" + prospects +
                '}';
    }
}
