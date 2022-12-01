package com.abcbank.shortcode.shortcode.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOAuthPayloadResponse {
    public String access_token;
    public int expires_in;
    public int refresh_expires_in;
    public String refresh_token;
    public String token_type;
    @JsonProperty("not-before-policy")
    public int notBeforePolicy;
    public String session_state;
    public String scope;
}