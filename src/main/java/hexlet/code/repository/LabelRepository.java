package hexlet.code.repository;

import hexlet.code.model.Label;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByName(String name);

    List<Label> findByIdIn(Collection<Long> ids, Sort sort);
}
