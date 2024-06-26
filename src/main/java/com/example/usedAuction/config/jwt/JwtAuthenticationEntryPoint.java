package com.example.usedAuction.config.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.usedAuction.dto.result.ResponseResult;
import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.errors.ErrorEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
   @Override
   public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
      // 유효한 자격증명을 제공하지 않고 접근하려 할때 401
      ObjectMapper objectMapper = new ObjectMapper();
      response.setContentType("text/html;charset=utf8;");

      if(authException instanceof BadCredentialsException){
         ResponseResultError responseResult = new ResponseResultError("fail",ErrorEnum.SIGN_IN_ERROR.getMessage());
         response.setStatus(HttpStatus.BAD_REQUEST.value());
         response.getWriter().write(objectMapper.writeValueAsString(responseResult));
      }else{
         ResponseResultError responseResultError = new ResponseResultError("fail",ErrorEnum.UNAUTHORIZED_ERROR.getMessage());
         response.setStatus(HttpStatus.UNAUTHORIZED.value());
         response.getWriter().write(objectMapper.writeValueAsString(responseResultError));
      }

   }
}
