package com.example.usedAuction.service.user;

import com.example.usedAuction.config.jwt.JwtFilter;
import com.example.usedAuction.config.jwt.TokenProvider;
import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.dto.user.UserDto;
import com.example.usedAuction.dto.user.UserSignInFormDto;
import com.example.usedAuction.dto.user.UserSignUpFormDto;
import com.example.usedAuction.dto.user.UserUpdateForm;
import com.example.usedAuction.entity.general.GeneralTransaction;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.auction.AuctionTransactionRepository;
import com.example.usedAuction.repository.general.GeneralTransactionRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final GeneralTransactionRepository generalTransactionRepository;
    private final AuctionTransactionRepository auctionTransactionRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional
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

    @Transactional(readOnly = true)
    public ResponseEntity<Object> findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if(user!=null){
            ResponseResultError result = new ResponseResultError();
            result.setStatus("fail");
            result.setMessage("사용중인 아이디입니다.");
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
        ResponseResult<Object> result = new ResponseResult<>();
        Map<String, String> data = new HashMap<>();
        result.setStatus("success");
        data.put("message","사용 가능합니다.");
        result.setData(data);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> findByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElse(null);

        if(user!=null){
            ResponseResultError result = new ResponseResultError();
            result.setStatus("fail");
            result.setMessage("사용중인 닉네임입니다.");
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
        ResponseResult<Object> result = new ResponseResult<>();
        Map<String, String> data = new HashMap<>();
        result.setStatus("success");
        data.put("message","사용 가능합니다.");
        result.setData(data);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> signIn(UserSignInFormDto userSignInFormDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userSignInFormDto.getUsername(), userSignInFormDto.getPassword());

        // authenticate() 함수 실행 될때 loadUserByUsername() 메소드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);

        // 헤더에 토큰 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        Map<String, Object> data = new HashMap<>();
        data.put("message","로그인 성공");
        data.put("token",jwt);
        data.put("nickname",userSignInFormDto.getUsername());

        return ResponseEntity.status(HttpStatus.OK)
                .headers(httpHeaders)
                .body(new ResponseResult<>("success",data));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserPage(Integer userId) {
        // 로그인한 유저정보
        String username = SecurityUtil.getCurrentUsername()
                .orElse("");
        // 검색한 유저 정보
        // 없으면 유저가 없는 에러
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

        HttpStatus httpStatus = HttpStatus.OK;
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        UserDto userDto = DataMapper.instance.UserEntityToDto(user);

        // 본인
        if (username.equals(user.getUsername())) {
            userDto.setPassword("");
            userDto.setAuthor(true);
            result.setData(userDto);
        } else { // 본인이 아님
            userDto.setPassword("");
            userDto.setAddress("");
            userDto.setDetailAddress("");
            userDto.setPhone("");
            userDto.setAuthor(false);
            result.setData(userDto);
        }
        return ResponseEntity.status(httpStatus).body(result);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserPage() {

        User user = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

        HttpStatus httpStatus = HttpStatus.OK;
        ResponseResult<Object> result = new ResponseResult<>();
        result.setStatus("success");
        UserDto userDto = DataMapper.instance.UserEntityToDto(user);
        userDto.setAuthor(true);
        userDto.setPassword("");
        result.setData(userDto);

        return ResponseEntity.status(httpStatus).body(result);
    }

    @Transactional
    public ResponseEntity<Object> updateUser(Integer userId, UserUpdateForm userUpdateForm) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;
        Map<String,Object> failData = new HashMap<>();

        String username = SecurityUtil.getCurrentUsername().orElse("");

        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));
        User idUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));
        //userUpdateForm.setPassword(passwordEncoder.encode(userUpdateForm.getPassword()));

        if(!loginUser.getUsername().equals(idUser.getUsername())){
            status = HttpStatus.BAD_REQUEST;
            result.setStatus("fail");
            failData.put("message","본인이 아닙니다.");
            result.setData(failData);
            return ResponseEntity.status(status).body(result);
        }

        if(userUpdateForm.getPassword()==null || !passwordEncoder.matches(userUpdateForm.getPassword(),loginUser.getPassword())){
            status = HttpStatus.BAD_REQUEST;
            result.setStatus("fail");
            failData.put("message","현재 비밀번호가 일치하지 않습니다.");
            result.setData(failData);
            return ResponseEntity.status(status).body(result);
        }else{
            loginUser.setAddress(userUpdateForm.getAddress());
            loginUser.setDetailAddress(userUpdateForm.getDetailAddress());
            loginUser.setNickname(userUpdateForm.getNickname());
            UserDto resultUser =DataMapper.instance.UserEntityToDto(loginUser);
            resultUser.setPassword("");
            result.setData(resultUser);
            result.setStatus("success");
        }
        return ResponseEntity.status(status).body(result);
    }

    @Transactional
    public ResponseEntity<Object> getUserGeneralTransactionBuyList(Integer userId, Integer size, Integer page, String sort) {
        Sort pageableSort = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
                Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page,size, pageableSort);

        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;

        User idUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
                .orElseThrow(()->  new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(loginUser.getUsername()==null || !loginUser.getUserId().equals(idUser.getUserId())){
            return ResponseEntity.status(status).body(new ResponseResultError("fail","본인 인증 실패"));
        }

        List<GeneralTransactionDto> generalTransactionDtoList = generalTransactionRepository.findAllByBuyer(loginUser,pageable)
                .stream().map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());
        result.setData(generalTransactionDtoList);
        result.setStatus("success");
        return ResponseEntity.status(status).body(result);
    }

    public ResponseEntity<Object> getUserGeneralTransactionSellList(Integer userId, Integer size, Integer page, String sort) {
        Sort pageableSort = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
                Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page,size, pageableSort);

        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;

        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

        List<GeneralTransactionDto> generalTransactionDtoList = generalTransactionRepository.findAllBySeller(loginUser,pageable)
                .stream().map(DataMapper.instance::generalTransactionToDto).collect(Collectors.toList());

        result.setData(generalTransactionDtoList);
        result.setStatus("success");
        return ResponseEntity.status(status).body(result);
    }

    public ResponseEntity<Object> getUserAuctionTransactionSellList(Integer userId) {
      ResponseResult<Object> result = new ResponseResult<>();
      HttpStatus status = HttpStatus.OK;

      User idUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

      List<AuctionTransactionDto> auctionTransactionDtoList = auctionTransactionRepository.findAllBySellerOrderByCreatedAtDesc(idUser)
              .stream().map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
     
      result.setData(auctionTransactionDtoList);
      result.setStatus("success");
      return ResponseEntity.status(status).body(result);
   }

    public ResponseEntity<Object> getUserAuctionTransactionBuyList(Integer userId) {
        ResponseResult<Object> result = new ResponseResult<>();
        HttpStatus status = HttpStatus.OK;

        String username = SecurityUtil.getCurrentUsername().orElse("");

        User loginUser = userRepository.findByUsername(username)
                .orElse(new User());

        User idUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

        if(loginUser.getUsername()==null || !loginUser.getUserId().equals(idUser.getUserId())){
            return ResponseEntity.status(status).body(new ResponseResultError("fail","본인 인증 실패"));
        }

        List<AuctionTransactionDto> generalTransactionDtoList = auctionTransactionRepository.findAllByBuyerOrderByCreatedAtDesc(idUser)
                .stream().map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());

        result.setData(generalTransactionDtoList);
        result.setStatus("success");

        return ResponseEntity.status(status).body(result);
    }
}

