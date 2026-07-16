package az.manga.demo.user.api;

import az.manga.demo.user.api.dto.CreateUserCommand;
import az.manga.demo.user.api.dto.UserDto;
import az.manga.demo.user.dto.PasswordChangeDto;
import az.manga.demo.user.dto.UserUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

public interface UserService {

    UserDto getProfile(Long id);
    UserDto updateProfile(Long id, UserUpdateDto userDto);
    void changePassword(Long id, PasswordChangeDto password);
    void deleteProfile(Long id);
    Page<UserDto> getAllUsers(Pageable pageable);
    void banUser(Long id);
    UserDto getPublicProfile(String username);
    List<UserDto> getUsersByIds(Set<Long> ids);
    UserDto findByEmail(String email);
    UserDto createUser(CreateUserCommand dto);
    UserDto findById(Long userId);
    void verifyPassword(Long id, String rawPassword);
}
