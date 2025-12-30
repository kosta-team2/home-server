package com.home.domain.user;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByTokenHash(String tokenHash);

	@Modifying(flushAutomatically = true, clearAutomatically = false)
	@Query("""
        update RefreshToken rt
           set rt.revokedAt = :now
         where rt.user.id = :userId
           and rt.revokedAt is null
    """)
	int revokeActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);

}
