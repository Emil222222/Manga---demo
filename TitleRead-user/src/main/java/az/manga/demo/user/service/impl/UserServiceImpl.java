package az.manga.demo.user.service.impl;

import az.manga.demo.common.enums.UserRole;
import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.common.exception.UnauthorizedException;
import az.manga.demo.user.api.dto.CreateUserCommand;
import az.manga.demo.user.api.dto.UserDto;
import az.manga.demo.user.mapper.UserMapper;
import az.manga.demo.user.api.UserService;
import az.manga.demo.user.entity.User;
import az.manga.demo.user.dto.PasswordChangeDto;
import az.manga.demo.user.dto.UserUpdateDto;
import az.manga.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserDto getProfile(Long id) {
        log.debug("Fetching profile for user id = {}", id);
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.USER_NOT_FOUND)));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users", key = "'public:' + #result.username")
    })
    public UserDto updateProfile(Long id, UserUpdateDto userDto) {
        log.debug("Updating profile for user id = {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
        userMapper.updateEntity(user,userDto);
        User savedUser = userRepository.save(user);
        log.debug("User id = {} updated successfully", id);
        return userMapper.toDto(savedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void changePassword(Long id, PasswordChangeDto password) {
        log.debug("Changing password for user id = {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password.getCurrentPassword(),user.getPassword())) {
            throw new UnauthorizedException(ErrorMessage.INVALID_CREDENTIALS);
        }
        user.setPassword(passwordEncoder.encode(password.getNewPassword()));
        userRepository.save(user);
        log.debug("Password for user id = {} changed successfully", id);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteProfile(Long id) {
        log.debug("Deleting profile for user id = {}", id);
        userRepository.deleteById(id);
        log.debug("User id = {} deleted successfully", id);
    }

    //ADMIN
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users, page = {}, size = {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void banUser(Long id) {
        log.debug("Baning profile for user id = {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
        user.setIsActive(false);
        userRepository.save(user);
        log.debug("User id = {} deactivated successfully", id);
    }

    @Override
    @Cacheable(value = "users", key = "'public:' + #username")
    public UserDto getPublicProfile(String username) {
        return userMapper.toDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND)));
    }

    @Override
    public List<UserDto> getUsersByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(ids)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    // for UserPrincipal
    @Override
    public UserDto findByEmail(String email) {
        return userMapper.toDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND)));
    }

    @Override
    public UserDto findById(Long userId) {
        return userMapper.toDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND)));
    }

    @Override
    public void verifyPassword(Long id, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new UnauthorizedException(ErrorMessage.INVALID_CREDENTIALS);
        }
    }

    @Override
    public UserDto createUser(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new ConflictException(ErrorMessage.USER_ALREADY_EXISTS);
        }
        return userMapper.toDto(userRepository.save(User.builder()
                .username(command.username())
                .email(command.email())
                .password(passwordEncoder.encode(command.rawPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .build()));
    }
}


