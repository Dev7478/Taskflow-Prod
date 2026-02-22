package com.jbs.tfv3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserAddRequest {
	@NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
	private String email;
	
	@NotBlank(message = "Password cannot be empty")
	private String password;
	
	@NotBlank(message = "ROLE cannot be empty")
	@Pattern(regexp = "ROLE_ADMIN|ROLE_USER", message = "Role must be either ROLE_ADMIN or ROLE_USER")
	private String role;
}
