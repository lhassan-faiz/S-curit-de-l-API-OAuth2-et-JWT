package dcc.tp2.security_microservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class Oauth2_controller {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;

    public Oauth2_controller(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
    }

    // DTO for login request
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // Parameter validation
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("Message", "Username and password are required"));
        }

        try {
            // Verify authentication
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Generate tokens
            Instant instant = Instant.now();
            String scopes = authenticate.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.joining(" "));

            JwtClaimsSet jwtClaimsSetAccessToken = JwtClaimsSet.builder()
                    .issuer("MS_sec")
                    .subject(authenticate.getName())
                    .issuedAt(instant)
                    .expiresAt(instant.plus(2, ChronoUnit.MINUTES))
                    .claim("name", authenticate.getName())
                    .claim("SCOPE", scopes)
                    .build();

            // Sign the token
            String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSetAccessToken)).getTokenValue();

            // Generate refresh token
            JwtClaimsSet jwtClaimsSetRefreshToken = JwtClaimsSet.builder()
                    .issuer("MS_sec")
                    .subject(authenticate.getName())
                    .issuedAt(instant)
                    .expiresAt(instant.plus(15, ChronoUnit.MINUTES))
                    .claim("name", authenticate.getName())
                    .build();

            String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSetRefreshToken)).getTokenValue();

            Map<String, String> idToken = new HashMap<>();
            idToken.put("Access_Token", accessToken);
            idToken.put("Refresh_Token", refreshToken);

            return ResponseEntity.ok(idToken);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Message", "Invalid username or password"));
        }
    }

    @PostMapping("/RefreshToken")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("Message", "Refresh_Token is required"));
        }

        try {
            // Verify the signature
            Jwt decoded = jwtDecoder.decode(refreshToken);
            String username = decoded.getSubject();

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Renew access token
            Instant instant = Instant.now();
            String scopes = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.joining(" "));

            JwtClaimsSet jwtClaimsSetAccessToken = JwtClaimsSet.builder()
                    .issuer("MS_sec")
                    .subject(userDetails.getUsername())
                    .issuedAt(instant)
                    .expiresAt(instant.plus(2, ChronoUnit.MINUTES))
                    .claim("name", userDetails.getUsername())
                    .claim("SCOPE", scopes)
                    .build();

            String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSetAccessToken)).getTokenValue();

            Map<String, String> idToken = new HashMap<>();
            idToken.put("Access_Token", accessToken);
            idToken.put("Refresh_Token", refreshToken);

            return ResponseEntity.ok(idToken);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Message", "Invalid refresh token"));
        }
    }
}
