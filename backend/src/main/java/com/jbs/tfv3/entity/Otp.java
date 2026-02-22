package com.jbs.tfv3.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "otps")
public class Otp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonBackReference  // child side
	private UserDtls userDtls;

	@Column(nullable = false, length = 6)
	private String otp;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	public Otp() {
	}

	public Otp(UserDtls userDtls, String otp) {
		this.userDtls = userDtls;
		this.otp = otp;
		this.createdAt = LocalDateTime.now();
		this.isActive = true;
	}
	
	@PrePersist
    protected void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
	
	public Long getId() { return id; }
    public UserDtls getUserDtls() { return userDtls; }
    public String getOtp() { return otp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }
    public void setIsActive(boolean active) { this.isActive = active; }
}
