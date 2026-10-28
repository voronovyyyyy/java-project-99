package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
        @NotBlank @Email String email,
        String firstName,
        String lastName,
        @NotBlank @Size(min = 3, max = 72) String password) {}
