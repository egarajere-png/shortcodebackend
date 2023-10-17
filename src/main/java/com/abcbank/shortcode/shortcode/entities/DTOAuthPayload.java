package com.abcbank.shortcode.shortcode.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOAuthPayload {
    String username;
    String password;
}
