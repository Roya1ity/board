package com.example.board.auth;

import com.example.board.Global.Entity.User;
import com.example.board.Global.Entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {
    boolean existsByUser(User user);

}
