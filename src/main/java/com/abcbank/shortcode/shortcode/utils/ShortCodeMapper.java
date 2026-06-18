package com.abcbank.shortcode.shortcode.utils;

import org.springframework.stereotype.Component;

import com.abcbank.shortcode.shortcode.dto.ShortCodeDto;
import com.abcbank.shortcode.shortcode.entities.ShortCode;

@Component
public class ShortCodeMapper {

    public ShortCodeDto toDto(ShortCode sc) {

        ShortCodeDto dto = new ShortCodeDto();

        dto.setId(sc.getId());
        dto.setInitiator(sc.getInitiator());
        dto.setApprover(sc.getApprover());
        dto.setAccountNumber(sc.getAccountNumber());
        dto.setAccountName(sc.getAccountName());
        dto.setPhoneNumber(sc.getPhoneNumber());
        dto.setEmailAddress(sc.getEmailAddress());
        dto.setIdNumber(sc.getIdNumber());
        dto.setCustId(sc.getCustId());
        dto.setRemark(sc.getRemark());
        dto.setDeleteRemark(sc.getDeleteRemark());
        dto.setShortCode(sc.getShortCode());
        dto.setSequenceNumber(sc.getSequenceNumber());

        dto.setDateInitiated(
                sc.getDateInitiated() != null
                        ? sc.getDateInitiated().toString()
                        : null);

        dto.setDateApproved(
                sc.getDateApproved() != null
                        ? sc.getDateApproved().toString()
                        : null);

        dto.setApproved(sc.isApproved());
        dto.setDeleteInitiated(sc.isDeleteInitiated());
        dto.setDeleted(sc.isDeleted());

        return dto;
    }
}