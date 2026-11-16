package pe.edu.upeu.moliselva.application.shared.cqrs;

/**
 * Petición que cambia el estado del sistema (equivale a {@code IRequest<R>} de MediatR para un comando).
 * El mediador la ejecuta dentro de una transacción de escritura.
 *
 * @param <R> tipo de la respuesta; {@link Void} si no devuelve nada
 */
public interface Command<R> {}
