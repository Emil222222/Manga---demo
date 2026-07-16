package az.manga.demo.user.service;

import az.manga.demo.common.enums.UserRole;
import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.common.exception.UnauthorizedException;
import az.manga.demo.user.api.dto.CreateUserCommand;
import az.manga.demo.user.api.dto.UserDto;
import az.manga.demo.user.dto.PasswordChangeDto;
import az.manga.demo.user.dto.UserUpdateDto;
import az.manga.demo.user.entity.User;
import az.manga.demo.user.mapper.UserMapper;
import az.manga.demo.user.repository.UserRepository;
import az.manga.demo.user.service.impl.UserServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .username("testuser")
                .email("test@mail.com")
                .password("encoded-pass")
                .role(UserRole.USER)
                .isActive(true)
                .build();
    }

    private UserDto buildUserDto(Long id) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUsername("testuser");
        dto.setEmail("test@mail.com");
        dto.setIsActive(true);
        dto.setRole(UserRole.USER);
        return dto;
    }

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("возвращает профиль пользователя")
        void shouldReturnProfile() {
            User user = buildUser(1L);
            UserDto expectedDto = buildUserDto(1L);

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Mockito.when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserDto result = userService.getProfile(1L);

            assertThat(result.getId()).isEqualTo(1L);
            Mockito.verify(userRepository).findById(1L);
            Mockito.verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если пользователь не найден")
        void shouldThrowNotFoundIfUserNotFound() {
            Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.getProfile(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("обновляет профиль пользователя")
        void shouldUpdateProfile() {
            User user = buildUser(1L);
            UserUpdateDto updateDto = new UserUpdateDto();
            UserDto expectedDto = buildUserDto(1L);

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Mockito.doNothing().when(userMapper).updateEntity(user, updateDto);
            Mockito.when(userRepository.save(user)).thenReturn(user);
            Mockito.when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserDto result = userService.updateProfile(1L, updateDto);

            assertThat(result).isEqualTo(expectedDto);

            InOrder inOrder = Mockito.inOrder(userRepository, userMapper);
            inOrder.verify(userRepository).findById(1L);
            inOrder.verify(userMapper).updateEntity(user, updateDto);
            inOrder.verify(userRepository).save(user);
            inOrder.verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если пользователь не найден")
        void shouldThrowNotFoundIfUserNotFound() {
            Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.updateProfile(99L, new UserUpdateDto()))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("меняет пароль успешно")
        void shouldChangePassword() {
            User user = buildUser(1L);
            PasswordChangeDto dto = new PasswordChangeDto();
            dto.setCurrentPassword("old-pass");
            dto.setNewPassword("new-pass");
            dto.setConfirmNewPassword("new-pass");

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Mockito.when(passwordEncoder.matches("old-pass", "encoded-pass")).thenReturn(true);
            Mockito.when(passwordEncoder.encode("new-pass")).thenReturn("new-encoded-pass");

            userService.changePassword(1L, dto);

            assertThat(user.getPassword()).isEqualTo("new-encoded-pass");
            Mockito.verify(userRepository).save(user);
        }

        @Test
        @DisplayName("выбрасывает UnauthorizedException если текущий пароль неверный")
        void shouldThrowUnauthorizedIfWrongCurrentPassword() {
            User user = buildUser(1L);
            PasswordChangeDto dto = new PasswordChangeDto();
            dto.setCurrentPassword("wrong-pass");
            dto.setNewPassword("new-pass");
            dto.setConfirmNewPassword("new-pass");

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Mockito.when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

            Assertions.assertThatThrownBy(() -> userService.changePassword(1L, dto))
                    .isInstanceOf(UnauthorizedException.class);

            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any());
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если пользователь не найден")
        void shouldThrowNotFoundIfUserDoesNotExist() {
            Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.changePassword(99L, new PasswordChangeDto()))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(passwordEncoder);
            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("deleteProfile()")
    class DeleteProfile {

        @Test
        @DisplayName("удаляет профиль пользователя")
        void shouldDeleteProfile() {
            User user = buildUser(1L);
            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.deleteProfile(1L);

            Mockito.verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если пользователь не найден")
        void shouldThrowNotFoundIfUserNotFound() {
            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.deleteProfile(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(userRepository, Mockito.never()).delete(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("banUser()")
    class BanUser {

        @Test
        @DisplayName("деактивирует пользователя")
        void shouldBanUser() {
            User user = buildUser(1L);

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.banUser(1L);

            assertThat(user.getIsActive()).isFalse();
            Mockito.verify(userRepository).save(user);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если пользователь не найден")
        void shouldThrowNotFoundIfUserNotFound() {
            Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.banUser(99L))
                    .isInstanceOf(NotFoundException.class);
            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any());
        }
    }

    @Nested
    @DisplayName("getPublicProfile()")
    class GetPublicProfile {

        @Test
        @DisplayName("возвращает публичный профиль по username")
        void shouldReturnPublicProfile() {
            User user = buildUser(1L);
            UserDto expectedDto = buildUserDto(1L);

            Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            Mockito.when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserDto result = userService.getPublicProfile("testuser");

            assertThat(result).isEqualTo(expectedDto);
            Mockito.verify(userRepository).findByUsername("testuser");
            Mockito.verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если username не найден")
        void shouldThrowNotFoundIfUsernameNotFound() {
            Mockito.when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.getPublicProfile("unknown"))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("возвращает страницу пользователей")
        void shouldReturnPageOfUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            User user = buildUser(1L);
            UserDto userDto = buildUserDto(1L);

            Mockito.when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));
            Mockito.when(userMapper.toDto(user)).thenReturn(userDto);

            Page<UserDto> result = userService.getAllUsers(pageable);

            Assertions.assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(userDto);
            Mockito.verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("возвращает пустую страницу если пользователей нет")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);

            Mockito.when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            Page<UserDto> result = userService.getAllUsers(pageable);

            Assertions.assertThat(result.getContent()).isEmpty();
            Mockito.verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("getUsersByIds()")
    class GetUsersByIds {

        @Test
        @DisplayName("возвращает список пользователей по ids")
        void shouldReturnUsersByIds() {
            Set<Long> ids = Set.of(1L, 2L);
            User u1 = buildUser(1L);
            User u2 = buildUser(2L);
            UserDto dto1 = buildUserDto(1L);
            UserDto dto2 = buildUserDto(2L);

            Mockito.when(userRepository.findAllById(ids)).thenReturn(List.of(u1, u2));
            Mockito.when(userMapper.toDto(u1)).thenReturn(dto1);
            Mockito.when(userMapper.toDto(u2)).thenReturn(dto2);

            List<UserDto> result = userService.getUsersByIds(ids);

            Assertions.assertThat(result).hasSize(2);
            Assertions.assertThat(result).containsExactlyInAnyOrder(dto1, dto2);
            Mockito.verify(userMapper).toDto(u1);
            Mockito.verify(userMapper).toDto(u2);
        }

        @Test
        @DisplayName("возвращает пустой список если ids пустой")
        void shouldReturnEmptyListIfIdsEmpty() {
            Mockito.when(userRepository.findAllById(Set.of())).thenReturn(List.of());

            List<UserDto> result = userService.getUsersByIds(Set.of());

            Assertions.assertThat(result).isEmpty();
            Mockito.verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("возвращает пользователя по email")
        void shouldReturnUserByEmail() {
            User user = buildUser(1L);
            UserDto expectedDto = buildUserDto(1L);

            Mockito.when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
            Mockito.when(userMapper.toDto(user)).thenReturn(expectedDto);

            UserDto result = userService.findByEmail("test@mail.com");

            assertThat(result).isEqualTo(expectedDto);
            Mockito.verify(userRepository).findByEmail("test@mail.com");
            Mockito.verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если email не найден")
        void shouldThrowNotFoundIfEmailNotFound() {
            Mockito.when(userRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> userService.findByEmail("unknown@mail.com"))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("verifyPassword()")
    class VerifyPassword {

        @Test
        @DisplayName("не выбрасывает исключение если пароль верный")
        void shouldNotThrowIfPasswordMatches() {
            User user = buildUser(1L);

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Mockito.when(passwordEncoder.matches("raw-pass", "encoded-pass")).thenReturn(true);

            userService.verifyPassword(1L, "raw-pass"); // не должно упасть

            Assertions.assertThatNoException().isThrownBy(() -> userService.verifyPassword(1L, "raw-pass"));
        }

        @Test
        @DisplayName("выбрасывает UnauthorizedException если пароль неверный")
        void shouldThrowUnauthorizedIfPasswordDoesNotMatch() {
            User user = buildUser(1L);

            Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            Mockito.when(passwordEncoder.matches("wrong-pass", "encoded-pass")).thenReturn(false);

            Assertions.assertThatThrownBy(() -> userService.verifyPassword(1L, "wrong-pass"))
                    .isInstanceOf(UnauthorizedException.class);

            Mockito.verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("создаёт пользователя и возвращает UserDto")
        void shouldCreateUser() {
            CreateUserCommand command = new CreateUserCommand("testuser", "test@mail.com", "pass123");


            User saved = buildUser(1L);
            UserDto expectedDto = buildUserDto(1L);

            Mockito.when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
            Mockito.when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");
            Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(saved);
            Mockito.when(userMapper.toDto(saved)).thenReturn(expectedDto);

            UserDto result = userService.createUser(command);

            assertThat(result).isEqualTo(expectedDto);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            Mockito.verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("test@mail.com");
            assertThat(savedUser.getPassword()).isEqualTo("encoded-pass");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getIsActive()).isTrue();

            Mockito.verify(passwordEncoder).encode("pass123");
            Mockito.verify(userMapper).toDto(saved);
        }

        @Test
        @DisplayName("выбрасывает ConflictException если email уже существует")
        void shouldThrowConflictIfEmailExists() {
            CreateUserCommand command = new CreateUserCommand(null, "test@mail.com", null);


            Mockito.when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

            Assertions.assertThatThrownBy(() -> userService.createUser(command))
                    .isInstanceOf(ConflictException.class);

            Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any());
        }
    }
}
