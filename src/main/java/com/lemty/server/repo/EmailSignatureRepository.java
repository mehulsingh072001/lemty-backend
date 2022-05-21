package com.lemty.server.repo;

import java.util.List;

import com.lemty.server.domain.EmailSignature;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSignatureRepository extends JpaRepository<EmailSignature, String>{
    EmailSignature findBySignature(String signature);
    List<EmailSignature> findByAppUserId(String userId);
}
