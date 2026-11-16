package pe.edu.upeu.moliselva.application.auth.usecase;

import pe.edu.upeu.moliselva.application.auth.gateway.CifradorClaves;
import pe.edu.upeu.moliselva.application.auth.gateway.EmisorToken;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

public class IniciarSesion implements IniciarSesionUseCase {
    private final UsuarioGateway usuarios;
    private final CifradorClaves claves;
    private final EmisorToken tokens;
    private final BitacoraGateway bitacora;

    public IniciarSesion(UsuarioGateway usuarios, CifradorClaves claves, EmisorToken tokens, BitacoraGateway bitacora) {
        this.usuarios=usuarios; this.claves=claves; this.tokens=tokens; this.bitacora=bitacora;
    }

    @Override
    public Sesion iniciar(String usuario, String clave) {
        var cuenta = usuarios.buscarPorUsuario(usuario).orElse(null);
        if (cuenta == null || !cuenta.activo() || !claves.coincide(clave,cuenta.hash())) {
            throw new FallaAplicacion(FallaNegocio.Tipo.NO_AUTENTICADO,"CREDENCIALES_INVALIDAS",
                "Usuario o contraseña incorrectos");
        }
        String token = tokens.emitir(cuenta);
        bitacora.registrar(cuenta.id(),"LOGIN usuario="+cuenta.usuario(),null);
        return new Sesion(token,cuenta.publico());
    }
}
