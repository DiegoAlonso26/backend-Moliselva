package pe.edu.upeu.moliselva.proveedor;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProveedorService {
    private final ProveedorRepository repository;
    public ProveedorService(ProveedorRepository repository) { this.repository=repository; }
    public List<ProveedorDto> list() { return repository.activos(); }
    public boolean existeActivo(long id) { return repository.existeActivo(id); }
}
