package hexlet.code.dto;

import java.time.LocalDate;

public record LabelDTO(Long id, String name, LocalDate createdAt) {}
