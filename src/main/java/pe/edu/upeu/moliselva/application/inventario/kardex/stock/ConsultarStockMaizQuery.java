package pe.edu.upeu.moliselva.application.inventario.kardex.stock;

import pe.edu.upeu.moliselva.application.shared.cqrs.Query;

/** Stock actual de maíz en kg. Disponible para los cuatro roles. */
public record ConsultarStockMaizQuery() implements Query<StockMaizResponse> {}
