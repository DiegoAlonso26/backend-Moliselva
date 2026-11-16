package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.moliselva.application.auth.gateway.CifradorClaves;
import pe.edu.upeu.moliselva.application.auth.gateway.EmisorToken;
import pe.edu.upeu.moliselva.application.auth.gateway.RolGateway;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;
import pe.edu.upeu.moliselva.application.auth.usecase.ActivarUsuariosDemo;
import pe.edu.upeu.moliselva.application.auth.usecase.ActivarUsuariosDemoUseCase;
import pe.edu.upeu.moliselva.application.auth.usecase.ConsultarRoles;
import pe.edu.upeu.moliselva.application.auth.usecase.ConsultarRolesUseCase;
import pe.edu.upeu.moliselva.application.auth.usecase.ConsultarUsuarios;
import pe.edu.upeu.moliselva.application.auth.usecase.ConsultarUsuariosUseCase;
import pe.edu.upeu.moliselva.application.auth.usecase.GestionarUsuarios;
import pe.edu.upeu.moliselva.application.auth.usecase.GestionarUsuariosUseCase;
import pe.edu.upeu.moliselva.application.auth.usecase.IniciarSesion;
import pe.edu.upeu.moliselva.application.auth.usecase.IniciarSesionUseCase;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;

/** Composition root de autenticación, usuarios y roles. */
@Configuration
public class AuthUseCaseConfig {
    private final Transacciones tx;

    public AuthUseCaseConfig(Transacciones tx) { this.tx = tx; }

    @Bean
    IniciarSesionUseCase iniciarSesion(UsuarioGateway usuarios, CifradorClaves claves, EmisorToken tokens,
            BitacoraGateway bitacora) {
        return tx.envolver(IniciarSesionUseCase.class, new IniciarSesion(usuarios,claves,tokens,bitacora));
    }

    @Bean
    ConsultarUsuariosUseCase consultarUsuarios(UsuarioGateway usuarios) {
        return tx.envolver(ConsultarUsuariosUseCase.class, new ConsultarUsuarios(usuarios));
    }

    @Bean
    GestionarUsuariosUseCase gestionarUsuarios(UsuarioGateway usuarios, CifradorClaves claves, BitacoraGateway bitacora) {
        return tx.envolver(GestionarUsuariosUseCase.class, new GestionarUsuarios(usuarios,claves,bitacora));
    }

    @Bean
    ConsultarRolesUseCase consultarRoles(RolGateway roles) {
        return tx.envolver(ConsultarRolesUseCase.class, new ConsultarRoles(roles));
    }

    @Bean
    ActivarUsuariosDemoUseCase activarUsuariosDemo(UsuarioGateway usuarios, CifradorClaves claves) {
        return tx.envolver(ActivarUsuariosDemoUseCase.class, new ActivarUsuariosDemo(usuarios,claves));
    }
}
