package com.lemty.server.repo;

import java.util.List;

import com.lemty.server.domain.GmailCreds;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GmailCredsRepo extends JpaRepository<GmailCreds, String> {
    GmailCreds findByEmail(String email);
    List<GmailCreds> findByAppUserId(String userId);
    void deleteAllByAppUserId(String userId);
}
