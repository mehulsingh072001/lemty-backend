package com.lemty.server.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Emails {
    @Id
    @Column(name = "id", length = 8, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String from;
    private String to;
    private String body;
    private Integer step;
    private LocalDateTime dateTime;

    @JsonIgnore
    @ManyToOne(targetEntity = Campaign.class,fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;
}
