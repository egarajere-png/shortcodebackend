package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

@Data
public class DTOResponse {
    private String statusCode;
    private int shortCode;
    private String message;
}
