package com.example.usedAuction.service.user;

import com.example.usedAuction.config.jwt.JwtFilter;
import com.example.usedAuction.config.jwt.TokenProvider;
import com.example.usedAuction.dto.DataMapper;
import com.example.usedAuction.dto.auction.AuctionTransactionDto;
import com.example.usedAuction.dto.general.GeneralTransactionDto;
import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.dto.user.*;
import com.example.usedAuction.entity.user.User;
import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.auction.AuctionTransactionRepository;
import com.example.usedAuction.repository.general.GeneralTransactionRepository;
import com.example.usedAuction.repository.user.UserRepository;
import com.example.usedAuction.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private final UserRepository userRepository;

    private final JavaMailSender javaMailSender;
    // redis
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${spring.mail.username}")
    private String SENDER_MAIL;
    @Value("${spring.mail.auth-timeout}")
    private Long MAIL_TIMEOUT;

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

        User loginUser = userRepository.findByUsername(userSignInFormDto.getUsername())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER));

        String accessToken = tokenProvider.createToken(authentication,loginUser.getNickname());
        // 수정 - 리프레시 토큰 발급
        String refreshToken = tokenProvider.createToken(authentication,loginUser.getNickname());

        // 헤더에 토큰 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + accessToken);
        // 수정 - 쿠키에 리프레시 토큰 추가
        

        UserDto userDto = DataMapper.instance.UserEntityToDto(userRepository.findByUsername(userSignInFormDto.getUsername())
                .orElseThrow(()->new ApiException(ErrorEnum.NOT_FOUND_USER)));

        Map<String, Object> data = new HashMap<>();
        data.put("message","로그인 성공");
        data.put("accessToken",accessToken);
        data.put("refreshToken",refreshToken);
        data.put("nickname",userDto.getNickname());

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

        User loginUser = userRepository.findByUsername(SecurityUtil.getCurrentUsername().orElse(""))
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
        }
        try{
            loginUser.setNickname(userUpdateForm.getNickname());
            if(userUpdateForm.getChangePassword()!=null && !userUpdateForm.getChangePassword().isEmpty()){
                loginUser.setPassword(passwordEncoder.encode(userUpdateForm.getChangePassword()));
            }
            loginUser.setAddress(userUpdateForm.getAddress());
            loginUser.setDetailAddress(userUpdateForm.getDetailAddress());

            UserDto resultUser =DataMapper.instance.UserEntityToDto(loginUser);
            resultUser.setPassword("");
            result.setData(resultUser);
            result.setStatus("success");
        }catch (Exception e){
            throw new ApiException(ErrorEnum.USER_UPDATE_ERROR);
        }

        return ResponseEntity.status(status).body(result);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
   public ResponseEntity<Object> getUserAuctionTransactionSellList(Integer userId, Integer size, Integer page, String sort) {
       Sort pageableSort = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
               Sort.by("createdAt").descending();
       Pageable pageable = PageRequest.of(page,size, pageableSort);

      ResponseResult<Object> result = new ResponseResult<>();
      HttpStatus status = HttpStatus.OK;

      User idUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorEnum.NOT_FOUND_USER));

      List<AuctionTransactionDto> auctionTransactionDtoList = auctionTransactionRepository.findAllBySeller(idUser,pageable)
              .stream().map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());
     
      result.setData(auctionTransactionDtoList);
      result.setStatus("success");
      return ResponseEntity.status(status).body(result);
   }

    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserAuctionTransactionBuyList(Integer userId, Integer size, Integer page, String sort) {
        Sort pageableSort = sort.equals("asc") ? Sort.by("createdAt").ascending()  :
                Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page,size, pageableSort);

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

        List<AuctionTransactionDto> generalTransactionDtoList = auctionTransactionRepository.findAllByBuyer(idUser,pageable)
                .stream().map(DataMapper.instance::auctionTransactionToDto).collect(Collectors.toList());

        result.setData(generalTransactionDtoList);
        result.setStatus("success");

        return ResponseEntity.status(status).body(result);
    }

    public String createNumber(){
        StringBuilder sb = new StringBuilder();
        try{
            Random random = SecureRandom.getInstanceStrong();
            for(int i=0; i<6; i++){
                sb.append(random.nextInt(10));
            }
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.CREATE_MAIL_SECURE_NUMBER_ERROR);
        }
        return sb.toString();
    }

    public String createPassword(){
        String charString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder();

        try{
            Random random = SecureRandom.getInstanceStrong();
            for(int i=0; i<6; i++){
                sb.append(charString.charAt(random.nextInt(charString.length())));
            }
        }catch (Exception e ){
            throw new ApiException(ErrorEnum.CREATE_MAIL_SECURE_NUMBER_ERROR);
        }
        return sb.toString();
    }

    @Transactional
    public ResponseEntity<Object> sendMail(MailAuthDto mailAuthDto){
        User user = userRepository.findByUsername(mailAuthDto.getMail())
                        .orElse(null);

        if(user!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","사용중인 아이디입니다."));
        }

        mailAuthDto.setNumber(String.valueOf(createNumber()));

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try{
            mimeMessage.setFrom(SENDER_MAIL);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, mailAuthDto.getMail());
            mimeMessage.setSubject("usedAuction 이메일 인증");
            String body = "<h2>usedAuction 이메일 인증번호 입니다.</h2>" +
                    "<h1>"+mailAuthDto.getNumber()+"</h1>";
            mimeMessage.setText(body,"UTF-8","html");
            stringRedisTemplate.opsForValue()
                    .set(mailAuthDto.getMail(),mailAuthDto.getNumber(), Duration.ofSeconds(MAIL_TIMEOUT));
            javaMailSender.send(mimeMessage);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","인증 메일 발송 실패"));
        }

        ResponseResult<Object> result = new ResponseResult<>();
        Map<String,String> map = new HashMap<>();
        map.put("data","인증 메일 전송 성공");
        result.setData(map);
        result.setStatus("success");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    public ResponseEntity<Object> getMailAuth(MailAuthDto mailAuthDto) {

        ResponseResult<Object> result = new ResponseResult<>();
        Map<String,Object> map = new HashMap<>();
        String authNumber = stringRedisTemplate.opsForValue().get(mailAuthDto.getMail());
        if(authNumber!=null){
            if(mailAuthDto.getNumber().equals(authNumber)){
                map.put("auth",true);
                result.setStatus("success");
            }else{
                map.put("auth",false);
                result.setStatus("success");
            }
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","인증 시간이 초과되었습니다."));
        }
        result.setData(map);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public ResponseEntity<Object> phoneCheck(String phone) {
        User user = userRepository.findByPhone(phone).orElse(null);
        ResponseResult<Object> result = new ResponseResult<>();
        Map<String,String> map = new HashMap<>();
        if(user==null){
            map.put("message","사용 가능합니다.");
            result.setStatus("success");
            result.setData(map);
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","사용중인 번호입니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Transactional
    public ResponseEntity<Object> findPassword(String username) {
        System.out.println(username);
        User user = userRepository.findByUsername(username)
                .orElse(null);

        Map<String, String> result = new HashMap<>();

        if(user==null){
            result.put("message","임시 비밀번호 전송 실패.");
            result.put("status","fail");
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        String temporary = createPassword();
        try{
            mimeMessage.setFrom(SENDER_MAIL);
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, username);
            mimeMessage.setSubject("usedAuction 임시 비밀번호 발급");
            String body = "<h2>usedAuction 임시 비밀번호 입니다.</h2>" +
                    "<h1>"+temporary+"</h1>";
            mimeMessage.setText(body,"UTF-8","html");
            javaMailSender.send(mimeMessage);

            user.setPassword(passwordEncoder.encode(temporary));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseResultError("fail","임시 비밀번호 발송 실패."));
        }
        result.put("message","임시 비밀번호 전송 완료.");
        result.put("status","success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public UserDto getLoginUser(String nickname) {
        User loginUser = userRepository.findByNickname(nickname)
                .orElseThrow(()-> new ApiException(ErrorEnum.NOT_FOUND_USER));

        return DataMapper.instance.UserEntityToDto(loginUser);
    }
}

