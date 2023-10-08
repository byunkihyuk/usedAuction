package com.example.usedAuction.controller;

import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<Object> signUp(@RequestBody @Valid UserSignUpFormDto userSignUpFormDto){
        return userService.signUp(userSignUpFormDto);
    }

    @PostMapping("/sign-up/usernameCheck")
    public String usernameCheck(@RequestParam("username") String username){
        return userService.findByUsername(username);
    }

    @PostMapping("/sign-up/nicknameCheck")
    public String nicknameCheck(@RequestParam("nickname") String nickname){
        return userService.findByNickname(nickname);
    }
}
