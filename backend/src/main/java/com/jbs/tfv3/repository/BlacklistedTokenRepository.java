package com.jbs.tfv3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jbs.tfv3.entity.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
	Optional<BlacklistedToken> findByToken(String token);
	void deleteByExpiryDateBefore(LocalDateTime now);
}
