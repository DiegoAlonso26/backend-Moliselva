package pe.edu.upeu.moliselva.application.auth.usecase;

import pe.edu.upeu.moliselva.application.auth.gateway.CifradorClaves;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;

public class ActivarUsuariosDemo implements ActivarUsuariosDemoUseCase {
    private final UsuarioGateway usuarios;
    private final CifradorClaves claves;

    public ActivarUsuariosDemo(UsuarioGateway usuarios, CifradorClaves claves) {
        this.usuarios=usuarios; this.claves=claves;
    }

    @Override
    public void activar(String claveDemo) {
        if (usuarios.contarClavesDemoPendientes() == 0) return;
        if (claveDemo.length() < 12) {
            throw new IllegalStateException("DEMO_PASSWORD requiere al menos 12 caracteres para activar usuarios demo");
        }
        usuarios.asignarClaveDemo(claves.cifrar(claveDemo));
    }
}
