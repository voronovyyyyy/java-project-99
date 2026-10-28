package hexlet.code.repository;

import hexlet.code.model.TaskStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);

    Optional<TaskStatus> findByName(String name);

    List<TaskStatus> findByIdIn(Collection<Long> ids, Sort sort);
}
