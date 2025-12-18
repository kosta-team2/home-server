package com.home.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	@Query("select m from User m where m.providerId = :providerId")
	Optional<User> findByProviderId(@Param("providerId") String providerId);
}
