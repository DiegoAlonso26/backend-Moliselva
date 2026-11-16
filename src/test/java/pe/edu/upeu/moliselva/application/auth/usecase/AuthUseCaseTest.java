package pe.edu.upeu.moliselva.application.auth.usecase;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.auth.gateway.CifradorClaves;
import pe.edu.upeu.moliselva.application.auth.gateway.EmisorToken;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;
import pe.edu.upeu.moliselva.domain.auth.UsuarioDatos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Material ficticio: ninguna clave procede del .env ni de usuarios reales. */
class AuthUseCaseTest {
    private final UsuarioGateway usuarios = mock(UsuarioGateway.class);
    private final CifradorClaves claves = mock(CifradorClaves.class);
    private final EmisorToken tokens = mock(EmisorToken.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);
    private final CuentaUsuario operario = new CuentaUsuario(3,"Operario","operario_x","hash-ficticio","OPERARIO",true);

    @Test
    void loginCorrectoEmiteTokenYRegistraBitacora() {
        when(usuarios.buscarPorUsuario("operario_x")).thenReturn(Optional.of(operario));
        when(claves.coincide("clave-ficticia","hash-ficticio")).thenReturn(true);
        when(tokens.emitir(operario)).thenReturn("token-ficticio");

        var sesion = new IniciarSesion(usuarios,claves,tokens,bitacora).iniciar("operario_x","clave-ficticia");

        assertEquals("token-ficticio",sesion.token());
        assertEquals("OPERARIO",sesion.usuario().rol());
        verify(bitacora).registrar(3,"LOGIN usuario=operario_x",null);
    }

    @Test
    void loginConClaveIncorrectaUsuarioInactivoOInexistenteDevuelve401SinToken() {
        var login = new IniciarSesion(usuarios,claves,tokens,bitacora);
        when(usuarios.buscarPorUsuario("operario_x")).thenReturn(Optional.of(operario));
        when(usuarios.buscarPorUsuario("inactivo")).thenReturn(Optional.of(
            new CuentaUsuario(4,"I","inactivo","hash-ficticio","OPERARIO",false)));
        when(claves.coincide(anyString(),anyString())).thenReturn(true);
        when(claves.coincide("mala","hash-ficticio")).thenReturn(false);

        for (String[] caso : new String[][]{{"operario_x","mala"},{"inactivo","clave"},{"nadie","clave"}}) {
            var error = assertThrows(FallaNegocio.class, () -> login.iniciar(caso[0],caso[1]));
            assertEquals("CREDENCIALES_INVALIDAS",error.codigo());
            assertEquals(FallaNegocio.Tipo.NO_AUTENTICADO,error.tipo());
        }
        verifyNoInteractions(tokens,bitacora);
    }

    @Test
    void administradorNoPuedeQuitarseSuRolNiDesactivarse() {
        var gestionar = new GestionarUsuarios(usuarios,claves,bitacora);
        when(usuarios.existeRol("OPERARIO")).thenReturn(true);

        var quitarRol = assertThrows(FallaNegocio.class, () ->
            gestionar.actualizar(1,new UsuarioDatos("Admin","admin","OPERARIO"),null,1));
        var desactivarse = assertThrows(FallaNegocio.class, () -> gestionar.desactivar(1,1));

        assertEquals("ULTIMO_ADMINISTRADOR",quitarRol.codigo());
        assertEquals("ULTIMO_ADMINISTRADOR",desactivarse.codigo());
        verify(usuarios,never()).actualizar(anyLong(),any(),any());
        verify(usuarios,never()).desactivar(anyLong());
        verifyNoInteractions(bitacora);
    }

    @Test
    void rolDesconocidoSeRechazaAntesDeCifrarLaClave() {
        when(usuarios.existeRol("GERENTE")).thenReturn(false);

        var error = assertThrows(FallaNegocio.class, () -> new GestionarUsuarios(usuarios,claves,bitacora)
            .crear(new UsuarioDatos("N","u","GERENTE"),"clave-ficticia-larga",1));

        assertEquals("ROL_INVALIDO",error.codigo());
        verifyNoInteractions(claves,bitacora);
    }

    @Test
    void crearNormalizaYActualizarSinClaveConservaLaActual() {
        var gestionar = new GestionarUsuarios(usuarios,claves,bitacora);
        when(usuarios.existeRol("OPERARIO")).thenReturn(true);
        when(claves.cifrar("clave-ficticia-larga")).thenReturn("hash-nuevo");
        when(usuarios.crear(new UsuarioDatos("Ana","ana","OPERARIO"),"hash-nuevo")).thenReturn(9L);

        var creado = gestionar.crear(new UsuarioDatos("  Ana ","  ana ","OPERARIO"),"clave-ficticia-larga",1);
        assertEquals("Ana",creado.nombres());
        assertEquals("ana",creado.usuario());

        when(usuarios.actualizar(9,new UsuarioDatos("Ana","ana","OPERARIO"),null)).thenReturn(true);
        when(usuarios.buscarPorId(9)).thenReturn(Optional.of(new CuentaUsuario(9,"Ana","ana","h","OPERARIO",true)));
        gestionar.actualizar(9,new UsuarioDatos("Ana","ana","OPERARIO"),null,1);
        verify(usuarios).actualizar(9,new UsuarioDatos("Ana","ana","OPERARIO"),null);
        verify(claves,times(1)).cifrar(anyString());
    }

    @Test
    void claveDemoCortaNoActivaUsuariosPendientes() {
        when(usuarios.contarClavesDemoPendientes()).thenReturn(4);

        assertThrows(IllegalStateException.class, () -> new ActivarUsuariosDemo(usuarios,claves).activar("corta"));
        verify(usuarios,never()).asignarClaveDemo(anyString());
    }
}
