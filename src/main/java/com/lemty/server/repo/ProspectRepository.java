package com.lemty.server.repo;

import com.lemty.server.domain.Prospect;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProspectRepository extends JpaRepository<Prospect, String>{
    Page<Prospect> findByAppUserId(String userId, Pageable pageable);
    Page<Prospect> findByProspectListId(String listId, Pageable pageable);
    Page<Prospect> findByCampaignsId(String campaignId, Pageable pageable);

    Page<Prospect> findByAppUserIdAndStatusIs(String userId, String status, Pageable pageable);
    Page<Prospect> findByProspectListIdAndStatus(String listId, String status, Pageable pageable);

    @Query(value = "SELECT p FROM Prospect p WHERE LOWER(p.firstName) like :keyword% or LOWER(p.lastName) like :keyword% or LOWER(p.prospectEmail) like :keyword% or LOWER(p.prospectCompany) like :keyword%")
    List<Prospect> findByKeyword(@Param("keyword") String keyword);
    Boolean existsByProspectEmail(String email);
}