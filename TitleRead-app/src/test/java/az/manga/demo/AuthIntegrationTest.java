package az.manga.demo;


import az.manga.demo.auth.dto.AuthResponseDto;
import az.manga.demo.auth.dto.LoginDto;
import az.manga.demo.auth.dto.RegisterDto;
import az.manga.demo.auth.service.AuthService;
import az.manga.demo.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthIntegrationTest extends  BaseIntegrationTest {

   @Autowired
   private AuthService authService;

   @Test
   @DisplayName("completes full auth flow: register → login → refresh → logout")
   void shouldCompleteFullAuthFlow() {
       RegisterDto registerDto = new RegisterDto();
       registerDto.setEmail("user@test.com");
       registerDto.setUsername("testuser");
       registerDto.setPassword("pass123");
       registerDto.setConfirmPassword("pass123");

       AuthResponseDto registerResponse = authService.register(registerDto);
       assertThat(registerResponse.getToken()).isNotBlank();
       assertThat(registerResponse.getToken().split("\\.")).hasSize(3);


       LoginDto loginDto = new LoginDto();
       loginDto.setEmail("user@test.com");
       loginDto.setPassword("pass123");

       AuthResponseDto loginResponse = authService.login(loginDto);
       assertThat(loginResponse.getToken()).isNotBlank();
       assertThat(loginResponse.getRefreshToken()).isNotBlank();


       AuthResponseDto refreshResponse = authService.refresh(loginResponse.getRefreshToken());
       assertThat(refreshResponse.getToken()).isNotBlank();
       assertThat(refreshResponse.getRefreshToken()).isNotBlank();

       assertThatThrownBy(() -> authService.refresh(loginResponse.getRefreshToken()))
               .isInstanceOf(UnauthorizedException.class);
       assertThat(refreshResponse.getRefreshToken()).isNotEqualTo(loginResponse.getRefreshToken());

       authService.logout(refreshResponse.getRefreshToken());

       assertThatThrownBy(() -> authService.refresh(refreshResponse.getRefreshToken()))
               .isInstanceOf(UnauthorizedException.class);
   }

   @Test
   @DisplayName("duplicate registration with same email throws ConflictException")
   void shouldThrowOnDuplicateEmail() {
       RegisterDto dto = new RegisterDto();
       dto.setEmail("duplicate@test.com");
       dto.setUsername("user1");
       dto.setPassword("pass123");
       dto.setConfirmPassword("pass123");

       authService.register(dto);

       dto.setUsername("user2");
       assertThatThrownBy(() -> authService.register(dto))
               .isInstanceOf(az.manga.demo.common.exception.ConflictException.class);
   }

   @Test
   @DisplayName("login with wrong password throws UnauthorizedException")
   void shouldThrowOnWrongPassword() {
       RegisterDto registerDto = new RegisterDto();
       registerDto.setEmail("wrongpass@test.com");
       registerDto.setUsername("wrongpassuser");
       registerDto.setPassword("pass123");
       registerDto.setConfirmPassword("pass123");
       authService.register(registerDto);

       LoginDto loginDto = new LoginDto();
       loginDto.setEmail("wrongpass@test.com");
       loginDto.setPassword("wrongpassword");

       assertThatThrownBy(() -> authService.login(loginDto))
               .isInstanceOf(UnauthorizedException.class);
   }
}
