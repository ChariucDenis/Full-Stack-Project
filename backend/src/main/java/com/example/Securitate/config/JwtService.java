package com.example.Securitate.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// NOTE: Ajustează importul User dacă pachetul tău e diferit!
import com.example.Securitate.User.User;


@Service
public class JwtService {

    /**
     * Cheia trebuie să fie Base64. Poți seta în application.properties:
     * application.security.jwt.secret-key=<cheie_base64>
     * application.security.jwt.expiration-ms=900000  (15 minute)
     *
     * Dacă nu setezi proprietățile, se folosește fallback-ul de mai jos.
     */
    @Value("${application.security.jwt.secret-key:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${application.security.jwt.expiration-ms:900000}")
    private long jwtExpirationMs;

    // ------------ Extracție claims / validare ------------
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        // secretKey trebuie să fie Base64. Dacă folosești hex, convertește-l sau setează corect proprietatea.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ------------ Generare token ------------
    /**
     * Preferat: generează token din User complet (include extra claims)
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Claims utile pentru frontend:
        claims.put("email", user.getEmail());
        claims.put("firstname", user.getFirstname());
        claims.put("lastname", user.getLastname());
        claims.put("role", user.getRole().name());
        // Compat cu frontul: authorities conțin ROLE_*
        claims.put("authorities", List.of("ROLE_" + user.getRole().name()));

        // sub (subject) = email
        return buildToken(claims, user.getEmail());
    }

    /**
     * Overload folosit adesea în fluxul Spring Security.
     * Dacă poți obține User-ul concret, folosește generateToken(User).
     */
    public String generateToken(UserDetails userDetails) {
        if (userDetails instanceof User u) {
            return generateToken(u);
        }
        // Fallback minimal dacă nu avem un User concret (doar subject-ul)
        return buildToken(new HashMap<>(), userDetails.getUsername());
    }

    /**
     * Construiește efectiv JWT-ul
     */
    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
