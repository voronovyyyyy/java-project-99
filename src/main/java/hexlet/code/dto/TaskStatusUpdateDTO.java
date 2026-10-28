package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {
    private JsonNullable<@NotBlank String> name = JsonNullable.undefined();
    private JsonNullable<@NotBlank String> slug = JsonNullable.undefined();
}
