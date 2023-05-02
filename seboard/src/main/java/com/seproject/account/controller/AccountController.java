package com.seproject.account.controller;

import com.seproject.account.application.LogoutAppService;
import com.seproject.account.jwt.JwtDecoder;
import com.seproject.account.service.AccountService;
import com.seproject.account.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "계정 시스템 API", description = "계정(Account) 관련 API")
@AllArgsConstructor
@Controller
public class AccountController {

    private final LogoutAppService logoutAppService;
    private final AccountService accountService;
    private final TokenService tokenService;
    private final JwtDecoder jwtDecoder;

    @Operation(summary = "로그아웃", description = "로그아웃")
    @GetMapping("/logoutProc") //미해결
    public ResponseEntity<?> logout(Authentication authentication) {

        if(authentication == null) {
            return new ResponseEntity<>("로그인 상태가 아닙니다.",HttpStatus.BAD_REQUEST);
        }

        String accessToken = jwtDecoder.getAccessToken();
        tokenService.deleteToken(accessToken);
        return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
//        if(authentication instanceof OAuth2AuthenticationToken) {
//            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken)authentication;
//            logoutAppService.logout(token.getAuthorizedClientRegistrationId());
//        }
//
//        return null;
    }
}
