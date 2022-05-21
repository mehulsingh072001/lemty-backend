package com.lemty.server.domain;

        import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.OnDelete;
        import org.hibernate.annotations.OnDeleteAction;

        import javax.persistence.*;
        import java.util.*;

@Entity
@Table(name = "steps")
public class Step{
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    private Integer stepNumber;
    private Integer day;
    private Integer dayGap;
    private Integer hour;
    private Integer hourGap;
    private Integer minute;
    private Integer minuteGap;
    private String deliveryWindow;
    private String whichEmail;
    private Boolean enabled;
    private String[] days;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, targetEntity = Mail.class, mappedBy = "step")
    @NotFound(action = NotFoundAction.IGNORE)
    private List<Mail> mails = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(targetEntity = Campaign.class,fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;


    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public String[] getDays() {
        return days;
    }

    public void setDays(String[] days) {
        this.days = days;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getDayGap() {
        return dayGap;
    }

    public void setDayGap(Integer dayGap) {
        this.dayGap = dayGap;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getHourGap() {
        return hourGap;
    }

    public void setHourGap(Integer hourGap) {
        this.hourGap = hourGap;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public Integer getMinuteGap() {
        return minuteGap;
    }

    public void setMinuteGap(Integer minuteGap) {
        this.minuteGap = minuteGap;
    }

    public String getDeliveryWindow() {
        return deliveryWindow;
    }

    public void setDeliveryWindow(String deliveryWindow) {
        this.deliveryWindow = deliveryWindow;
    }

    public String getWhichEmail() {
        return whichEmail;
    }

    public void setWhichEmail(String whichEmail) {
        this.whichEmail = whichEmail;
    }

    public List<Mail> getMails() {
        return mails;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Step{" +
                "id='" + id + '\'' +
                ", stepNumber=" + stepNumber +
                ", day=" + day +
                ", dayGap=" + dayGap +
                ", hour=" + hour +
                ", hourGap=" + hourGap +
                ", minute=" + minute +
                ", minuteGap=" + minuteGap +
                ", deliveryWindow='" + deliveryWindow + '\'' +
                ", whichEmail='" + whichEmail + '\'' +
                ", enabled=" + enabled +
                ", days=" + Arrays.toString(days) +
                ", mails=" + mails +
                ", campaign=" + campaign +
                '}';
    }
}
