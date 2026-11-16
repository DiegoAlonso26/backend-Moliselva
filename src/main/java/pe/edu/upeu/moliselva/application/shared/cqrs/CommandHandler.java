package pe.edu.upeu.moliselva.application.shared.cqrs;

/** Atiende un único tipo de {@link Command} (equivale a {@code IRequestHandler<C,R>} de MediatR). */
public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
