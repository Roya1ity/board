package com.example.board.auth;

import com.example.board.Global.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByProviderId(String providerId);
    Optional<User> findByEmailAndProvider(String email, String provider);

}
