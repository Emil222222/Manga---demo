package az.manga.demo.user.controller;

import az.manga.demo.common.security.UserPrincipal;
import az.manga.demo.user.api.UserService;
import az.manga.demo.user.api.dto.UserDto;
import az.manga.demo.user.dto.PasswordChangeDto;
import az.manga.demo.user.dto.UserUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDto getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getProfile(principal.getId());
    }

    @GetMapping("/{username}")
    public UserDto getPublicProfile(@PathVariable String username) {
        return userService.getPublicProfile(username);
    }

    @PutMapping("/me")
    public UserDto updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                 @RequestBody @Valid UserUpdateDto userUpdateDto) {
        return userService.updateProfile(principal.getId(), userUpdateDto);
    }

    @PatchMapping("/me/password")
    public void changePassword(@AuthenticationPrincipal UserPrincipal principal,
                               @RequestBody @Valid PasswordChangeDto passwordChangeDto) {
        userService.changePassword(principal.getId(), passwordChangeDto);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteProfile(principal.getId());
    }

    // ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void banUser(@PathVariable Long id) {
        userService.banUser(id);
    }
}
