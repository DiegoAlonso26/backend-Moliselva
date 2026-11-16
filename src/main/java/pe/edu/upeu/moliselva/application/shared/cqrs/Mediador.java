package pe.edu.upeu.moliselva.application.shared.cqrs;

/**
 * Intermediario entre la presentación y los casos de uso (equivale a {@code IMediator} de MediatR).
 * El controlador solo arma el Command o la Query y lo envía; el mediador busca el handler
 * registrado para ese tipo y lo ejecuta en su transacción.
 */
public interface Mediador {
    <R> R enviar(Command<R> command);

    <R> R enviar(Query<R> query);
}
