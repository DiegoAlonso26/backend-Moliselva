package pe.edu.upeu.moliselva.infrastructure.persistence.jpa.recepcion;

import pe.edu.upeu.moliselva.domain.recepcion.PesajeMaiz;
import pe.edu.upeu.moliselva.domain.recepcion.PorcentajeHumedad;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

/** Convierte entre la entidad de persistencia (columnas) y la entidad del dominio (reglas). */
final class RecepcionJpaMapper {
    private RecepcionJpaMapper() {}

    static Recepcion aDominio(RecepcionJpa fila) {
        return Recepcion.existente(fila.getId(), fila.getIdProveedor(), fila.getIdInsumo(), fila.getIdRegistra(),
            PesajeMaiz.de(fila.getPesoBrutoKg(), fila.getTaraKg(), fila.getPesoSacosKg(), fila.getMermaLimpiezaKg()),
            PorcentajeHumedad.de(fila.getHumedadPct()), fila.getEstado(), fila.getIdAutoriza());
    }

    static RecepcionJpa nueva(Recepcion recepcion) {
        var fila = new RecepcionJpa(recepcion.idInsumo(), recepcion.idRegistra());
        copiar(recepcion, fila);
        return fila;
    }

    /** Copia el estado del dominio en la fila administrada; JPA detecta los cambios. */
    static void copiar(Recepcion recepcion, RecepcionJpa fila) {
        var pesaje = recepcion.pesaje();
        fila.setIdProveedor(recepcion.idProveedor());
        fila.setPesoBrutoKg(pesaje.bruto().valor());
        fila.setTaraKg(pesaje.tara().valor());
        fila.setPesoSacosKg(pesaje.sacos().valor());
        fila.setMermaLimpiezaKg(pesaje.merma().valor());
        fila.setHumedadPct(recepcion.humedad().valor());
        fila.setEstado(recepcion.estado());
        fila.setIdAutoriza(recepcion.idAutoriza());
    }
}
