package dcc.tp2.security_microservice.Entity;
import dcc.tp2.security_microservice.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setRoles("ADMIN");
        userRepository.save(admin);

        UserEntity user1 = new UserEntity();
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("1234"));
        user1.setRoles("USER");
        userRepository.save(user1);

        UserEntity user2 = new UserEntity();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("1234"));
        user2.setRoles("USER");
        userRepository.save(user2);
    }
}

