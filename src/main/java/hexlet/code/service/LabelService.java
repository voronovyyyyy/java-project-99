package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.dto.ListQuery;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.EntityMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabelService {
    private final LabelRepository labels;
    private final TaskRepository tasks;
    private final EntityMapper mapper;

    public List<LabelDTO> list(ListQuery query) {
        var sort = query.sort(Map.of("id", "id", "name", "name", "createdAt", "createdAt"));
        var values =
                query.ids() == null ? labels.findAll(sort) : labels.findByIdIn(query.ids(), sort);
        return values.stream().map(mapper::map).toList();
    }

    public LabelDTO get(Long id) {
        return mapper.map(find(id));
    }

    @Transactional
    public LabelDTO create(LabelCreateDTO input) {
        ensureUnique(input.name(), null);
        var label = new Label();
        label.setName(input.name());
        return mapper.map(labels.saveAndFlush(label));
    }

    @Transactional
    public LabelDTO update(Long id, LabelUpdateDTO input) {
        var label = find(id);
        input.getName()
                .ifPresent(
                        name -> {
                            ensureUnique(name, id);
                            label.setName(name);
                        });
        return mapper.map(labels.saveAndFlush(label));
    }

    @Transactional
    public void delete(Long id) {
        var label = find(id);
        if (tasks.existsByLabelsId(id)) {
            throw new BadRequestException("Нельзя удалить метку, связанную с задачами");
        }
        labels.delete(label);
    }

    private Label find(Long id) {
        return labels.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Метка не найдена"));
    }

    private void ensureUnique(String name, Long currentId) {
        if (labels.findByName(name)
                .filter(label -> !Objects.equals(label.getId(), currentId))
                .isPresent()) {
            throw new BadRequestException("Название метки должно быть уникальным");
        }
    }
}
