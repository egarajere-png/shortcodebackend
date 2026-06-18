package com.abcbank.shortcode.shortcode.middleware;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;

@Service
public class AuditTrailService {

    @Autowired
    private AuditTrailRepo auditTrailRepo;

    public void logAction(
            ShortCode shortCode,
            String action,
            String performedBy,
            String remarks) {

        AuditTrail audit = new AuditTrail();

        audit.setShortCodeId(shortCode.getId());
        audit.setAccountNumber(shortCode.getAccountNumber());
        audit.setShortCode(shortCode.getShortCode());
        audit.setAction(action);
        audit.setPerformedBy(performedBy);
        audit.setRemarks(remarks);
        audit.setActionDate(LocalDateTime.now());

        auditTrailRepo.save(audit);
    }
}