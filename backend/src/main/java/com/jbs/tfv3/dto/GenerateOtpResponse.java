package com.jbs.tfv3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GenerateOtpResponse {
	private String email;
    private String otp;            // demo only
    private long expiresInSeconds; // TTL
    private String message;
}
