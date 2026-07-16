package az.manga.demo.user.api.dto;

import az.manga.demo.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;

    private String username;

    private String email;
    private Boolean isActive;

    private UserRole role;

    private String avatarUrl;

    private LocalDateTime createdAt;
}
