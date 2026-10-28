package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<@NotBlank String> title = JsonNullable.undefined();
    private JsonNullable<String> content = JsonNullable.undefined();
    private JsonNullable<Integer> index = JsonNullable.undefined();
    private JsonNullable<@NotBlank String> status = JsonNullable.undefined();

    @JsonProperty("assignee_id")
    private JsonNullable<@Positive Long> assigneeId = JsonNullable.undefined();

    private JsonNullable<Set<@NotNull @Positive Long>> taskLabelIds = JsonNullable.undefined();
}
