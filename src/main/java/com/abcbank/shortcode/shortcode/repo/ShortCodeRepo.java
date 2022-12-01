package com.abcbank.shortcode.shortcode.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.abcbank.shortcode.shortcode.entities.ShortCode;

public interface ShortCodeRepo extends CrudRepository<ShortCode, Integer> {
	List<ShortCode> findAll();
	ShortCode findById(int id);
	List<ShortCode> findByApproved(boolean isApproved);
	List<ShortCode> findByAccountNumberOrderByIdDesc(String accountNumber);
	List<ShortCode> findByAccountNumberAndApproved(String accountNumber, boolean approved);
	ShortCode findByShortCode(int shortCode);
}