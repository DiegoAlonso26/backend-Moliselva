package pe.edu.upeu.moliselva.application.auth.usecase;

import pe.edu.upeu.moliselva.application.auth.gateway.CifradorClaves;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.Permisos;
import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;
import pe.edu.upeu.moliselva.domain.auth.Usuario;
import pe.edu.upeu.moliselva.domain.auth.UsuarioDatos;

public class GestionarUsuarios implements GestionarUsuariosUseCase {
    private final UsuarioGateway usuarios;
    private final CifradorClaves claves;
    private final BitacoraGateway bitacora;

    public GestionarUsuarios(UsuarioGateway usuarios, CifradorClaves claves, BitacoraGateway bitacora) {
        this.usuarios=usuarios; this.claves=claves; this.bitacora=bitacora;
    }

    @Override
    public Usuario crear(UsuarioDatos datos, String clave, long adminId) {
        if (!usuarios.existeRol(datos.rol())) { throw UsuarioFallas.rolInvalido(); }
        long id = usuarios.crear(datos,claves.cifrar(clave));
        bitacora.registrar(adminId,"CREAR_USUARIO id="+id,null);
        return new Usuario(id,datos.nombres(),datos.usuario(),datos.rol(),true);
    }

    @Override
    public Usuario actualizar(long id, UsuarioDatos datos, String clave, long adminId) {
        if (!usuarios.existeRol(datos.rol())) { throw UsuarioFallas.rolInvalido(); }
        if (id == adminId && !Permisos.ADMINISTRADOR.equals(datos.rol())) {
            throw UsuarioFallas.ultimoAdministrador("No puede quitarse su propio rol de administrador");
        }
        String hash = clave == null ? null : claves.cifrar(clave);
        if (!usuarios.actualizar(id,datos,hash)) { throw UsuarioFallas.noEncontrado(); }
        bitacora.registrar(adminId,"ACTUALIZAR_USUARIO id="+id,null);
        return usuarios.buscarPorId(id).map(CuentaUsuario::publico).orElseThrow(UsuarioFallas::noEncontrado);
    }

    @Override
    public void desactivar(long id, long adminId) {
        if (id == adminId) {
            throw UsuarioFallas.ultimoAdministrador("No puede desactivar su propia cuenta");
        }
        if (!usuarios.desactivar(id)) { throw UsuarioFallas.noEncontrado(); }
        bitacora.registrar(adminId,"DESACTIVAR_USUARIO id="+id,null);
    }
}
