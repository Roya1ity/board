package com.example.board.auth.dto;


import com.example.board.Global.Entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String pw;

    @NotBlank
    private String nick;

    @NotBlank
    private String role;
}
