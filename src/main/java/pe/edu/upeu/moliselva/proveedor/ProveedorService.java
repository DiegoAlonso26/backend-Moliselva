package pe.edu.upeu.moliselva.proveedor;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.comun.ApiException;
import pe.edu.upeu.moliselva.comun.BitacoraService;

@Service
public class ProveedorService {
    private final ProveedorRepository repository;
    private final BitacoraService audit;
    public ProveedorService(ProveedorRepository repository, BitacoraService audit) {
        this.repository=repository; this.audit=audit;
    }
    public List<ProveedorDto> list() { return repository.activos(); }
    public ProveedorDto find(long id) { return repository.find(id).orElseThrow(() -> missing()); }

    @Transactional
    public ProveedorDto crear(ProveedorDto.Datos input, long usuarioId) {
        ProveedorDto proveedor = repository.crear(input.razonSocial().trim(),
            optional(input.documento()),optional(input.telefono()));
        audit.registrar(usuarioId,"CREAR_PROVEEDOR id="+proveedor.idProveedor(),null);
        return proveedor;
    }

    @Transactional
    public ProveedorDto actualizar(long id, ProveedorDto.Datos input, long usuarioId) {
        ProveedorDto proveedor = repository.actualizar(id,input.razonSocial().trim(),
            optional(input.documento()),optional(input.telefono())).orElseThrow(() -> missing());
        audit.registrar(usuarioId,"ACTUALIZAR_PROVEEDOR id="+id,null);
        return proveedor;
    }

    @Transactional
    public void eliminar(long id, long usuarioId) {
        if (!repository.desactivar(id)) { throw missing(); }
        audit.registrar(usuarioId,"DESACTIVAR_PROVEEDOR id="+id,null);
    }

    public boolean existeActivo(long id) { return repository.existeActivo(id); }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND,"PROVEEDOR_NO_ENCONTRADO","El proveedor no existe o está inactivo");
    }
}
