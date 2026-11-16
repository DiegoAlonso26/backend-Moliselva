package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.upeu.moliselva.application.auth.usecase.ActivarUsuariosDemoUseCase;

/** Al arrancar, activa los usuarios demo con DEMO_PASSWORD si aún tienen el marcador. */
@Component
public class UsuariosDemoInicializador implements CommandLineRunner {
    private final ActivarUsuariosDemoUseCase activar;
    private final String demoPassword;

    public UsuariosDemoInicializador(ActivarUsuariosDemoUseCase activar,
            @Value("${app.demo-password}") String demoPassword) {
        this.activar = activar; this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) { activar.activar(demoPassword); }
}
