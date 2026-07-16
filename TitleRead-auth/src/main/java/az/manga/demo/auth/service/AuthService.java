package az.manga.demo.auth.service;

import az.manga.demo.auth.dto.AuthResponseDto;
import az.manga.demo.auth.dto.LoginDto;
import az.manga.demo.auth.dto.RegisterDto;

public interface AuthService {

    AuthResponseDto register(RegisterDto registerDto);
    AuthResponseDto login(LoginDto loginDto);
    AuthResponseDto refresh(String refreshToken);
    void logout(String refreshToken);
}
