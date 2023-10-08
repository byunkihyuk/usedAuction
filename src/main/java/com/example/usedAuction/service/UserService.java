package com.example.usedAuction.service;

import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.entity.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    public ResponseEntity<Object> signUp( UserSignUpFormDto userSignUpFormDto) {

        userSignUpFormDto.setPassword(passwordEncoder.encode(userSignUpFormDto.getPassword()));
        User user = DataMapper.instance.userToEntity(userSignUpFormDto);
        try{
            userRepository.save(user);
        }catch (Exception e){
            throw new ApiException(ErrorEnum.SIGN_UP_ERROR);
        }

        Map<String,Object> data = new HashMap<String,Object>();
        data.put("message","회원가입 성공");

        return  ResponseEntity.status(HttpStatus.OK).body(new ResponseResult<>("success",data));
    }

    public String findByUsername(String username) {

        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()){
            return "Y";
        }
        return "N";
    }

    public String findByNickname(String nickname) {
        Optional<User> user = userRepository.findByNickname(nickname);
        if(user.isPresent()){
            return "Y";
        }
        return "N";
    }
}
