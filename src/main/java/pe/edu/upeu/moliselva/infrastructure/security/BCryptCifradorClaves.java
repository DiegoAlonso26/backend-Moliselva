package pe.edu.upeu.moliselva.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.upeu.moliselva.application.auth.gateway.CifradorClaves;

@Component
public class BCryptCifradorClaves implements CifradorClaves {
    private final PasswordEncoder passwords;

    public BCryptCifradorClaves(PasswordEncoder passwords) { this.passwords = passwords; }

    @Override public String cifrar(String clave) { return passwords.encode(clave); }

    @Override public boolean coincide(String clave, String hash) { return passwords.matches(clave,hash); }
}
