package com.example.usedAuction.config.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.usedAuction.dto.result.ResponseResultError;
import com.example.usedAuction.errors.ErrorEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
   @Override
   public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
      //필요한 권한이 없이 접근하려 할때 403
      //response.sendError(HttpServletResponse.SC_FORBIDDEN);
      ObjectMapper objectMapper = new ObjectMapper();
      ResponseResultError responseResultError = new ResponseResultError("error", ErrorEnum.FORBIDDEN_ERROR.getMessage());
      response.getWriter().write(objectMapper.writeValueAsString(responseResultError));
      response.setStatus(HttpStatus.FORBIDDEN.value());
   }
}
