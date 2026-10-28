package hexlet.code.service;

import hexlet.code.dto.ListQuery;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.EntityMapper;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    private final TaskRepository tasks;
    private final PasswordEncoder encoder;
    private final EntityMapper mapper;

    public List<UserDTO> list(ListQuery query) {
        var sort =
                query.sort(
                        Map.of(
                                "id",
                                "id",
                                "email",
                                "email",
                                "firstName",
                                "firstName",
                                "lastName",
                                "lastName",
                                "createdAt",
                                "createdAt"));
        var values =
                query.ids() == null ? users.findAll(sort) : users.findByIdIn(query.ids(), sort);
        return values.stream().map(mapper::map).toList();
    }

    public UserDTO get(Long id) {
        return mapper.map(find(id));
    }

    @Transactional
    public UserDTO create(UserCreateDTO input) {
        var user = new User();
        user.setEmail(uniqueEmail(input.email(), null));
        user.setFirstName(input.firstName());
        user.setLastName(input.lastName());
        user.setPassword(encoder.encode(input.password()));
        return mapper.map(users.saveAndFlush(user));
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO input) {
        var user = find(id);
        input.getEmail().ifPresent(email -> user.setEmail(uniqueEmail(email, id)));
        input.getFirstName().ifPresent(user::setFirstName);
        input.getLastName().ifPresent(user::setLastName);
        input.getPassword().ifPresent(password -> user.setPassword(encoder.encode(password)));
        return mapper.map(users.saveAndFlush(user));
    }

    @Transactional
    public void delete(Long id) {
        var user = find(id);
        if (tasks.existsByAssigneeId(id)) {
            throw new BadRequestException("Нельзя удалить пользователя, связанного с задачами");
        }
        users.delete(user);
    }

    private User find(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    private String uniqueEmail(String email, Long currentId) {
        var normalized = email.trim().toLowerCase(Locale.ROOT);
        users.findByEmail(normalized)
                .filter(user -> !Objects.equals(user.getId(), currentId))
                .ifPresent(
                        user -> {
                            throw new BadRequestException(
                                    "Пользователь с таким email уже существует");
                        });
        return normalized;
    }
}
