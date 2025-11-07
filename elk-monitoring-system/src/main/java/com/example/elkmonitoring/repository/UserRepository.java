package com.example.elkmonitoring.repository;

import com.example.elkmonitoring.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByStatus(User.UserStatus status);

    boolean existsByEmail(String email);
}
