package hexlet.code.service;

import hexlet.code.dto.ListQuery;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskFilter;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.EntityMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;
    private final TaskStatusRepository statuses;
    private final LabelRepository labels;
    private final EntityMapper mapper;
    private final TaskSpecification specifications;

    public List<TaskDTO> list(TaskFilter filter, ListQuery query) {
        var sort =
                query.sort(
                        Map.of(
                                "id",
                                "id",
                                "index",
                                "index",
                                "title",
                                "name",
                                "content",
                                "description",
                                "status",
                                "taskStatus.slug",
                                "assignee_id",
                                "assignee.id",
                                "createdAt",
                                "createdAt"));
        return tasks.findAll(specifications.build(filter, query.ids()), sort).stream()
                .map(mapper::map)
                .toList();
    }

    public TaskDTO get(Long id) {
        return mapper.map(find(id));
    }

    @Transactional
    public TaskDTO create(TaskCreateDTO input) {
        var task = new Task();
        task.setName(input.title());
        task.setDescription(input.content());
        task.setIndex(input.index() == null ? 0 : input.index());
        task.setTaskStatus(findStatus(input.status()));
        task.setAssignee(findAssignee(input.assigneeId()));
        task.setLabels(findLabels(input.taskLabelIds()));
        return mapper.map(tasks.saveAndFlush(task));
    }

    @Transactional
    public TaskDTO update(Long id, TaskUpdateDTO input) {
        var task = find(id);
        input.getTitle().ifPresent(task::setName);
        input.getContent().ifPresent(task::setDescription);
        input.getIndex().ifPresent(value -> task.setIndex(value == null ? 0 : value));
        input.getStatus().ifPresent(slug -> task.setTaskStatus(findStatus(slug)));
        input.getAssigneeId().ifPresent(value -> task.setAssignee(findAssignee(value)));
        input.getTaskLabelIds().ifPresent(value -> task.setLabels(findLabels(value)));
        return mapper.map(tasks.saveAndFlush(task));
    }

    @Transactional
    public void delete(Long id) {
        tasks.delete(find(id));
    }

    private Task find(Long id) {
        return tasks.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена"));
    }

    private TaskStatus findStatus(String slug) {
        return statuses.findBySlug(slug)
                .orElseThrow(() -> new BadRequestException("Статус не найден"));
    }

    private User findAssignee(Long id) {
        if (id == null) {
            return null;
        }
        return users.findById(id)
                .orElseThrow(() -> new BadRequestException("Исполнитель не найден"));
    }

    private Set<Label> findLabels(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        var values = labels.findAllById(ids);
        if (values.size() != ids.size()) {
            throw new BadRequestException("Метка не найдена");
        }
        return new LinkedHashSet<>(values);
    }
}
