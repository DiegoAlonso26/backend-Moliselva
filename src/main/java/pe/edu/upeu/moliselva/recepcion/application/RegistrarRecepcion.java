package pe.edu.upeu.moliselva.recepcion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.recepcion.application.port.in.RegistrarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.out.BitacoraPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.InventarioPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.ProveedorPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionDatos;

@Service
public class RegistrarRecepcion implements RegistrarRecepcionUseCase {
    private final RecepcionPort recepciones;
    private final ProveedorPort proveedores;
    private final InventarioPort inventario;
    private final BitacoraPort bitacora;

    public RegistrarRecepcion(RecepcionPort recepciones, ProveedorPort proveedores,
            InventarioPort inventario, BitacoraPort bitacora) {
        this.recepciones=recepciones; this.proveedores=proveedores;
        this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override @Transactional
    public Recepcion registrar(RecepcionDatos datos, ActorRecepcion actor) {
        RecepcionChecks.puedeRegistrar(actor);
        RecepcionChecks.proveedorActivo(proveedores,datos.idProveedor());
        long idMaiz=RecepcionChecks.maizId(inventario);
        var evaluacion=RecepcionChecks.evaluar(datos);
        long id=recepciones.insertar(datos,idMaiz,evaluacion.estado());
        if (evaluacion.estado()==EstadoRecepcion.ACEPTADA) {
            inventario.registrarEntrada(idMaiz,id,evaluacion.entradaKardexKg(),actor.id());
        }
        bitacora.registrar(actor.id(),"REGISTRAR_RECEPCION id="+id+" estado="+evaluacion.estado(),null);
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing)
            .conRegistradoPor(actor.usuario());
    }
}
