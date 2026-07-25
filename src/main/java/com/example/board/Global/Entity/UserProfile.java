package com.example.board.Global.Entity;

import com.example.board.user.dto.UserProfileDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false,unique = true)
    private User user;

    @Column(name = "name")
    private String name;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "birth")
    private String birth;

    @Column(name = "number")
    private String number;

    @Column(name = "address")
    private String address;

    @Column(name = "create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @LastModifiedDate
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static UserProfileDTO toDTO(UserProfile userProfile) {
        UserProfileDTO res = new UserProfileDTO();
        res.setName(userProfile.getName());
        res.setNumber(userProfile.getNumber());
        res.setBirth(userProfile.getBirth());
        res.setCreateAt(userProfile.getCreateAt().toString());

        return res;
    }
}
