package pe.edu.upeu.moliselva.application.shared.cqrs;

/**
 * Petición de solo lectura (equivale a {@code IRequest<R>} de MediatR para una consulta).
 * El mediador la ejecuta dentro de una transacción de solo lectura.
 *
 * @param <R> tipo de la respuesta
 */
public interface Query<R> {}
