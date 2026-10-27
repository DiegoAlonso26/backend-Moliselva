package pe.edu.upeu.moliselva.recepcion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.recepcion.application.port.in.CorregirRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.out.BitacoraPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.InventarioPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.ProveedorPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionDatos;

@Service
public class CorregirRecepcion implements CorregirRecepcionUseCase {
    private final RecepcionPort recepciones;
    private final ProveedorPort proveedores;
    private final InventarioPort inventario;
    private final BitacoraPort bitacora;
    public CorregirRecepcion(RecepcionPort recepciones, ProveedorPort proveedores,
            InventarioPort inventario, BitacoraPort bitacora) {
        this.recepciones=recepciones; this.proveedores=proveedores;
        this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override @Transactional
    public Recepcion corregir(long id, RecepcionDatos datos, ActorRecepcion actor) {
        RecepcionChecks.puedeRegistrar(actor);
        var locked=recepciones.bloquear(id).orElseThrow(RecepcionChecks::missing);
        RecepcionChecks.editable(locked);
        RecepcionChecks.proveedorActivo(proveedores,datos.idProveedor());
        var evaluacion=RecepcionChecks.evaluar(datos);
        if (!recepciones.corregir(id,datos,evaluacion.estado())) {
            throw RecepcionChecks.notEditable();
        }
        if (evaluacion.estado()==EstadoRecepcion.ACEPTADA) {
            inventario.registrarEntrada(locked.idInsumo(),id,evaluacion.entradaKardexKg(),actor.id());
        }
        bitacora.registrar(actor.id(),"ACTUALIZAR_RECEPCION id="+id+" estado="+evaluacion.estado(),null);
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing);
    }
}
