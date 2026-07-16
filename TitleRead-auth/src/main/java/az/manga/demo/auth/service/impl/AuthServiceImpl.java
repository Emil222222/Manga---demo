package az.manga.demo.auth.service.impl;

import az.manga.demo.auth.dto.AuthResponseDto;
import az.manga.demo.auth.dto.LoginDto;
import az.manga.demo.auth.dto.RegisterDto;
import az.manga.demo.auth.security.JwtService;
import az.manga.demo.auth.security.UserPrincipalMapper;
import az.manga.demo.auth.service.AuthService;
import az.manga.demo.auth.service.RefreshTokenService;
import az.manga.demo.common.exception.BadRequestException;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.ForbiddenException;
import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.user.api.UserService;
import az.manga.demo.user.api.dto.CreateUserCommand;
import az.manga.demo.user.api.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserPrincipalMapper  userPrincipalMapper;

    @Override
    public AuthResponseDto register(RegisterDto registerDto){
        log.info("Register attempt for email = {}", registerDto.getEmail());
        if(!registerDto.getPassword().equals(registerDto.getConfirmPassword())){
            throw new BadRequestException(ErrorMessage.PASSWORDS_DO_NOT_MATCH);
        }

        UserPrincipal principal = userPrincipalMapper.toUserPrincipal(
                userService.createUser(new CreateUserCommand(
                        registerDto.getUsername(),
                        registerDto.getEmail(),
                        registerDto.getPassword()
                ))
        );

        log.info("User created successfully for email = {}", registerDto.getEmail());
        return buildAuthResponseDto(principal);
    }

    @Override
    public AuthResponseDto login(LoginDto loginDto) {
        log.info("Login attempt for email = {}", loginDto.getEmail());
        UserDto dto = userService.findByEmail(loginDto.getEmail());
        UserPrincipal principal = userPrincipalMapper.toUserPrincipal(dto);

        if (!principal.isEnabled()) {
            throw new ForbiddenException(ErrorMessage.ACCOUNT_DEACTIVATED);
        }

        userService.verifyPassword(dto.getId(), loginDto.getPassword());

        return buildAuthResponseDto(principal);
    }

    @Override
    public AuthResponseDto refresh(String refreshToken) {
        Long userId = refreshTokenService.validate(refreshToken);
        UserDto dto = userService.findById(userId);
        UserPrincipal principal = userPrincipalMapper.toUserPrincipal(dto);
        String newRefreshToken = refreshTokenService.rotate(refreshToken, userId);
        return AuthResponseDto.builder()
                .token(jwtService.generateAccessToken(principal))
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AuthResponseDto buildAuthResponseDto(UserPrincipal principal){
        String refreshToken = refreshTokenService.create(principal.getId());
        return AuthResponseDto.builder()
                .token(jwtService.generateAccessToken(principal))
                .refreshToken(refreshToken)
                .build();
    }
}
