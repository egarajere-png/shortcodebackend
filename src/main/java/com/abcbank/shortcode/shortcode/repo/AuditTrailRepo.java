package com.abcbank.shortcode.shortcode.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;

public interface AuditTrailRepo extends JpaRepository<AuditTrail, Long> {

    List<AuditTrail> findByShortCodeIdOrderByActionDateDesc(Integer shortCodeId);

    List<AuditTrail> findByAccountNumberOrderByActionDateDesc(String accountNumber);
}