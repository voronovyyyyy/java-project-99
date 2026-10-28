package hexlet.code.service;

import hexlet.code.model.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtEncoder encoder;

    public String generate(User user) {
        var now = Instant.now();
        var claims =
                JwtClaimsSet.builder()
                        .issuer("task-manager")
                        .subject(user.getEmail())
                        .claim("userId", user.getId())
                        .issuedAt(now)
                        .expiresAt(now.plus(1, ChronoUnit.DAYS))
                        .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
