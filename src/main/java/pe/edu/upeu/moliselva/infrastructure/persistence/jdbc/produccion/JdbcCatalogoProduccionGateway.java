package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.produccion;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.operaciones.OperacionRepository;
import pe.edu.upeu.moliselva.application.produccion.gateway.CatalogoProduccionGateway;

@Repository
public class JdbcCatalogoProduccionGateway implements CatalogoProduccionGateway {
    private final OperacionRepository db;
    public JdbcCatalogoProduccionGateway(OperacionRepository db) { this.db=db; }
    @Override public boolean productoActivo(long idProducto) {
        return db.one("SELECT 1 FROM producto WHERE id_producto=? AND activo=true",idProducto).isPresent();
    }
    @Override public boolean formulaVigente(long idFormula, long idProducto) {
        return db.one("SELECT 1 FROM formula WHERE id_formula=? AND id_producto=? AND vigente=true",
            idFormula,idProducto).isPresent();
    }
    @Override public boolean formulaTieneDetalle(long idFormula) {
        return db.one("SELECT 1 FROM formula_detalle WHERE id_formula=?",idFormula).isPresent();
    }
    @Override public boolean insumoActivo(long idInsumo) {
        return db.one("SELECT 1 FROM insumo WHERE id_insumo=? AND activo=true",idInsumo).isPresent();
    }
    @Override public boolean insumoEnFormula(long idLote, long idInsumo) {
        return db.one("SELECT 1 FROM lote l JOIN formula_detalle fd ON fd.id_formula=l.id_formula " +
            "WHERE l.id_lote=? AND fd.id_insumo=?",idLote,idInsumo).isPresent();
    }
}
