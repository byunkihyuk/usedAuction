package com.example.usedAuction.service;

import com.example.usedAuction.errors.ApiException;
import com.example.usedAuction.errors.ErrorEnum;
import com.example.usedAuction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

   private final UserRepository userRepository;

   @Override
   @Transactional
   public UserDetails loadUserByUsername(final String username) {
      return userRepository.findOneWithAuthoritiesByUsername(username)
         .map(user -> createUser(username, user))
         //.orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
         .orElseThrow(() -> new ApiException(ErrorEnum.SIGN_IN_FAIL));
   }

   private org.springframework.security.core.userdetails.User createUser(String username, com.example.usedAuction.entity.User user) {
//      if (!user.isActivated()) {
//         throw new RuntimeException(username + " -> 활성화되어 있지 않습니다.");
//      }

//      List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
//              .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
//              .collect(Collectors.toList());
      List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
      grantedAuthorities.add(new SimpleGrantedAuthority(user.getUsername()));

      return new org.springframework.security.core.userdetails.User(user.getUsername(),
              user.getPassword(),
              grantedAuthorities);
   }
}