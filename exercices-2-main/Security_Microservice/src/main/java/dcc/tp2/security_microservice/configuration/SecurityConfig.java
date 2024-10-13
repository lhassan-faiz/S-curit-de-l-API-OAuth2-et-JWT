package dcc.tp2.security_microservice.configuration;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final RsaConfig rsaConfig;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(PasswordEncoder passwordEncoder, RsaConfig rsaConfig, UserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.rsaConfig = rsaConfig;
        this.userDetailsService = userDetailsService; // Use UserService for user details
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService); // Use UserService
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(auth -> auth
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/RefreshToken/**").permitAll()

                        // Autoriser l'accès à la gestion des comptes uniquement pour les ADMIN
                        .requestMatchers(HttpMethod.POST, "/comptes").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/comptes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/comptes/**").hasRole("ADMIN")

                        // Autoriser l'accès à la récupération des comptes pour tout utilisateur authentifié
                        .requestMatchers(HttpMethod.GET, "/comptes/{id}").authenticated()

                        // Autoriser l'accès au crédit et au débit pour tout utilisateur authentifié
                        .requestMatchers(HttpMethod.POST, "/comptes/{id}/crediter/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comptes/{id}/debiter/**").authenticated()

                        // Autoriser la récupération de tous les comptes uniquement pour les ADMIN
                        .requestMatchers(HttpMethod.GET, "/comptes").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .httpBasic(Customizer.withDefaults())
                .build();
    }


    // Signed JWT token
    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaConfig.publicKey()).privateKey(rsaConfig.privateKey()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaConfig.publicKey()).build();
    }
}
