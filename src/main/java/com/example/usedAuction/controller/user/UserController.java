package com.example.usedAuction.controller.user;

import com.example.usedAuction.dto.user.UserDto;
import com.example.usedAuction.dto.user.UserSignInFormDto;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.dto.user.UserUpdateForm;
import com.example.usedAuction.service.user.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {


    @Autowired
    private UserService userService;

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

    @PostMapping("/sign-in")
    public ResponseEntity<Object> signIn(@RequestBody UserSignInFormDto userSignInFormDto){
        return userService.signIn(userSignInFormDto);
    }


    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<Object> getMyPage(@PathVariable Integer userId) {
        return userService.getUserPage(userId);
    }
    @PutMapping(value = "/user/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Integer userId, @RequestBody @Valid UserUpdateForm userUpdateForm){
        return userService.updateUser(userId,userUpdateForm);
    }

    @GetMapping(value = "/user/{userId}/general-buy-history")
    public ResponseEntity<Object> getUserGeneralTransactionList(@PathVariable Integer userId) {
        return userService.getUserGeneralTransactionList(userId);
    }

    @GetMapping(value = "/user/{userId}/auction-sell-history")
    public ResponseEntity<Object> getUserAuctionTransactionSellList(@PathVariable Integer userId) {
        return userService.getUserAuctionTransactionSellList(userId);
    }

}
