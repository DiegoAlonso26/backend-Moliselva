package pe.edu.upeu.moliselva.infrastructure.cqrs;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;
import pe.edu.upeu.moliselva.application.shared.cqrs.Query;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** El mediador busca el handler por el tipo de la petición y aplica la transacción que corresponde. */
class MediadorSpringTest {
    record Guardar(boolean fallar) implements Command<String> {}
    record Consultar(int id) implements Query<String> {}
    record SinHandler() implements Command<Void> {}

    static class GuardarHandler implements CommandHandler<Guardar, String> {
        @Override public String handle(Guardar command) {
            if (command.fallar()) { throw new IllegalStateException("regla incumplida"); }
            return "guardado";
        }
    }

    static class OtroGuardarHandler implements CommandHandler<Guardar, String> {
        @Override public String handle(Guardar command) { return "otro"; }
    }

    static class ConsultarHandler implements QueryHandler<Consultar, String> {
        @Override public String handle(Consultar query) { return "dato " + query.id(); }
    }

    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final TransactionStatus estado = mock(TransactionStatus.class);
    private final MediadorSpring mediador = new MediadorSpring(List.of(new GuardarHandler()),
        List.of(new ConsultarHandler()), manager);

    @Test
    void elCommandVaASuHandlerEnUnaTransaccionDeEscritura() {
        when(manager.getTransaction(any())).thenReturn(estado);

        assertEquals("guardado", mediador.enviar(new Guardar(false)));

        var definicion = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(manager).getTransaction(definicion.capture());
        assertFalse(definicion.getValue().isReadOnly());
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, definicion.getValue().getPropagationBehavior());
        verify(manager).commit(estado);
        verify(manager, never()).rollback(any());
    }

    @Test
    void laQueryVaASuHandlerEnUnaTransaccionDeSoloLectura() {
        when(manager.getTransaction(any())).thenReturn(estado);

        assertEquals("dato 5", mediador.enviar(new Consultar(5)));

        var definicion = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(manager).getTransaction(definicion.capture());
        assertTrue(definicion.getValue().isReadOnly());
    }

    @Test
    void unaFallaRevierteYSePropagaSinCambios() {
        when(manager.getTransaction(any())).thenReturn(estado);

        var falla = assertThrows(IllegalStateException.class, () -> mediador.enviar(new Guardar(true)));

        assertEquals("regla incumplida", falla.getMessage());
        verify(manager).rollback(estado);
        verify(manager, never()).commit(any());
    }

    @Test
    void sinHandlerRegistradoFallaSinAbrirTransaccion() {
        var error = assertThrows(IllegalStateException.class, () -> mediador.enviar(new SinHandler()));

        assertTrue(error.getMessage().contains("SinHandler"));
        verifyNoInteractions(manager);
    }

    @Test
    void dosHandlersParaElMismoCommandImpidenArrancar() {
        var error = assertThrows(IllegalStateException.class, () -> new MediadorSpring(
            List.of(new GuardarHandler(), new OtroGuardarHandler()), List.of(), manager));

        assertTrue(error.getMessage().contains("Guardar tiene dos handlers"));
    }

    @Test
    void unHandlerSinTipoConcretoImpideArrancar() {
        CommandHandler<Guardar, String> lambda = command -> "x";
        var error = assertThrows(IllegalStateException.class, () ->
            new MediadorSpring(List.of(lambda), List.of(), manager));
        assertSame(IllegalStateException.class, error.getClass());
    }
}
