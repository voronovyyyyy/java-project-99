package hexlet.code.controller;

import hexlet.code.dto.ListQuery;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService users;

    @GetMapping
    public ResponseEntity<List<UserDTO>> index(
            @Valid @ModelAttribute @ParameterObject ListQuery query) {
        return query.response(users.list(query));
    }

    @GetMapping("/{id}")
    public UserDTO show(@PathVariable Long id) {
        return users.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO input) {
        return users.create(input);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userAccess.isOwner(#id, authentication)")
    public UserDTO update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO input) {
        return users.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@userAccess.isOwner(#id, authentication)")
    public void delete(@PathVariable Long id) {
        users.delete(id);
    }
}
