package pe.edu.upeu.moliselva.comun;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BitacoraService {
    private static final Logger log = LoggerFactory.getLogger(BitacoraService.class);
    private final BitacoraRepository repository;

    public BitacoraService(BitacoraRepository repository) { this.repository = repository; }

    public void registrar(long usuarioId, String accion, String motivo) {
        repository.registrar(usuarioId,accion,motivo);
        log.info("bitacora usuarioId={} accion={}", usuarioId, accion);
    }

    public List<Map<String,Object>> recientes() { return repository.recientes(); }
}
