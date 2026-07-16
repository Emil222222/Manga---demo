package az.manga.demo.user.api.dto;

public record CreateUserCommand(
        String username,
        String email,
        String rawPassword
) {}
