package az.manga.demo.auth.security;

import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.UnauthorizedException;
import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.user.api.dto.UserDto;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserPrincipalMapper {

    public UserPrincipal toUserPrincipal(UserDto dto) {
        if (dto.getRole() == null) {
            throw new UnauthorizedException(ErrorMessage.USER_ROLE_NOT_FOUND);
        }
        return new UserPrincipal(
                dto.getId(),
                dto.getEmail(),
                dto.getUsername(),
                Boolean.TRUE.equals(dto.getIsActive()),
                List.of(new SimpleGrantedAuthority("ROLE_" + dto.getRole().name()))
        );
    }
}
