package com.school.service.impl;

import com.school.dto.auth.LoginRequest;
import com.school.dto.auth.TokenResponse;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Override
    public TokenResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
    @Override
    public TokenResponse refresh(String refreshToken) {
        throw new UnsupportedOperationException("TODO");
    }
}
