package hexlet.code.controller;

import hexlet.code.dto.AuthRequest;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final TokenService tokens;

    @PostMapping("/api/login")
    @SecurityRequirements
    public String login(@RequestBody AuthRequest request) {
        if (request.username() == null || request.password() == null) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }
        var authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(), request.password()));
        var user =
                users.findByEmail(authentication.getName())
                        .orElseThrow(
                                () -> new BadCredentialsException("Неверный логин или пароль"));
        return tokens.generate(user);
    }
}
