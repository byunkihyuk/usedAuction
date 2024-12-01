package com.example.usedAuction.config.jwt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class JwtFilter extends GenericFilterBean {

   public static final String AUTHORIZATION_HEADER = "Authorization";
//   private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
   private static final Logger logger = LogManager.getLogger(JwtFilter.class);
   private final TokenProvider tokenProvider;
   public JwtFilter(TokenProvider tokenProvider) {
      this.tokenProvider = tokenProvider;
   }

   public static String getClientIp(HttpServletRequest request) {

      String clientIp = request.getHeader("X-Forwarded-For");
      if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
         clientIp = request.getHeader("Proxy-Client-IP");
      }
      if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
         clientIp = request.getHeader("WL-Proxy-Client-IP");
      }
      if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
         clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
      }
      if (clientIp  == null || clientIp .length() == 0 || "unknown".equalsIgnoreCase(clientIp )) {
         clientIp  = request.getHeader("X-Real-IP");
      }
      if (clientIp  == null || clientIp .length() == 0 || "unknown".equalsIgnoreCase(clientIp )) {
         clientIp  = request.getHeader("X-RealIP");
      }
      if (clientIp  == null || clientIp .length() == 0 || "unknown".equalsIgnoreCase(clientIp )) {
         clientIp  = request.getHeader("REMOTE_ADDR");
      }

      if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
         clientIp = request.getRemoteAddr();
      }

      if(clientIp.equals("0:0:0:0:0:0:0:1") || clientIp.equals("127.0.0.1")){
         InetAddress address = null;
         try {
            address = InetAddress.getLocalHost();
         } catch (UnknownHostException e) {
            throw new RuntimeException(e);
         }
         clientIp = /* address.getHostName() + "/" + */ address.getHostAddress();
      }
      return clientIp;
   }

   @Override
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
      HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
      String jwt = resolveToken(httpServletRequest);
      String requestURI = httpServletRequest.getRequestURI();

      String requestIp = getClientIp((HttpServletRequest) servletRequest);

      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
         Authentication authentication = tokenProvider.getAuthentication(jwt);
         SecurityContextHolder.getContext().setAuthentication(authentication);
         logger.info("IP: "+requestIp+", Username: "+authentication.getName()+", uri: " +requestURI);
      } else {
         logger.info("IP: "+requestIp+", Not Token, uri: "+ requestURI);
      }

      filterChain.doFilter(servletRequest, servletResponse);
   }

   private String resolveToken(HttpServletRequest request) {
      String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

      if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
         return bearerToken.substring(7);
      }

      return null;
   }
}
