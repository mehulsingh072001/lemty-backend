package com.lemty.server.repo;

import java.util.List;
import java.util.Optional;

import com.lemty.server.domain.ProspectList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProspectListRepository extends JpaRepository<ProspectList, String>{
    Optional <ProspectList> findProspectListByName(String name);
    List<ProspectList> findByAppUserId(String userId);
    ProspectList findByCampaignId(String campaignId);
}
