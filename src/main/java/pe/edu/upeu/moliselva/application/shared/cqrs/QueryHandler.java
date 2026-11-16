package pe.edu.upeu.moliselva.application.shared.cqrs;

/** Atiende un único tipo de {@link Query} (equivale a {@code IRequestHandler<Q,R>} de MediatR). */
public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
