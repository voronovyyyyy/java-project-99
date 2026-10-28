package hexlet.code.dto;

import hexlet.code.exception.BadRequestException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.BindParam;

public record ListQuery(
        @BindParam("_start") @Min(0) Integer start,
        @BindParam("_end") @Min(0) Integer end,
        @BindParam("_sort") String sortBy,
        @BindParam("_order") String order,
        @BindParam("id") List<@Positive Long> ids) {

    public Sort sort(Map<String, String> fields) {
        var name = sortBy == null ? "id" : sortBy;
        var field = fields.get(name);
        if (field == null) {
            throw new BadRequestException("Неизвестное поле сортировки: " + name);
        }
        var direction = order == null ? Sort.Direction.ASC : Sort.Direction.fromString(order);
        return Sort.by(direction, field).and(Sort.by("id"));
    }

    public <T> ResponseEntity<List<T>> response(List<T> values) {
        int from = Math.min(start == null ? 0 : start, values.size());
        int to = Math.min(end == null ? values.size() : end, values.size());
        if (to < from) {
            throw new BadRequestException("Конец диапазона не может быть меньше начала");
        }
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(values.size()))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(values.subList(from, to));
    }
}
