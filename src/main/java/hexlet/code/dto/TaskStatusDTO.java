package hexlet.code.dto;

import java.time.LocalDate;

public record TaskStatusDTO(Long id, String name, String slug, LocalDate createdAt) {}
