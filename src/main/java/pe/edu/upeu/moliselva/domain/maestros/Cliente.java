package pe.edu.upeu.moliselva.domain.maestros;

public record Cliente(long idCliente, String tipoCliente, String nombreRazonSocial,
        String documento, String telefono, String zona) {}
