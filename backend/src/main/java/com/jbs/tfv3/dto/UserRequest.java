package com.jbs.tfv3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
	@NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
	private String email;
	
	@NotBlank(message = "Password cannot be empty")
	private String password;
	
	@NotBlank(message = "OTP cannot be empty")
	private String otp;
}
