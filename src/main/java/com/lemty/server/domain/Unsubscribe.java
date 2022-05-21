package com.lemty.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "unsubscribe")
public class Unsubscribe {
    @Id
	@Column(name = "id", length = 8, unique = true, nullable = false)
	private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    private String body = "";

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public String toString() {
        return "Unsubscribe{" +
                "id='" + id + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
