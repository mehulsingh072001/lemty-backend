package com.lemty.server.repo;

import com.lemty.server.domain.EmailSignature;
import com.lemty.server.domain.Unsubscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnsubscribeRepository extends JpaRepository<Unsubscribe, String> {
    Unsubscribe findByAppUserId(String userId);
}
