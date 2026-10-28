package hexlet.code.mapper;

import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {
    public UserDTO map(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt().toLocalDate());
    }

    public TaskStatusDTO map(TaskStatus status) {
        return new TaskStatusDTO(
                status.getId(),
                status.getName(),
                status.getSlug(),
                status.getCreatedAt().toLocalDate());
    }

    public LabelDTO map(Label label) {
        return new LabelDTO(label.getId(), label.getName(), label.getCreatedAt().toLocalDate());
    }

    public TaskDTO map(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getIndex(),
                task.getCreatedAt().toLocalDate(),
                task.getAssignee() == null ? null : task.getAssignee().getId(),
                task.getName(),
                task.getDescription(),
                task.getTaskStatus().getSlug(),
                task.getLabels().stream().map(Label::getId).sorted().toList());
    }
}
