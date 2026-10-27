package pe.edu.upeu.moliselva.recepcion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.comun.ApiException;
import pe.edu.upeu.moliselva.comun.BitacoraService;
import pe.edu.upeu.moliselva.inventario.InventarioService;
import pe.edu.upeu.moliselva.proveedor.ProveedorService;
import pe.edu.upeu.moliselva.service.BusinessRules;

@Service
public class RecepcionService {
    private final RecepcionRepository repository;
    private final ProveedorService proveedores;
    private final InventarioService inventario;
    private final BitacoraService audit;

    public RecepcionService(RecepcionRepository repository, ProveedorService proveedores,
            InventarioService inventario, BitacoraService audit) {
        this.repository=repository; this.proveedores=proveedores;
        this.inventario=inventario; this.audit=audit;
    }

    @Transactional
    public RecepcionDto.Respuesta crear(RecepcionDto.Crear input, long usuarioId, String usuario) {
        if (!proveedores.existeActivo(input.idProveedor())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"PROVEEDOR_INVALIDO","Proveedor inexistente o inactivo");
        }
        long maizId = inventario.maizId();
        BigDecimal neto;
        BigDecimal entrada;
        String estado;
        try {
            neto = BusinessRules.pesoNeto(input.pesoBrutoKg(),input.taraKg(),input.pesoSacosKg());
            entrada = BusinessRules.entradaKardex(neto,input.mermaLimpiezaKg());
            estado = BusinessRules.estadoRecepcion(input.humedadPct());
        } catch (IllegalArgumentException ex) {
            String codigo = ex.getMessage().contains("humedad") ? "HUMEDAD_FUERA_DE_RANGO" : "PESOS_INVALIDOS";
            throw new ApiException(HttpStatus.BAD_REQUEST,codigo,ex.getMessage());
        }
        long id = repository.insert(input.idProveedor(),maizId,input,estado);
        if (estado.equals("ACEPTADA")) {
            inventario.registrarEntrada(maizId,id,entrada,usuarioId);
        }
        audit.registrar(usuarioId,"REGISTRAR_RECEPCION id="+id+" estado="+estado,null);
        return repository.find(id).orElseThrow(() -> missing()).conRegistradoPor(usuario);
    }

    @Transactional
    public RecepcionDto.Respuesta decidir(long id, RecepcionDto.Decision input, long usuarioId) {
        var locked = repository.lock(id).orElseThrow(() -> missing());
        if (!locked.estado().equals("PENDIENTE")) {
            throw new ApiException(HttpStatus.CONFLICT,"RECEPCION_NO_PENDIENTE","La recepción ya tiene una decisión");
        }
        String estado = input.decision().equals("ACEPTAR") ? "ACEPTADA" : "RECHAZADA";
        if (!repository.decide(id,estado,usuarioId)) {
            throw new ApiException(HttpStatus.CONFLICT,"RECEPCION_NO_PENDIENTE","La recepción ya tiene una decisión");
        }
        if (estado.equals("ACEPTADA")) {
            BigDecimal entrada = BusinessRules.entradaKardex(locked.pesoNetoKg(),locked.mermaLimpiezaKg());
            inventario.registrarEntrada(locked.idInsumo(),id,entrada,usuarioId);
        }
        audit.registrar(usuarioId,"DECISION_RECEPCION id="+id+" estado="+estado,input.motivo().trim());
        return repository.find(id).orElseThrow(() -> missing());
    }

    @Transactional(readOnly=true)
    public List<RecepcionDto.Respuesta> listar(LocalDate fecha) { return repository.list(fecha); }

    @Transactional(readOnly=true)
    public RecepcionDto.Respuesta find(long id) { return repository.find(id).orElseThrow(() -> missing()); }

    @Transactional
    public RecepcionDto.Respuesta actualizar(long id, RecepcionDto.Crear input, long usuarioId) {
        var locked = repository.lock(id).orElseThrow(() -> missing());
        editable(locked);
        if (!proveedores.existeActivo(input.idProveedor())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"PROVEEDOR_INVALIDO","Proveedor inexistente o inactivo");
        }
        BigDecimal entrada;
        String estado;
        try {
            BigDecimal neto = BusinessRules.pesoNeto(input.pesoBrutoKg(),input.taraKg(),input.pesoSacosKg());
            entrada = BusinessRules.entradaKardex(neto,input.mermaLimpiezaKg());
            estado = BusinessRules.estadoRecepcion(input.humedadPct());
        } catch (IllegalArgumentException ex) {
            String codigo = ex.getMessage().contains("humedad") ? "HUMEDAD_FUERA_DE_RANGO" : "PESOS_INVALIDOS";
            throw new ApiException(HttpStatus.BAD_REQUEST,codigo,ex.getMessage());
        }
        if (!repository.updateEditable(id,input,estado)) { throw notEditable(); }
        if (estado.equals("ACEPTADA")) {
            inventario.registrarEntrada(locked.idInsumo(),id,entrada,usuarioId);
        }
        audit.registrar(usuarioId,"ACTUALIZAR_RECEPCION id="+id+" estado="+estado,null);
        return find(id);
    }

    @Transactional
    public void eliminar(long id, long usuarioId) {
        var locked = repository.lock(id).orElseThrow(() -> missing());
        editable(locked);
        if (!repository.deleteEditable(id)) { throw notEditable(); }
        audit.registrar(usuarioId,"ELIMINAR_RECEPCION id="+id,null);
    }

    private void editable(RecepcionRepository.Bloqueo row) {
        if (row.estado().equals("ACEPTADA") || row.idAutoriza() != null) { throw notEditable(); }
    }

    private ApiException notEditable() {
        return new ApiException(HttpStatus.CONFLICT,"RECEPCION_NO_EDITABLE",
            "La recepción aceptada o decidida no se puede modificar ni eliminar");
    }

    private ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND,"RECEPCION_NO_ENCONTRADA","La recepción no existe");
    }
}
