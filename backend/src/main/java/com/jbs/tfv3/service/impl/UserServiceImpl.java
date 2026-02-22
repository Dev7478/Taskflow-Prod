package com.jbs.tfv3.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jbs.tfv3.dto.LoginRequest;
import com.jbs.tfv3.dto.UserAddRequest;
import com.jbs.tfv3.dto.UserRequest;
import com.jbs.tfv3.entity.UserDtls;
import com.jbs.tfv3.repository.OtpRepository;
import com.jbs.tfv3.repository.UserDtlsRepository;
import com.jbs.tfv3.service.JwtService;
import com.jbs.tfv3.service.UserDtlsService;
import com.jbs.tfv3.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserDtlsRepository userDtlsRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserDtlsService userDtlsService;

	@Autowired
	private OtpServiceImpl otpServiceImpl;
	
	@Autowired
	private OtpRepository otpRepository;

	@Override
	public String login(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
		if (authentication.isAuthenticated()) {
			return jwtService.generateToken(loginRequest.getEmail(),
					userDtlsService.getRoleFromEmail(loginRequest.getEmail()));
		}
		return null;
	}

	@Override
	public List<UserDtls> getUserDtls() {
		return userDtlsRepository.findAll();
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		Optional<UserDtls> optionalUser = userDtlsRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			return null;
		}
		return optionalUser.get();
	};

	@Override
	public UserDtls registerUser(UserAddRequest request) {
		if (userDtlsRepository.findByEmail(request.getEmail()).isPresent()) {
			return null;
		}

		UserDtls user = new UserDtls();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
		user.setRole(request.getRole());

		UserDtls result = userDtlsRepository.save(user);
		return result;
	}

	@Override
	@Transactional
	public String updatePassword(UserRequest request) {
		// validate OTP first
		boolean isValid = otpServiceImpl.validateOtp(request.getEmail(), request.getOtp());
		if (!isValid) {
			return "Invalid or expired OTP";
		}

		// update password if OTP valid
		Optional<UserDtls> optionalUser = userDtlsRepository.findByEmail(request.getEmail());

		if (optionalUser.isEmpty()) {
			return "User not found";
		}

		UserDtls user = optionalUser.get();
		user.setPassword(passwordEncoder.encode(request.getPassword())); // Getting the new password
		userDtlsRepository.save(user);

		return "Password updated successfully";
	}

	@Override
	@Transactional
	public UserDtls deleteUserByEmail(String email) {
		UserDtls userDtls = userDtlsRepository.findByEmail(email)
	            .orElse(null);

	    if (userDtls == null) {
	        return null;
	    }

	    // Step 1: Delete all OTPs linked to this user
	    otpRepository.deleteByUserDtls(userDtls);

	    // Step 2: Delete the user
	    userDtlsRepository.delete(userDtls);

	    return userDtls;
	}

}
