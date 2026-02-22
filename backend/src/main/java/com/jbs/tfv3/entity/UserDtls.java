package com.jbs.tfv3.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name="users")
public class UserDtls {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
	@Column(nullable = false, unique = true)
	private String email;
	
	@NotBlank(message = "Password cannot be empty")
	@Column(nullable = false)
	private String password;
	
	@NotBlank(message = "ROLE cannot be empty")
	@Pattern(regexp = "ROLE_ADMIN|ROLE_USER", message = "Role must be either ROLE_ADMIN or ROLE_USER")
	@Column(nullable = false)
	private String role;
	
	@OneToMany(mappedBy = "userDtls", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference  // parent side
    private List<Otp> otps = new ArrayList<>();
	
	@OneToMany(mappedBy = "userDtls", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference // parent side for JSON serialization
	private List<Ticket> tickets = new ArrayList<>();

}
