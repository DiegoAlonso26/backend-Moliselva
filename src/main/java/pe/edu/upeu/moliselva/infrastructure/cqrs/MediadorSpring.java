package pe.edu.upeu.moliselva.infrastructure.cqrs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;
import pe.edu.upeu.moliselva.application.shared.cqrs.Mediador;
import pe.edu.upeu.moliselva.application.shared.cqrs.Query;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

/**
 * Implementación del {@link Mediador} con Spring.
 * <ul>
 *   <li>Al arrancar registra cada handler según el tipo genérico que declara
 *       ({@code CommandHandler<RegistrarRecepcionCommand, ...>}). Si dos handlers atienden
 *       el mismo tipo, la aplicación no arranca.</li>
 *   <li>Pipeline de transacciones (como un {@code IPipelineBehavior}): los commands corren en
 *       una transacción de escritura y las queries en una de solo lectura. Cualquier
 *       RuntimeException revierte todo y se propaga sin cambios.</li>
 * </ul>
 */
@Component
public class MediadorSpring implements Mediador {
    private final Map<Class<?>, CommandHandler<?, ?>> commands = new HashMap<>();
    private final Map<Class<?>, QueryHandler<?, ?>> queries = new HashMap<>();
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    @Autowired
    public MediadorSpring(ObjectProvider<CommandHandler<?, ?>> commandHandlers,
            ObjectProvider<QueryHandler<?, ?>> queryHandlers, PlatformTransactionManager transacciones) {
        this(commandHandlers.orderedStream().toList(), queryHandlers.orderedStream().toList(), transacciones);
    }

    MediadorSpring(List<CommandHandler<?, ?>> commandHandlers, List<QueryHandler<?, ?>> queryHandlers,
            PlatformTransactionManager transacciones) {
        for (var handler : commandHandlers) {
            registrar(commands, tipoAtendido(handler, CommandHandler.class), handler);
        }
        for (var handler : queryHandlers) {
            registrar(queries, tipoAtendido(handler, QueryHandler.class), handler);
        }
        this.escritura = new TransactionTemplate(transacciones);
        this.lectura = new TransactionTemplate(transacciones);
        this.lectura.setReadOnly(true);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> R enviar(Command<R> command) {
        CommandHandler handler = buscar(commands, command);
        return escritura.execute(estado -> (R) handler.handle(command));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> R enviar(Query<R> query) {
        QueryHandler handler = buscar(queries, query);
        return lectura.execute(estado -> (R) handler.handle(query));
    }

    private static <H> H buscar(Map<Class<?>, H> handlers, Object peticion) {
        H handler = handlers.get(peticion.getClass());
        if (handler == null) {
            throw new IllegalStateException("No hay handler registrado para " + peticion.getClass().getSimpleName());
        }
        return handler;
    }

    private static Class<?> tipoAtendido(Object handler, Class<?> contrato) {
        Class<?> tipo = ResolvableType.forClass(AopUtils.getTargetClass(handler)).as(contrato).getGeneric(0).resolve();
        if (tipo == null || tipo.isInterface()) {
            throw new IllegalStateException(handler.getClass().getName()
                + " debe declarar el tipo concreto de su " + contrato.getSimpleName());
        }
        return tipo;
    }

    private static <H> void registrar(Map<Class<?>, H> handlers, Class<?> tipo, H handler) {
        H anterior = handlers.putIfAbsent(tipo, handler);
        if (anterior != null) {
            throw new IllegalStateException(tipo.getSimpleName() + " tiene dos handlers: "
                + anterior.getClass().getSimpleName() + " y " + handler.getClass().getSimpleName());
        }
    }
}
