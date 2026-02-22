package com.jbs.tfv3.repository;

import com.jbs.tfv3.entity.Otp;
import com.jbs.tfv3.entity.UserDtls;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpRepository extends JpaRepository<Otp, Long> {
	// deactivate active OTPs for a user
    @Modifying
    @Query("UPDATE Otp o SET o.isActive = false WHERE o.userDtls.id = :userId AND o.isActive = true")
    int deactivateActiveOtpsForUserDtls(@Param("userId") Long userId);

    Optional<Otp> findTopByUserDtlsAndIsActiveTrueOrderByCreatedAtDesc(UserDtls userDtls);
    
    @Query("SELECT o FROM Otp o WHERE o.userDtls.email = :email AND o.otp = :otp AND o.isActive = true ORDER BY o.createdAt DESC")
    Optional<Otp> findValidOtp(@Param("email") String email, @Param("otp") String otp);
    
    void deleteByUserDtls(UserDtls userDtls);
}
