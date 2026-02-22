package com.jbs.tfv3.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jbs.tfv3.entity.UserDtls;
import com.jbs.tfv3.repository.UserDtlsRepository;
import com.jbs.tfv3.security.ExtendedUserDetails;

@Service
public class UserDtlsService implements UserDetailsService {
	
	@Autowired
	private UserDtlsRepository userDtlsRepository;

	@Override
	public UserDetails loadUserByUsername(String username) // Here user name refers to the email
			throws UsernameNotFoundException {
		Optional<UserDtls> optionalUser = userDtlsRepository.findByEmail(username);
		UserDtls userDtls = optionalUser.get();
		if(userDtls == null) {
			throw new UsernameNotFoundException("Invalid username!");
		}
		return new ExtendedUserDetails(userDtls);
	}
	
	public String getRoleFromEmail(String email) { // Here username refers to the email
		Optional<UserDtls> optionalUser = userDtlsRepository.findByEmail(email);
		UserDtls userDtls = optionalUser.get();
		if(userDtls == null) {
			throw new UsernameNotFoundException("Invalid username!");
		}
		return userDtls.getRole();
	}
	
	public Long getUserIdFroEmail(String email) {
		Optional<UserDtls> optionalUser = userDtlsRepository.findByEmail(email);
		UserDtls userDtls = optionalUser.get();
		if(userDtls == null) {
			throw new UsernameNotFoundException("Invalid username!");
		}
		return userDtls.getId();
	}

}
