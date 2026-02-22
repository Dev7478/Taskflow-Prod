package com.jbs.tfv3.service;

import com.jbs.tfv3.dto.GenerateOtpResponse;

public interface OtpService {
	GenerateOtpResponse generateOtpForEmail(String email);
	boolean validateOtp(String email, String otp);
}
