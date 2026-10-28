package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskStatusCreateDTO(@NotBlank String name, @NotBlank String slug) {}
