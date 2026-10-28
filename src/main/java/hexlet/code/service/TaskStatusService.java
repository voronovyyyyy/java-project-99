package hexlet.code.service;

import hexlet.code.dto.ListQuery;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.EntityMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskStatusService {
    private final TaskStatusRepository statuses;
    private final TaskRepository tasks;
    private final EntityMapper mapper;

    public List<TaskStatusDTO> list(ListQuery query) {
        var sort =
                query.sort(
                        Map.of(
                                "id",
                                "id",
                                "name",
                                "name",
                                "slug",
                                "slug",
                                "createdAt",
                                "createdAt"));
        var values =
                query.ids() == null
                        ? statuses.findAll(sort)
                        : statuses.findByIdIn(query.ids(), sort);
        return values.stream().map(mapper::map).toList();
    }

    public TaskStatusDTO get(Long id) {
        return mapper.map(find(id));
    }

    @Transactional
    public TaskStatusDTO create(TaskStatusCreateDTO input) {
        ensureUnique(input.name(), input.slug(), null);
        var status = new TaskStatus();
        status.setName(input.name());
        status.setSlug(input.slug());
        return mapper.map(statuses.saveAndFlush(status));
    }

    @Transactional
    public TaskStatusDTO update(Long id, TaskStatusUpdateDTO input) {
        var status = find(id);
        var name = input.getName().orElse(status.getName());
        var slug = input.getSlug().orElse(status.getSlug());
        ensureUnique(name, slug, id);
        status.setName(name);
        status.setSlug(slug);
        return mapper.map(statuses.saveAndFlush(status));
    }

    @Transactional
    public void delete(Long id) {
        var status = find(id);
        if (tasks.existsByTaskStatusId(id)) {
            throw new BadRequestException("Нельзя удалить статус, связанный с задачами");
        }
        statuses.delete(status);
    }

    private TaskStatus find(Long id) {
        return statuses.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Статус не найден"));
    }

    private void ensureUnique(String name, String slug, Long currentId) {
        if (statuses.findByName(name)
                        .filter(status -> !Objects.equals(status.getId(), currentId))
                        .isPresent()
                || statuses.findBySlug(slug)
                        .filter(status -> !Objects.equals(status.getId(), currentId))
                        .isPresent()) {
            throw new BadRequestException("Название и slug статуса должны быть уникальны");
        }
    }
}
