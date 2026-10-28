package hexlet.code.service;

import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccess {
    private final UserRepository users;

    public boolean isOwner(Long id, Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token) {
            var userId = token.getToken().getClaimAsString("userId");
            if (userId != null) {
                return id.toString().equals(userId);
            }
        }
        return users.findByEmail(authentication.getName())
                .map(user -> id.equals(user.getId()))
                .orElse(false);
    }
}
