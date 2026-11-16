package pe.edu.upeu.moliselva.domain.inventario;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Formato del kardex exportado: UTF-8 con BOM, separador punto y coma y fin de
 * línea CRLF, para que Excel en español lo abra sin pasos adicionales.
 */
public final class KardexCsv {
    static final String ENCABEZADO = "﻿idMovimiento;fechaHora;tipoMovimiento;cantidadKg;saldoKg;idRecepcion;usuario\r\n";

    private KardexCsv() {}

    public static byte[] generar(List<MovimientoKardex> movimientos) {
        StringBuilder csv = new StringBuilder(ENCABEZADO);
        for (var row : movimientos) {
            csv.append(row.idMovimiento()).append(';').append(row.fechaHora()).append(';')
                .append(row.tipoMovimiento()).append(';').append(row.cantidadKg().toPlainString()).append(';')
                .append(row.saldoKg().toPlainString()).append(';')
                .append(row.idRecepcion()==null ? "" : row.idRecepcion()).append(';')
                .append(texto(row.usuario())).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String texto(String value) {
        return "\""+value.replace("\"","\"\"")+"\"";
    }
}
