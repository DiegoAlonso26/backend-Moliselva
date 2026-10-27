package pe.edu.upeu.moliselva.operaciones;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OperacionRepository {
    private final JdbcTemplate db;
    public OperacionRepository(JdbcTemplate db) { this.db=db; }

    public List<Map<String,Object>> list(String sql, Object... args) {
        return db.queryForList(sql,args);
    }
    public Optional<Map<String,Object>> one(String sql, Object... args) {
        return list(sql,args).stream().findFirst();
    }
    public Map<String,Object> insert(String sql, Object... args) {
        return one(sql,args).orElseThrow(() -> new IllegalStateException("INSERT sin fila devuelta"));
    }
    public int update(String sql, Object... args) { return db.update(sql,args); }
    public void confirmarDespacho(long id, long userId) {
        db.queryForObject("SELECT confirmar_despacho(?,?)",Object.class,id,userId);
    }
}
