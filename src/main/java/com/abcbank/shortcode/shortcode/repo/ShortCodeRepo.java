package com.abcbank.shortcode.shortcode.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.abcbank.shortcode.shortcode.entities.ShortCode;

public interface ShortCodeRepo extends CrudRepository<ShortCode, Integer> {

    List<ShortCode> findAll();

    ShortCode findById(int id);

    List<ShortCode> findByDeleted(boolean isDeleted);

    List<ShortCode> findByApproved(boolean isApproved);

    List<ShortCode> findByAccountNumberOrderByIdDesc(String accountNumber);

    List<ShortCode> findByAccountNumberAndApproved(String accountNumber, boolean approved);

    List<ShortCode> findByAccountNumberAndApprovedAndDeleted(
            String accountNumber,
            boolean approved,
            boolean deleted);

    List<ShortCode> findByDeleteInitiatedAndDeleted(
            boolean initiated,
            boolean deleted);

    ShortCode findByShortCode(int shortCode);

    ShortCode findByAccountNumber(String accountNumber);

    /*
     * ===========================================================
     * Dashboard Analytics
     * ===========================================================
     */

    long count();

    long countByApproved(boolean approved);

    long countByDeleted(boolean deleted);

    long countByApprovedAndDeleted(boolean approved, boolean deleted);

    long countByDeleteInitiated(boolean deleteInitiated);

    long countByDeleteInitiatedAndDeleted(
            boolean deleteInitiated,
            boolean deleted);

    /*
     * ===========================================================
     * Weekly Summary
     * ===========================================================
     */

    @Query("""
        SELECT COUNT(s)
        FROM ShortCode s
        WHERE s.dateInitiated >= :start
          AND s.dateInitiated < :end
    """)
    long countInitiatedBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

}