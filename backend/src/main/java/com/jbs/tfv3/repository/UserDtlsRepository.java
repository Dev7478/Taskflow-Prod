package com.jbs.tfv3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jbs.tfv3.entity.UserDtls;

public interface UserDtlsRepository extends JpaRepository<UserDtls, Long>{
	Optional<UserDtls> findByEmail(String email);
    Optional<UserDtls> findByEmailIgnoreCase(String email); // helpful if you want case-insensitive
    boolean existsByEmailIgnoreCase(String email);
}
