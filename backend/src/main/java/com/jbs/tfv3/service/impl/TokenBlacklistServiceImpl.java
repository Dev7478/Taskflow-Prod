package com.jbs.tfv3.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jbs.tfv3.Taskflowv3Application;
import com.jbs.tfv3.entity.BlacklistedToken;
import com.jbs.tfv3.repository.BlacklistedTokenRepository;
import com.jbs.tfv3.service.TokenBlacklistService;

import jakarta.transaction.Transactional;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
	
	private static final Logger logger = LoggerFactory.getLogger(Taskflowv3Application.class);
	
	@Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklistToken(String token, LocalDateTime expiryDate) {
        BlacklistedToken blacklistedToken = new BlacklistedToken(token, expiryDate);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.findByToken(token).isPresent();
    }
    
    // The @Scheduled(cron = "0 0 0 * * ?") runs every day at midnight.
    // It deletes all records in blacklisted_tokens where expiry_date < now.
    // This keeps DB clean and avoids unnecessary entries.
//     @Scheduled(cron = "0 0 * * * ?")
     @Scheduled(cron = "0 */5 * * * ?") // For 5min testing
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokenRepository.deleteByExpiryDateBefore(now);
        logger.info("[CLEANUP] Expired blacklisted tokens removed at " + now);
    }
}
