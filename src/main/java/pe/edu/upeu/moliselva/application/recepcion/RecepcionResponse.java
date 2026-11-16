package pe.edu.upeu.moliselva.application.recepcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;

/**
 * Respuesta de los casos de uso de recepción: la vista completa de una recepción, con el
 * nombre del proveedor, la entrada al kardex y los usuarios que la registraron y la
 * decidieron. La comparten registrar, corregir, decidir, listar y obtener.
 */
public record RecepcionResponse(
    long idRecepcion, LocalDateTime fechaHora, long idProveedor, String proveedor,
    BigDecimal pesoBrutoKg, BigDecimal taraKg, BigDecimal pesoSacosKg,
    BigDecimal pesoNetoKg, BigDecimal humedadPct, BigDecimal mermaLimpiezaKg,
    EstadoRecepcion estado, BigDecimal entradaKardexKg,
    String registradoPor, String autorizadoPor) {

    public RecepcionResponse conRegistradoPor(String usuario) {
        return new RecepcionResponse(idRecepcion,fechaHora,idProveedor,proveedor,
            pesoBrutoKg,taraKg,pesoSacosKg,pesoNetoKg,humedadPct,mermaLimpiezaKg,
            estado,entradaKardexKg,usuario,autorizadoPor);
    }
}
