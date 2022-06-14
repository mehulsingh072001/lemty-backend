package com.lemty.server.repo;

import com.lemty.server.domain.Campaign;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, String>{
    Campaign findByName(String campaign_name);
    List<Campaign> findByAppUserId(String userId, Sort createdAt);
    void deleteAllByAppUserId(String userId);
}
