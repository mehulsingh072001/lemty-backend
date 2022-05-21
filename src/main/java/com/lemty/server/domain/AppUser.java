package com.lemty.server.domain;

import javax.persistence.*;
import java.util.*;

@Entity
public class AppUser {
    @Id
	@Column(name = "id", length = 8, unique = true, nullable = false)
	private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    // @ManyToMany(fetch = EAGER)
    private String role;
    private String company_name;
    private Long phone_number;
    private String work_email;
    private Integer interval;

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Campaign> campaigns = new ArrayList<>();

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private Set<Prospect> prospects = new HashSet<>();

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProspectList> prospectList = new ArrayList<>();

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<GmailCreds> gmailCreds = new ArrayList<>();

    @OneToOne(targetEntity = EmailSignature.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EmailSignature emailSignature;

    @OneToOne(targetEntity = Unsubscribe.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Unsubscribe unsubscribe;

    @OneToOne(targetEntity = DeliveribilitySettings.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DeliveribilitySettings deliveribilitySettings;

    public AppUser (){}

    public AppUser(String id, String name, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRoles(String role) {
        this.role = role;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public Long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(Long phone_number) {
        this.phone_number = phone_number;
    }

    public String getWork_email() {
        return work_email;
    }

    public void setWork_email(String work_email) {
        this.work_email = work_email;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public List<GmailCreds> getAppUserGmails() {
        return gmailCreds;
    }

    public void setAppUserGmails(List<GmailCreds> gmailCreds) {
        this.gmailCreds = gmailCreds;
    }

    public Set<Prospect> getProspects() {
        return prospects;
    }

    public void setProspects(Set<Prospect> prospects) {
        this.prospects = prospects;
    }

    public List<ProspectList> getProspectList() {
        return prospectList;
    }

    public void setProspectList(List<ProspectList> prospectList) {
        this.prospectList = prospectList;
    }

    public EmailSignature getEmailSignature() {
        return emailSignature;
    }

    public void setEmailSignature(EmailSignature emailSignature) {
        this.emailSignature = emailSignature;
    }

    public Unsubscribe getUnsubscribe() {
        return unsubscribe;
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

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public List<GmailCreds> getGmailCreds() {
        return gmailCreds;
    }

    public void setGmailCreds(List<GmailCreds> gmailCreds) {
        this.gmailCreds = gmailCreds;
    }

    public void setUnsubscribe(Unsubscribe unsubscribe) {
        this.unsubscribe = unsubscribe;
    }

    public DeliveribilitySettings getDeliveribilitySettings(){
        return deliveribilitySettings;
    }

    public void setDeliveribilitySettings(DeliveribilitySettings deliveribilitySettings) {
        this.deliveribilitySettings = deliveribilitySettings;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", company_name='" + company_name + '\'' +
                ", phone_number=" + phone_number +
                ", work_email='" + work_email + '\'' +
                ", campaigns=" + campaigns +
                ", gmailCreds=" + gmailCreds +
                '}';
    }
}
