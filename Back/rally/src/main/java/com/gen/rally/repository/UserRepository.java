package com.gen.rally.repository;

import com.gen.rally.entity.User;
import com.gen.rally.enums.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByNameIgnoreCase(String name);
    Optional<User> findBySocialIdAndLoginType(String socialId, LoginType loginType);
}