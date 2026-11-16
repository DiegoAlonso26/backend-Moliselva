package pe.edu.upeu.moliselva.application.proveedor.usecase;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.upeu.moliselva.application.proveedor.gateway.ProveedorGateway;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProveedorUseCaseTest {
    private final ProveedorGateway proveedores = mock(ProveedorGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);
    private final GestionarProveedor gestionar = new GestionarProveedor(proveedores,bitacora);
    private final Actor jefe = new Actor(2,"jefe","JEFE_PLANTA");

    @Test
    void datosSeNormalizanAntesDeGuardar() {
        var datos = new ProveedorDatos("  Proveedor SAC  ","  ","");
        assertEquals("Proveedor SAC",datos.razonSocial());
        assertNull(datos.documento());
        assertNull(datos.telefono());
    }

    @Test
    void crearRegistraBitacoraConElIdNuevo() {
        var datos = new ProveedorDatos("Proveedor SAC","20123456789",null);
        when(proveedores.crear(datos)).thenReturn(new Proveedor(9,"Proveedor SAC","20123456789",null));

        var creado = gestionar.crear(datos,jefe);

        assertEquals(9,creado.idProveedor());
        verify(bitacora).registrar(2,"CREAR_PROVEEDOR id=9",null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"OPERARIO","CONTADORA"})
    void rolSinPermisoNoGestionaProveedores(String rol) {
        var actor = new Actor(5,"usuario",rol);

        var error = assertThrows(FallaNegocio.class,
            () -> gestionar.crear(new ProveedorDatos("X",null,null),actor));

        assertEquals("SIN_PERMISO",error.codigo());
        verifyNoInteractions(proveedores,bitacora);
    }

    @Test
    void actualizarInexistenteDevuelveProveedorNoEncontradoSinBitacora() {
        var datos = new ProveedorDatos("X",null,null);
        when(proveedores.actualizar(4,datos)).thenReturn(Optional.empty());

        var error = assertThrows(ProveedorNoEncontrado.class, () -> gestionar.actualizar(4,datos,jefe));

        assertEquals("PROVEEDOR_NO_ENCONTRADO",error.codigo());
        assertEquals(FallaNegocio.Tipo.NO_ENCONTRADO,error.tipo());
        verifyNoInteractions(bitacora);
    }

    @Test
    void desactivarRegistraBitacora() {
        when(proveedores.desactivar(4)).thenReturn(true);

        gestionar.desactivar(4,jefe);

        verify(bitacora).registrar(2,"DESACTIVAR_PROVEEDOR id=4",null);
    }

    @Test
    void consultarSoloDevuelveActivos() {
        when(proveedores.activos()).thenReturn(List.of(new Proveedor(1,"A",null,null)));
        when(proveedores.buscarActivo(3)).thenReturn(Optional.empty());
        var consultar = new ConsultarProveedores(proveedores);

        assertEquals(1,consultar.listar().size());
        assertThrows(ProveedorNoEncontrado.class, () -> consultar.obtener(3));
    }
}
