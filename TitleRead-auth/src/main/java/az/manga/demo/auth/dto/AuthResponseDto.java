package az.manga.demo.auth.dto;

import az.manga.demo.user.api.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDto {
    private String token;
    private String refreshToken;
    String tokenType = "Bearer";
    private UserDto user;
}
