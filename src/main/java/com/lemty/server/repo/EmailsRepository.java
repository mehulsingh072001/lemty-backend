package com.lemty.server.repo;

import com.lemty.server.domain.Emails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailsRepository extends JpaRepository<Emails, String>{
}
