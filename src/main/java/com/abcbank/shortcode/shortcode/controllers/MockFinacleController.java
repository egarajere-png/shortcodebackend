package com.abcbank.shortcode.shortcode.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.entities.DTOAccount;

@RestController
@RequestMapping("/mock/finacle, /api/finacle")
public class MockFinacleController {

    @GetMapping("/account-data/{accountNumber}")
    public DTOAccount getAccount(@PathVariable String accountNumber) {

        DTOAccount account = new DTOAccount();

        account.setAccountNumber(accountNumber);
        account.setAccountName("Egara Jere");
        account.setCustId("CUST001");
        account.setIdNumber("12345678");
        account.setPhoneNumber("0712345678");
        account.setEmailAddress("egara.jere@test.com");
        account.setAccountStatus("ACTIVE");

        return account;
    }
}