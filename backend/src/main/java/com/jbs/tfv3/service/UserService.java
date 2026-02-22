package com.jbs.tfv3.service;

import java.util.List;

import com.jbs.tfv3.dto.LoginRequest;
import com.jbs.tfv3.dto.UserAddRequest;
import com.jbs.tfv3.dto.UserRequest;
import com.jbs.tfv3.entity.UserDtls;

public interface UserService {
	String login(LoginRequest loginRequest);
	List<UserDtls> getUserDtls();
	UserDtls registerUser(UserAddRequest request);
	String updatePassword(UserRequest request);
	UserDtls deleteUserByEmail(String email);
	UserDtls getUserByEmail(String email);
}
