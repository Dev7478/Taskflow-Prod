package com.jbs.tfv3.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jbs.tfv3.Taskflowv3Application;
import com.jbs.tfv3.dto.EmailDetails;
import com.jbs.tfv3.dto.EmailRequest;
import com.jbs.tfv3.dto.GenerateOtpResponse;
import com.jbs.tfv3.dto.JwtResponse;
import com.jbs.tfv3.dto.LoginRequest;
import com.jbs.tfv3.dto.UserAddRequest;
import com.jbs.tfv3.dto.UserRequest;
import com.jbs.tfv3.dto.UserRestrictedResponse;
import com.jbs.tfv3.entity.UserDtls;
import com.jbs.tfv3.service.JwtService;
import com.jbs.tfv3.service.TokenBlacklistService;
import com.jbs.tfv3.service.UserService;
import com.jbs.tfv3.service.impl.EmailServiceImpl;
import com.jbs.tfv3.service.impl.OtpServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(Taskflowv3Application.class);

	@Autowired
	private UserService userService;

	@Autowired
	private TokenBlacklistService tokenBlacklistService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private OtpServiceImpl otpServiceImpl;
	
	@Autowired
	private EmailServiceImpl emailServiceImpl;

	/*
	 * { "status": 200, "message": "Dashboard retrieved successfully", "data":
	 * "Hello, from Task-Flow Dashboard (❁´◡`❁)", "timestamp":
	 * "2025-10-01T15:15:30.123Z" }
	 */
	@Operation(summary = "Get Dashboard", description = "Fetches the dashboard view for authenticated users (Admin/User).")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
			@ApiResponse(responseCode = "404", description = "Dashboard not available") })
	@GetMapping("/dashboard")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<String>> dashboard() {
		logger.info("/dashboard encountered >> dashboard()");

		com.jbs.tfv3.dto.ApiResponse<String> response = new com.jbs.tfv3.dto.ApiResponse<>(200,
				"Dashboard retrieved successfully", "Hello, from Task-Flow Dashboard (❁´◡`❁)");
		return ResponseEntity.ok(response);
	}

	/*
	 * Success: { "status": 200, "message": "Successfully logged in", "data": {
	 * "token": "eyJhbGciOiJIUzI1NiIsInR...", "email": "user@example.com", "role":
	 * "USER" }, "timestamp": "2025-10-01T16:10:55.876Z" } Failure: { "status": 401,
	 * "message": "Invalid login credentials", "data": null, "timestamp":
	 * "2025-10-01T16:11:20.456Z" }
	 */
	@Operation(tags = "Authentication", summary = "Login with credentials", description = "Authenticates a user with email and password and returns a JWT token.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully logged in and token retrieved"),
			@ApiResponse(responseCode = "401", description = "Invalid login credentials"),
			@ApiResponse(responseCode = "404", description = "Login endpoint not available") })
	@PostMapping("/auth/login")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<JwtResponse>> login(
			@Valid @RequestBody LoginRequest loginRequest) {
		logger.info("/login encountered >> login(@RequestBody UserRequest userRequest)");

		String token = userService.login(loginRequest);

		if (token == null) {
			com.jbs.tfv3.dto.ApiResponse<JwtResponse> response = new com.jbs.tfv3.dto.ApiResponse<>(401,
					"Invalid login credentials", null);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		JwtResponse jwtResponse = new JwtResponse(token, jwtService.extractEmail(token), jwtService.extractRole(token));

		com.jbs.tfv3.dto.ApiResponse<JwtResponse> response = new com.jbs.tfv3.dto.ApiResponse<>(200,
				"Successfully logged in", jwtResponse);

		return ResponseEntity.ok(response);
	}

	/*
	 * Success: { "status": 200, "message":
	 * "Successfully retrieved all user details", "data": [ { "id": 1, "email":
	 * "admin@example.com", "role": "ADMIN" }, { "id": 2, "email":
	 * "user1@example.com", "role": "USER" } ], "timestamp":
	 * "2025-10-01T16:35:20.123Z" } Failure: { "status": 404, "message":
	 * "No users found", "data": null, "timestamp": "2025-10-01T16:36:05.789Z" }
	 */
	@Operation(summary = "Retrieve all user details", description = "Fetches a list of all registered users. Accessible only to ADMIN.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully retrieved all user details"),
			@ApiResponse(responseCode = "401", description = "Authentication required - only for logged-in users"),
			@ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
			@ApiResponse(responseCode = "404", description = "No user details available") })
	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/users")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<List<UserDtls>>> getAllUsers() {
		logger.info("/users encountered >> getUserDetails(HttpServletRequest request)");

		List<UserDtls> users = userService.getUserDtls();

		if (users == null || users.isEmpty()) {
			com.jbs.tfv3.dto.ApiResponse<List<UserDtls>> response = new com.jbs.tfv3.dto.ApiResponse<>(404,
					"No users found", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		com.jbs.tfv3.dto.ApiResponse<List<UserDtls>> response = new com.jbs.tfv3.dto.ApiResponse<>(200,
				"Successfully retrieved all user details", users);

		return ResponseEntity.ok(response);
	}

	/*
	Success:
	{
	    "status": 201,
	    "message": "User registered successfully",
	    "data": {
	        "id": 9,
	        "email": "arunava.nandi@yahoo.com",
	        "password": "$2a$10$PIBcDlZaCdilkbxeXUGRl..1sWMVpKkCMgG4ROCkZQfMHkdmgajBK",
	        "role": "ROLE_USER"
	    },
	    "timestamp": "2025-10-02T13:51:10.6041795"
	}
	Failure (If user already exists)
	{
	    "status": 409,
	    "message": "User already exists in the database",
	    "data": null,
	    "timestamp": "2025-10-02T13:48:56.6060108"
	}
	*/
	@Operation(summary = "Register a new user", description = "Registers a new user with email, password and role")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "User successfully registered"),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "409", description = "Email already exists") })
	@PostMapping("/users")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<UserRestrictedResponse>> register(
			@Valid @RequestBody UserAddRequest userAddRequest) {
		logger.info("/register encountered >> register(@RequestBody UserAddRequest userAddRequest)");
		UserDtls userDtls = userService.registerUser(userAddRequest);
		if (userDtls == null) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(409, "User already exists in the database", null));
		} else {
			UserRestrictedResponse userRestrictedResponse = new UserRestrictedResponse(
					userDtls.getId(), userDtls.getEmail(), userDtls.getRole());
			
			// mail to the newly created user
			// recipient, msgBody, subject, attachment
			EmailDetails emailDetails = new EmailDetails(userAddRequest.getEmail(), 
					"Welcome to Task-Flow Managaement System\nGo to the link: http://localhost:5173/login", "New Register Confirmation", null);
			String status = emailServiceImpl.sendSimpleEmail(emailDetails);
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(201, "User registered successfully. "+status, userRestrictedResponse));
		}
	}

	/*
	 * Success:
	 * 	{
		    "status": 200,
		    "message": "User password successfully updated",
		    "data": null,
		    "timestamp": "2025-10-02T14:01:04.3751888"
		}
	 * Failure: { "status":
	 * 404, "message": "User not found", "data": null, "timestamp":
	 * "2025-10-01T17:01:10.456Z" }
	 */
	@Operation(summary = "Update user password", description = "Allows a logged-in user (ADMIN/USER) to update their password.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User password successfully updated"),
			@ApiResponse(responseCode = "400", description = "Invalid input or request body"),
			@ApiResponse(responseCode = "401", description = "Authentication required - only for logged-in users"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	@PutMapping("/users")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Void>> updateUserPassword(@RequestBody UserRequest userRequest) {
		logger.info("/user encountered >> updateUserPassword(@RequestBody UserRequest userRequest)");

		String result = userService.updatePassword(userRequest);

		if ("User not found".equals(result)) {
			com.jbs.tfv3.dto.ApiResponse<Void> response = new com.jbs.tfv3.dto.ApiResponse<>(404, "User not found",
					null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if ("Invalid or expired OTP".equals(result)) {
			com.jbs.tfv3.dto.ApiResponse<Void> response = new com.jbs.tfv3.dto.ApiResponse<>(400,
					"Invalid or expired OTP", null);
			return ResponseEntity.badRequest().body(response);
		}

		com.jbs.tfv3.dto.ApiResponse<Void> response = new com.jbs.tfv3.dto.ApiResponse<>(200,
				"User password successfully updated", null);
		return ResponseEntity.ok(response);
	}

	/*
	Success:
	{
	    "status": 200,
	    "message": "User successfully deleted",
	    "data": {
	        "id": 13,
	        "email": "arunava.nandi@yahoo.com",
	        "role": "ROLE_USER"
	    },
	    "timestamp": "2025-10-02T15:06:17.1251354"
	}
	Failure:
	{
	    "status": 404,
	    "message": "Unable to delete the user",
	    "data": null,
	    "timestamp": "2025-10-02T15:07:12.8872801"
	}
	*/
	@Operation(summary = "Delete user", description = "Allows a to delete an user (ADMIN).")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User successfully deleted"),
			@ApiResponse(responseCode = "400", description = "Invalid input or request body"),
			@ApiResponse(responseCode = "401", description = "Authentication required - only for logged-in ADMIN"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	@DeleteMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<UserRestrictedResponse>> deleteUser(@RequestBody EmailRequest emailRequest) {
		logger.info("/users/ encountered >> deleteUser(@RequestBody EmailRequest emailRequest)");
		UserDtls userDtls = userService.deleteUserByEmail(emailRequest.getEmail());
		if (userDtls == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new com.jbs.tfv3.dto.ApiResponse<>(404, "Unable to delete the user", null));
		} else {
			UserRestrictedResponse userRestrictedResponse = new UserRestrictedResponse(userDtls.getId(), userDtls.getEmail(), userDtls.getRole());
			return ResponseEntity.ok(new com.jbs.tfv3.dto.ApiResponse<>(200, "User successfully deleted", userRestrictedResponse));
		}
	}

	/*
	 * Success: { "status": 200, "message": "Logout successful", "data": null,
	 * "timestamp": "2025-10-01T17:45:21.789Z" } Failure: { "status": 400,
	 * "message": "No token found in request", "data": null, "timestamp":
	 * "2025-10-01T17:46:02.456Z" }
	 */
	@Operation(tags = "Authentication", summary = "Logout", description = "This endpoint securely terminates the user's active session.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Logout Successful - User session has been terminated"),
			@ApiResponse(responseCode = "400", description = "Invalid Request - Malformed token or invalid logout request"),
			@ApiResponse(responseCode = "401", description = "Unauthorized Access - Authentication required or expired token"),
			@ApiResponse(responseCode = "403", description = "Access Denied - Token validation failed or insufficient privileges"),
			@ApiResponse(responseCode = "500", description = "Server Error - Internal server error during logout processing") })
	@PostMapping("/auth/logout")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<Void>> logout(HttpServletRequest request) {
		logger.info("/logout encountered >> logout(HttpServletRequest request)");

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			// Get token expiry from JWT
			LocalDateTime expiryDate = jwtService.extractExpiration(token).toInstant()
					.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

			tokenBlacklistService.blacklistToken(token, expiryDate);

			com.jbs.tfv3.dto.ApiResponse<Void> response = new com.jbs.tfv3.dto.ApiResponse<>(200, "Logout successful",
					null);
			return ResponseEntity.ok(response);
		}

		com.jbs.tfv3.dto.ApiResponse<Void> response = new com.jbs.tfv3.dto.ApiResponse<>(400,
				"No token found in request", null);
		return ResponseEntity.badRequest().body(response);
	}

	/*
	 * Success:
	 * 	{
		    "status": 200,
		    "message": "OTP successfully generated",
		    "data": {
		        "email": "arunava.nandi@yahoo.com",
		        "otp": "391850",
		        "expiresInSeconds": 300,
		        "message": "OTP generated successfully. (In production you should not return the OTP in response.)"
		    },
		    "timestamp": "2025-10-02T13:54:05.4733569"
		}
	 * Failure: { "status": 404, "message": "User not found for the given email",
	 * "data": null, "timestamp": "2025-10-01T18:01:42.789Z" }
	 */
	@Operation(summary = "Generate OTP for forgot password", description = "Generates a 6-digit OTP for the given registered email in case of forgot password.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OTP successfully generated"),
			@ApiResponse(responseCode = "400", description = "Invalid email format or email not registered"),
			@ApiResponse(responseCode = "404", description = "User not found for the given email") })
	@PostMapping("/otps")
	public ResponseEntity<com.jbs.tfv3.dto.ApiResponse<GenerateOtpResponse>> generateOtp(
			@Valid @RequestBody EmailRequest req) {
		logger.info("/auth/otps encountered >> generateOtp(@RequestBody EmailRequest req)");

		GenerateOtpResponse resp = otpServiceImpl.generateOtpForEmail(req.getEmail());

		if (resp == null) {
			com.jbs.tfv3.dto.ApiResponse<GenerateOtpResponse> response = new com.jbs.tfv3.dto.ApiResponse<>(404,
					"User not found for the given email", null);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		// recipient, msgBody, subject, attachment
		EmailDetails emailDetails = new EmailDetails(req.getEmail(), 
				"OTP: "+resp.getOtp()+"\nValid for 5 min.", "OTP", null);
		String status = emailServiceImpl.sendSimpleEmail(emailDetails);
		resp.setMessage(resp.getMessage()+", "+status);
		com.jbs.tfv3.dto.ApiResponse<GenerateOtpResponse> response = new com.jbs.tfv3.dto.ApiResponse<>(200,
				"OTP successfully generated", resp);
		return ResponseEntity.ok(response);
	}

}
