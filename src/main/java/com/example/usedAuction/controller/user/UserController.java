package com.example.usedAuction.controller.user;

import com.example.usedAuction.dto.user.*;
import com.example.usedAuction.service.user.UserService;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "사용자",description = "UserController")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "회원가입 API",description = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<Object> signUp(@RequestBody @Valid UserSignUpFormDto userSignUpFormDto){
        return userService.signUp(userSignUpFormDto);
    }

    @Operation(summary = "회원가입 아이디 중복확인 API",description = "회원가입 아이디 중복확인")
    @PostMapping("/sign-up/username-check")
    public ResponseEntity<Object> usernameCheck(@RequestBody Map<String,String> getJson){
        return userService.findByUsername(getJson.get("username"));
    }

    @Operation(summary = "회원가입 닉네임 중복확인 API",description = "회원가입 닉네임 중복확인")
    @PostMapping("/sign-up/nickname-check")
    public ResponseEntity<Object> nicknameCheck(@RequestBody Map<String,String> getJson){
        return userService.findByNickname(getJson.get("nickname"));
    }

    @Operation(summary = "회원가입 전화번호 중복확인 API",description = "회원가입 전화번호 중복확인")
    @PostMapping("/sign-up/phone-check")
    public ResponseEntity<Object> phoneCheck(@RequestBody Map<String,String> getJson){
        return userService.phoneCheck(getJson.get("phone"));
    }

    @Operation(summary = "로그인 API",description = "로그인")
    @PostMapping("/sign-in")
    public ResponseEntity<Object> signIn(@RequestBody UserSignInFormDto userSignInFormDto){
        return userService.signIn(userSignInFormDto);
    }


    @Operation(summary = "로그인 사용자 마이페이지 조회 API (JWT 토큰 필요)",description = "로그인 사용자 마이페이지 조회")
    @GetMapping(value = "/user")
    public ResponseEntity<Object> getMyPage(@RequestParam(value = "user-id", required = false) String userId) {
        if(userId==null){
            return userService.getUserPage();
        }else{
            return userService.getUserPage(Integer.parseInt(userId));
        }
    }

    @Operation(summary = "다른 사용자 마이페이지 조회 API",description = "다른 사용자 마이페이지 조회")
    @PutMapping(value = "/user/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Integer userId, @RequestBody @Valid UserUpdateForm userUpdateForm){
        return userService.updateUser(userId,userUpdateForm);
    }

    @Operation(summary = "사용자 중고 구매내역 조회 API (JWT 토큰 필요)",description = "사용자 중고 구매내역 조회")
    @GetMapping(value = "/user/{userId}/general-buy-history")
    public ResponseEntity<Object> getUserGeneralTransactionList(@PathVariable Integer userId,
                                                                @RequestParam(value = "size", required = false)Integer size,
                                                                @RequestParam(value = "page", required = false)Integer page,
                                                                @RequestParam(value = "sort", required = false)String sort) {
        return userService.getUserGeneralTransactionBuyList(userId,size,page,sort);
    }

    @Operation(summary = "사용자 중고 판매내역 조회 API",description = "사용자 중고 판매내역 조회")
    @GetMapping(value = "/user/{userId}/general-sell-history")
    public ResponseEntity<Object> getUserGeneralTransactionSellList(@PathVariable Integer userId,
                                                                    @RequestParam(value = "size", required = false)Integer size,
                                                                    @RequestParam(value = "page", required = false)Integer page,
                                                                    @RequestParam(value = "sort", required = false)String sort) {
        return userService.getUserGeneralTransactionSellList(userId,size,page,sort);
    }

    @Operation(summary = "사용자 경매 판매내역 조회 API",description = "사용자 경매 판매내역 조회")
    @GetMapping(value = "/user/{userId}/auction-sell-history")
    public ResponseEntity<Object> getUserAuctionTransactionSellList(@PathVariable Integer userId,
                                                                    @RequestParam(value = "size", required = false)Integer size,
                                                                    @RequestParam(value = "page", required = false)Integer page,
                                                                    @RequestParam(value = "sort", required = false)String sort) {
        return userService.getUserAuctionTransactionSellList(userId,size,page,sort);
    }

    @Operation(summary = "사용자 경매 구매내역 조회 API (JWT 토큰 필요)",description = "사용자 경매 구매내역 조회")
    @GetMapping(value = "/user/{userId}/auction-buy-history")
    public ResponseEntity<Object> getUserAuctionTransactionBuyList(@PathVariable Integer userId,
                                                                   @RequestParam(value = "size", required = false)Integer size,
                                                                   @RequestParam(value = "page", required = false)Integer page,
                                                                   @RequestParam(value = "sort", required = false)String sort) {
        return userService.getUserAuctionTransactionBuyList(userId,size,page,sort);
    }

    @Operation(summary = "회원가입 아이디(이메일) 인증번호 발송 API",description = "회원가입 아이디(이메일) 인증번호 발송")
    @PostMapping(value = "/mail-auth")
    public ResponseEntity<Object> postMailAuth(@RequestBody MailAuthDto mailAuthDto){
        return userService.sendMail(mailAuthDto);
    }

    @Operation(summary = "회원가입 아이디(이메일) 인증번호 인증 API",description = "회원가입 아이디(이메일) 인증번호 인증")
    @PostMapping(value = "/mail-auth-number")
    public ResponseEntity<Object> getMailAuth(@RequestBody MailAuthDto mailAuthDto){
        return userService.getMailAuth(mailAuthDto);
    }

}
