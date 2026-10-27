package pe.edu.upeu.moliselva.recepcion.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Recepcion(
    long idRecepcion, LocalDateTime fechaHora, long idProveedor, String proveedor,
    BigDecimal pesoBrutoKg, BigDecimal taraKg, BigDecimal pesoSacosKg,
    BigDecimal pesoNetoKg, BigDecimal humedadPct, BigDecimal mermaLimpiezaKg,
    EstadoRecepcion estado, BigDecimal entradaKardexKg,
    String registradoPor, String autorizadoPor) {

    public Recepcion conRegistradoPor(String usuario) {
        return new Recepcion(idRecepcion,fechaHora,idProveedor,proveedor,
            pesoBrutoKg,taraKg,pesoSacosKg,pesoNetoKg,humedadPct,mermaLimpiezaKg,
            estado,entradaKardexKg,usuario,autorizadoPor);
    }
}
