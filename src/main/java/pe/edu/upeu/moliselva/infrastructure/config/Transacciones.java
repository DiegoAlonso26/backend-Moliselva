package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Envuelve un caso de uso (Java puro) en una transacción, sin anotar el caso de uso.
 * Convención: las interfaces cuyo nombre empieza con {@code Consultar} son de solo
 * lectura; todas las demás abren una transacción de escritura. Como con
 * {@code @Transactional}, cualquier RuntimeException o Error revierte todo.
 */
@Component
public class Transacciones {
    private final PlatformTransactionManager transacciones;

    public Transacciones(PlatformTransactionManager transacciones) {
        this.transacciones = transacciones;
    }

    public <T> T envolver(Class<T> contrato, T casoDeUso) {
        var atributo = new DefaultTransactionAttribute();
        atributo.setReadOnly(contrato.getSimpleName().startsWith("Consultar"));
        var fuente = new MatchAlwaysTransactionAttributeSource();
        fuente.setTransactionAttribute(atributo);

        var proxy = new ProxyFactory();
        proxy.setTarget(casoDeUso);
        proxy.addInterface(contrato);
        proxy.addAdvice(new TransactionInterceptor(transacciones, fuente));
        return contrato.cast(proxy.getProxy(contrato.getClassLoader()));
    }
}
