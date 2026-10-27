package pe.edu.upeu.moliselva.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoPasswordInitializer implements CommandLineRunner {
    private final AuthRepository users;
    private final PasswordEncoder passwords;
    private final String demoPassword;

    public DemoPasswordInitializer(AuthRepository users, PasswordEncoder passwords,
            @Value("${app.demo-password}") String demoPassword) {
        this.users = users; this.passwords = passwords; this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) {
        if (users.countPlaceholderHashes() == 0) return;
        if (demoPassword.length() < 12) {
            throw new IllegalStateException("DEMO_PASSWORD requiere al menos 12 caracteres para activar usuarios demo");
        }
        users.replacePlaceholderHashes(passwords.encode(demoPassword));
    }
}
