package com.school.service;

import com.school.dto.auth.LoginRequest;
import com.school.dto.auth.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(String refreshToken);
}
