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

import java.util.Map;

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

    @PostMapping("/sign-up/username-check")
    public ResponseEntity<Object> usernameCheck(@RequestBody Map<String,String> getJson){
        return userService.findByUsername(getJson.get("username"));
    }

    @PostMapping("/sign-up/nickname-check")
    public ResponseEntity<Object> nicknameCheck(@RequestBody Map<String,String> getJson){
        return userService.findByNickname(getJson.get("nickname"));
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


    @GetMapping(value = "/user/{userId}/general-sell-history")
    public ResponseEntity<Object> getUserGeneralTransactionSellList(@PathVariable Integer userId) {
        return userService.getUserGeneralTransactionSellList(userId);
    }
  
    @GetMapping(value = "/user/{userId}/auction-sell-history")
    public ResponseEntity<Object> getUserAuctionTransactionSellList(@PathVariable Integer userId) {
        return userService.getUserAuctionTransactionSellList(userId);
    }
  
    @GetMapping(value = "/user/{userId}/auction-buy-history")
    public ResponseEntity<Object> getUserAuctionTransactionBuyList(@PathVariable Integer userId) {
        return userService.getUserAuctionTransactionBuyList(userId);
    }
}
