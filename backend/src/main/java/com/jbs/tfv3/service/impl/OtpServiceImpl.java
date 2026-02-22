package com.jbs.tfv3.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jbs.tfv3.dto.GenerateOtpResponse;
import com.jbs.tfv3.entity.Otp;
import com.jbs.tfv3.entity.UserDtls;
import com.jbs.tfv3.exception.ResourceNotFoundException;
import com.jbs.tfv3.exception.TooManyRequestsException;
import com.jbs.tfv3.repository.OtpRepository;
import com.jbs.tfv3.repository.UserDtlsRepository;
import com.jbs.tfv3.service.OtpService;

import jakarta.transaction.Transactional;

@Service
public class OtpServiceImpl implements OtpService {

	@Autowired
	private OtpRepository otpRepository;
	@Autowired
	private UserDtlsRepository userDtlsRepository;

	private final SecureRandom secureRandom = new SecureRandom();

	private static final long OTP_TTL_SECONDS = 5 * 60; // 5 minutes
	private static final long RESEND_INTERVAL_SECONDS = 60; // 1 minute minimum between requests

//    public OtpServiceImpl(OtpRepository otpRepository, UserDtlsRepository userDtlsRepository) {
//        this.otpRepository = otpRepository;
//        this.userDtlsRepository = userDtlsRepository;
//    }

	@Override
	@Transactional
	public GenerateOtpResponse generateOtpForEmail(String email) {
		// 1) Find user by email
		UserDtls userDtls = userDtlsRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("No registered user with that email"));

		// 2) rate limit check — if an active OTP was created very recently, deny
		Optional<Otp> active = otpRepository.findTopByUserDtlsAndIsActiveTrueOrderByCreatedAtDesc(userDtls);
		if (active.isPresent()) {
			LocalDateTime created = active.get().getCreatedAt();
			if (created.isAfter(LocalDateTime.now().minusSeconds(RESEND_INTERVAL_SECONDS))) {
				throw new TooManyRequestsException("OTP requested too frequently. Please wait a while.");
			}
		}

		// 3) deactivate any previous active OTPs for the user
		otpRepository.deactivateActiveOtpsForUserDtls(userDtls.getId());

		// 4) generate secure 6-digit OTP (100000..999999)
		int code = secureRandom.nextInt(900_000) + 100_000;
		String otp = String.valueOf(code);

		// 5) persist OTP (DEMO: plaintext stored; production: store hash instead)
		Otp otpEntity = new Otp(userDtls, otp);
		otpRepository.save(otpEntity);

		// 6) (Optional) send OTP by email/SMS — recommended in production (not shown
		// here)
		// sendOtpToEmail(userDtls.getEmail(), otp);

		// 7) return response (DEMO: includes OTP)
		return new GenerateOtpResponse(userDtls.getEmail(), otp, OTP_TTL_SECONDS,
				"OTP generated successfully. (In production you should not return the OTP in response.)");
	}

	@Override
	@Transactional
	public boolean validateOtp(String email, String otp) {
		Optional<Otp> otpEntity = otpRepository.findValidOtp(email, otp);

		if (otpEntity.isEmpty()) {
			return false; // not found or inactive
		}

		Otp o = otpEntity.get();

		// check expiry (example: 10 minutes)
		LocalDateTime expiryTime = o.getCreatedAt().plusMinutes(10);
		if (LocalDateTime.now().isAfter(expiryTime)) {
			return false; // expired
		}

		// mark OTP as used
		o.setIsActive(false);
		otpRepository.save(o);

		return true;
	}
}
