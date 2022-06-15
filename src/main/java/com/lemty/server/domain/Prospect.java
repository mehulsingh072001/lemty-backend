package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Table(name = "PROSPECT", schema = "public")
public class Prospect{
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    @Column(nullable = true)
    private String firstName;
    @Column(nullable = true)
    private String lastName;
    @Column(nullable = true)
    private String prospectEmail;
    @Column(nullable = true)
    private String prospectCompany;
    @Column(nullable = true)
    private String prospectMobileNumber;
    @Column(nullable = true)
    private String prospectAccount;
    @Column(nullable = true)
    private String prospectCompanyEmail;
    @Column(nullable = true)
    private String prospectDepartment;
    @Column(nullable = true)
    private String prospectTitle;
    @Column(nullable = true)
    private String prospectCompanyPhone;
    @Column(nullable = true)
    private String prospectCompanyDomain;
    @Column(nullable = true)
    private String prospectLinkedinUrl;
    @Column(nullable = true)
    private String prospectTwitterUrl;
    @Column(nullable = true)
    private String prospectLocation;
    @Column(nullable = true)
    private String prospectCity;
    @Column(nullable = true)
    private String prospectCountry;

    @Column(nullable = true)
    private Boolean stopped = false;
    @Column(nullable = true)
    private Boolean replied = false;
    @Column(nullable = true)
    private Boolean unsubscribed = false;

    private String status;
    private Integer points = 0;
    private Boolean hot;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospect_list_id")
    private ProspectList prospectList;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "campaign_prospects", joinColumns = @JoinColumn(name = "prospect_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "campaign_id", referencedColumnName = "id")
    )
    private List<Campaign> campaigns = new ArrayList<>();

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL)
    private List<ProspectMetadata> prospectMetadatas = new ArrayList<>();

    @OneToMany(targetEntity = Emails.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Emails> emails = new ArrayList<>();

    public Prospect(String firstName,
                    String lastName,
                    String prospectEmail,
                    String prospectCompany,
                    String prospectMobileNumber,
                    String prospectAccount,
                    String prospectCompanyEmail,
                    String prospectDepartment,
                    String prospectTitle,
                    String prospectCompanyDomain,
                    String prospectLinkedinUrl,
                    String prospectTwitterUrl,
                    String prospectLocation,
                    String prospectCountry
                    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.prospectEmail = prospectEmail;
        this.prospectCompany = prospectCompany;
        this.prospectMobileNumber = prospectMobileNumber;
        this.prospectAccount = prospectAccount;
        this.prospectCompanyEmail = prospectCompanyEmail;
        this.prospectDepartment = prospectDepartment;
        this.prospectTitle = prospectTitle;
        this.prospectCompanyDomain = prospectCompanyDomain;
        this.prospectLinkedinUrl = prospectLinkedinUrl;
        this.prospectTwitterUrl = prospectTwitterUrl;
        this.prospectLocation = prospectLocation;
        this.prospectCountry = prospectCountry;
    }

    public Prospect() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProspectEmail() {
        return prospectEmail;
    }

    public void setProspectEmail(String prospectEmail) {
        this.prospectEmail = prospectEmail;
    }

    public String getProspectCompany() {
        return prospectCompany;
    }

    public void setProspectCompany(String prospectCompany) {
        this.prospectCompany = prospectCompany;
    }

    public String getProspectMobileNumber() {
        return prospectMobileNumber;
    }

    public void setProspectMobileNumber(String prospectMobileNumber) {
        this.prospectMobileNumber = prospectMobileNumber;
    }

    public String getProspectAccount() {
        return prospectAccount;
    }

    public void setProspectAccount(String prospectAccount) {
        this.prospectAccount = prospectAccount;
    }

    public String getProspectCompanyEmail() {
        return prospectCompanyEmail;
    }

    public void setProspectCompanyEmail(String prospectCompanyEmail) {
        this.prospectCompanyEmail = prospectCompanyEmail;
    }

    public String getProspectDepartment() {
        return prospectDepartment;
    }

    public void setProspectDepartment(String prospectDepartment) {
        this.prospectDepartment = prospectDepartment;
    }

    public String getProspectTitle() {
        return prospectTitle;
    }

    public void setProspectTitle(String prospectTitle) {
        this.prospectTitle = prospectTitle;
    }

    public String getProspectCompanyDomain() {
        return prospectCompanyDomain;
    }

    public void setProspectCompanyDomain(String prospectCompanyDomain) {
        this.prospectCompanyDomain = prospectCompanyDomain;
    }

    public String getProspectLinkedinUrl() {
        return prospectLinkedinUrl;
    }

    public void setProspectLinkedinUrl(String prospectLinkedinUrl) {
        this.prospectLinkedinUrl = prospectLinkedinUrl;
    }

    public String getProspectTwitterUrl() {
        return prospectTwitterUrl;
    }

    public void setProspectTwitterUrl(String prospectTwitterUrl) {
        this.prospectTwitterUrl = prospectTwitterUrl;
    }

    public String getProspectLocation() {
        return prospectLocation;
    }

    public void setProspectLocation(String prospectLocation) {
        this.prospectLocation = prospectLocation;
    }

    public String getProspectCountry() {
        return prospectCountry;
    }

    public void setProspectCountry(String prospectCountry) {
        this.prospectCountry = prospectCountry;
    }

    public ProspectList getProspectList() {
        return prospectList;
    }

    public void setProspectList(ProspectList prospectList) {
        this.prospectList = prospectList;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public void setStopped(Boolean stopped) {
        this.stopped = stopped;
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

    public String getProspectCompanyPhone() {
        return prospectCompanyPhone;
    }

    public void setProspectCompanyPhone(String prospectCompanyPhone) {
        this.prospectCompanyPhone = prospectCompanyPhone;
    }

    public String getProspectCity() {
        return prospectCity;
    }

    public void setProspectCity(String prospectCity) {
        this.prospectCity = prospectCity;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public List<ProspectMetadata> getProspectMetadatas() {
        return prospectMetadatas;
    }

    public void setProspectMetadatas(List<ProspectMetadata> prospectMetadatas) {
        this.prospectMetadatas = prospectMetadatas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Emails> getEmails() {
        return emails;
    }

    public void setEmails(List<Emails> emails) {
        this.emails = emails;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Boolean getHot() {
        return hot;
    }

    public void setHot(Boolean hot) {
        this.hot = hot;
    }

    @Override
    public String toString() {
        return "Prospect{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", prospectEmail='" + prospectEmail + '\'' +
                ", prospectCompany='" + prospectCompany + '\'' +
                ", prospectMobileNumber='" + prospectMobileNumber + '\'' +
                ", prospectAccount='" + prospectAccount + '\'' +
                ", prospectCompanyEmail='" + prospectCompanyEmail + '\'' +
                ", prospectDepartment='" + prospectDepartment + '\'' +
                ", prospectTitle='" + prospectTitle + '\'' +
                ", prospectCompanyDomain='" + prospectCompanyDomain + '\'' +
                ", prospectLinkedinUrl='" + prospectLinkedinUrl + '\'' +
                ", prospectTwitterUrl='" + prospectTwitterUrl + '\'' +
                ", prospectLocation='" + prospectLocation + '\'' +
                ", prospectCountry='" + prospectCountry + '\'' +
                ", appUser=" + appUser +
                ", prospectList=" + prospectList +
                '}';
    }
}
