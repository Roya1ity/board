package com.example.board.user;

import com.example.board.auth.CustomUserDetails;
import com.example.board.auth.LoginUserId;
import com.example.board.user.dto.UserProfileDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserProfileController {
    private final UserProfileService userProfileService;


    @GetMapping("/me")
    public UserProfileDTO me(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return userProfileService.me(userDetails);
    }
}
