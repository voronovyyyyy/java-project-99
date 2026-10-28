package hexlet.code.specification;

import hexlet.code.dto.TaskFilter;
import hexlet.code.model.Task;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(TaskFilter filter, List<Long> ids) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.titleCont() != null && !filter.titleCont().isEmpty()) {
                var value =
                        filter.titleCont()
                                .toLowerCase(java.util.Locale.ROOT)
                                .replace("\\", "\\\\")
                                .replace("%", "\\%")
                                .replace("_", "\\_");
                predicates.add(
                        builder.like(builder.lower(root.get("name")), "%" + value + "%", '\\'));
            }
            if (filter.assigneeId() != null) {
                predicates.add(builder.equal(root.get("assignee").get("id"), filter.assigneeId()));
            }
            if (filter.status() != null && !filter.status().isBlank()) {
                predicates.add(builder.equal(root.get("taskStatus").get("slug"), filter.status()));
            }
            if (filter.labelId() != null) {
                predicates.add(builder.equal(root.join("labels").get("id"), filter.labelId()));
            }
            if (ids != null) {
                predicates.add(root.get("id").in(ids));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
