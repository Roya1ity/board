package com.example.board.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false,unique = true)
    private String email;

    @Column(name = "pw", nullable = false)
    private String pw;

    @Column(name = "nick", nullable = false)
    private String nick;

    @Column(name = "role")
    @Builder.Default
    private Role role = Role.User;

    @Column(name = "create_at")
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Role {
        Admin, User, Guest
    }

}
