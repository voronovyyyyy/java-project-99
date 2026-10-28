package hexlet.code.dto;

import java.time.LocalDate;

public record UserDTO(
        Long id, String email, String firstName, String lastName, LocalDate createdAt) {}
