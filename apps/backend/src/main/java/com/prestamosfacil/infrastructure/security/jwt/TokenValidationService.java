package com.prestamosfacil.infrastructure.security.jwt;

import com.prestamosfacil.domain.auth.port.out.TokenParserPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TokenValidationService implements TokenParserPort {
    private final JwtTokenConfig config;
    public TokenValidationService(JwtTokenConfig config) { this.config = config; }

    public Claims parseClaims(String token) {
        return Jwts.parser().clockSkewSeconds(config.getAllowedClockSkewSeconds())
                .verifyWith(config.getKey()).build().parseSignedClaims(token).getPayload();
    }

    @Override
    public Map<String, Object> parse(String token) {
        Claims claims = parseClaims(token);
        Map<String, Object> r = new HashMap<>();
        r.put("sub", claims.getSubject()); r.put("sid", claims.get("sid", String.class));
        r.put("email", claims.get("email", String.class)); r.put("typ", claims.get("typ", String.class));
        r.put("jti", claims.get("jti", String.class));
        r.put("type", claims.get("type", String.class));
        return r;
    }
}
