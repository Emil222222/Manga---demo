package az.manga.demo.auth.service;

import az.manga.demo.auth.dto.AuthResponseDto;
import az.manga.demo.auth.dto.LoginDto;
import az.manga.demo.auth.dto.RegisterDto;
import az.manga.demo.auth.security.JwtService;
import az.manga.demo.auth.security.UserPrincipalMapper;
import az.manga.demo.auth.service.impl.AuthServiceImpl;
import az.manga.demo.common.enums.UserRole;
import az.manga.demo.common.exception.BadRequestException;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.ForbiddenException;
import az.manga.demo.common.exception.UnauthorizedException;
import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.user.api.UserService;
import az.manga.demo.user.api.dto.CreateUserCommand;
import az.manga.demo.user.api.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserPrincipalMapper userPrincipalMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserPrincipal buildPrincipal(Long id, boolean isActive) {
        return new UserPrincipal(id, "test@mail.com", "testuser", isActive,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private UserDto buildUserDto(Long id, boolean isActive) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setEmail("test@mail.com");
        dto.setUsername("testuser");
        dto.setIsActive(isActive);
        dto.setRole(UserRole.USER);
        return dto;
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("регистрирует пользователя и возвращает токены")
        void shouldRegisterAndReturnTokens() {
            RegisterDto dto = new RegisterDto();
            dto.setEmail("test@mail.com");
            dto.setPassword("pass123");
            dto.setConfirmPassword("pass123");

            UserDto userDto = buildUserDto(1L, true);
            UserPrincipal principal = buildPrincipal(1L, true);

            when(userService.createUser(any(CreateUserCommand.class))).thenReturn(userDto);
            when(userPrincipalMapper.toUserPrincipal(userDto)).thenReturn(principal);
            when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
            when(refreshTokenService.create(1L)).thenReturn("refresh-token");

            AuthResponseDto result = authService.register(dto);
            AuthResponseDto expected = AuthResponseDto.builder()
                    .token("access-token")
                    .refreshToken("refresh-token")
                    .build();

            assertThat(result).isEqualTo(expected);

            verify(userService).createUser(any(CreateUserCommand.class));
            verify(userPrincipalMapper).toUserPrincipal(userDto);
            verify(jwtService).generateAccessToken(principal);
            verify(refreshTokenService).create(1L);
        }

        @Test
        @DisplayName("выбрасывает BadRequestException если пароли не совпадают")
        void shouldThrowBadRequestIfPasswordsMismatch() {
            RegisterDto dto = new RegisterDto();
            dto.setPassword("pass123");
            dto.setConfirmPassword("different");

            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOf(BadRequestException.class);


            verify(userService, never()).createUser(any());
            verifyNoInteractions(userPrincipalMapper, jwtService, refreshTokenService);
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("выполняет вход и возвращает токены")
        void shouldLoginAndReturnTokens() {
            LoginDto dto = new LoginDto();
            dto.setEmail("test@mail.com");
            dto.setPassword("pass123");

            UserDto userDto = buildUserDto(1L, true);
            UserPrincipal principal = buildPrincipal(1L, true);

            when(userService.findByEmail(dto.getEmail())).thenReturn(userDto);
            when(userPrincipalMapper.toUserPrincipal(userDto)).thenReturn(principal);
            when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
            when(refreshTokenService.create(1L)).thenReturn("refresh-token");

            AuthResponseDto result = authService.login(dto);
            AuthResponseDto expected = AuthResponseDto.builder()
                    .token("access-token")
                    .refreshToken("refresh-token")
                    .build();

            assertThat(result).isEqualTo(expected);

            verify(userService).findByEmail(dto.getEmail());
            verify(userPrincipalMapper).toUserPrincipal(userDto);
            verify(jwtService).generateAccessToken(principal);
            verify(refreshTokenService).create(1L);
            verify(userService).verifyPassword(1L, dto.getPassword());
        }

        @Test
        @DisplayName("выбрасывает исключение если пароль неверный")
        void shouldThrowIfPasswordInvalid() {
            LoginDto dto = new LoginDto();
            dto.setEmail("test@mail.com");
            dto.setPassword("wrongpass");

            UserDto userDto = buildUserDto(1L, true);
            UserPrincipal principal = buildPrincipal(1L, true);

            when(userService.findByEmail(dto.getEmail())).thenReturn(userDto);
            when(userPrincipalMapper.toUserPrincipal(userDto)).thenReturn(principal);
            doThrow(new BadRequestException(ErrorMessage.INVALID_CREDENTIALS))
                    .when(userService).verifyPassword(1L, dto.getPassword());

            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(BadRequestException.class);

            verifyNoInteractions(jwtService, refreshTokenService);
        }

        @Test
        @DisplayName("выбрасывает ForbiddenException если аккаунт деактивирован")
        void shouldThrowForbiddenIfAccountDeactivated() {
            LoginDto dto = new LoginDto();
            dto.setEmail("test@mail.com");
            dto.setPassword("pass123");

            UserDto userDto = buildUserDto(1L, false);
            UserPrincipal principal = buildPrincipal(1L, false);

            when(userService.findByEmail(dto.getEmail())).thenReturn(userDto);
            when(userPrincipalMapper.toUserPrincipal(userDto)).thenReturn(principal);

            assertThatThrownBy(() -> authService.login(dto))
                    .isInstanceOf(ForbiddenException.class);

            verify(userService, never()).verifyPassword(any(), any());
            verifyNoInteractions(jwtService, refreshTokenService);
        }
    }

    @Nested
    @DisplayName("refresh()")
    class Refresh {

        @Test
        @DisplayName("обновляет токены и ротирует refresh токен")
        void shouldRefreshAndRotateToken() {
            String oldRefreshToken = "old-refresh-token";
            String newRefreshToken = "new-refresh-token";
            Long userId = 1L;

            UserDto userDto = buildUserDto(userId, true);
            UserPrincipal principal = buildPrincipal(userId, true);

            when(refreshTokenService.validate(oldRefreshToken)).thenReturn(userId);
            when(userService.findById(userId)).thenReturn(userDto);
            when(userPrincipalMapper.toUserPrincipal(userDto)).thenReturn(principal);
            when(refreshTokenService.rotate(oldRefreshToken, userId)).thenReturn(newRefreshToken);
            when(jwtService.generateAccessToken(principal)).thenReturn("new-access-token");

            AuthResponseDto result = authService.refresh(oldRefreshToken);

            AuthResponseDto expected = AuthResponseDto.builder()
                    .token("new-access-token")
                    .refreshToken("new-refresh-token")
                    .build();

            assertThat(result).isEqualTo(expected);

            verify(refreshTokenService).validate(oldRefreshToken);
            verify(userService).findById(userId);
            verify(userPrincipalMapper).toUserPrincipal(userDto);
            verify(jwtService).generateAccessToken(principal);
            verify(refreshTokenService).rotate(oldRefreshToken, userId);
        }

        @Test
        @DisplayName("выбрасывает исключение если refresh токен невалиден")
        void shouldThrowIfRefreshTokenInvalid() {
            when(refreshTokenService.validate("invalid-token"))
                    .thenThrow(new UnauthorizedException(ErrorMessage.TOKEN_INVALID));

            assertThatThrownBy(() -> authService.refresh("invalid-token"))
                    .isInstanceOf(UnauthorizedException.class);

            verifyNoInteractions(userService, userPrincipalMapper, jwtService);
        }
    }

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("отзывает refresh токен")
        void shouldRevokeRefreshToken() {
            String refreshToken = "refresh-token";

            authService.logout(refreshToken);

            verify(refreshTokenService).revoke(refreshToken);
        }
    }
}
