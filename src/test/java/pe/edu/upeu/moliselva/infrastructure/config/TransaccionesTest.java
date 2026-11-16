package pe.edu.upeu.moliselva.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** El envoltorio debe comportarse igual que el @Transactional que reemplaza. */
class TransaccionesTest {
    interface ConsultarAlgoUseCase { String consultar(); }
    interface GestionarAlgoUseCase { String guardar(boolean fallar); }

    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final TransactionStatus estado = mock(TransactionStatus.class);
    private final Transacciones tx = new Transacciones(manager);

    @Test
    void escrituraHaceCommitYNoEsSoloLectura() {
        when(manager.getTransaction(any())).thenReturn(estado);
        GestionarAlgoUseCase caso = tx.envolver(GestionarAlgoUseCase.class, fallar -> "ok");

        assertEquals("ok", caso.guardar(false));

        var definicion = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(manager).getTransaction(definicion.capture());
        assertFalse(definicion.getValue().isReadOnly());
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, definicion.getValue().getPropagationBehavior());
        verify(manager).commit(estado);
        verify(manager, never()).rollback(any());
    }

    @Test
    void fallaDeNegocioRevierteYSePropagaSinCambios() {
        when(manager.getTransaction(any())).thenReturn(estado);
        var falla = new IllegalStateException("regla incumplida");
        GestionarAlgoUseCase caso = tx.envolver(GestionarAlgoUseCase.class, fallar -> { throw falla; });

        var lanzada = assertThrows(IllegalStateException.class, () -> caso.guardar(true));

        assertSame(falla, lanzada);
        verify(manager).rollback(estado);
        verify(manager, never()).commit(any());
    }

    @Test
    void consultarEsSoloLectura() {
        when(manager.getTransaction(any())).thenReturn(estado);
        ConsultarAlgoUseCase caso = tx.envolver(ConsultarAlgoUseCase.class, () -> "dato");

        assertEquals("dato", caso.consultar());

        var definicion = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(manager).getTransaction(definicion.capture());
        assertTrue(definicion.getValue().isReadOnly());
    }
}
