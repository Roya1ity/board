package com.example.board.Global.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "Auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false,unique = true)
    private String email;

    @Column(name = "pw", nullable = false)
    private String pw;

    @Column(name = "nick", nullable = false)
    private String nick;

    @Column(name = "provider",length = 100)
    private String provider;

    @Column(name = "provider_id",length = 100)
    private String providerId;

    @Column(name = "profile_image_url",length = 2000)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Role {
        ADMIN, USER, GUEST
    }

    public static User fromInfo(String email, String pw, String nick, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPw(pw);
        user.setNick(nick);

        if (role.toLowerCase(Locale.ROOT).equals("admin")) {
            user.setRole(Role.ADMIN);
        }
        else if (role.toLowerCase(Locale.ROOT).equals("guest")) {
            user.setRole(Role.GUEST);
        }
        else {
            user.setRole(Role.USER);
        }

        return user;
    }

}
