package pe.edu.upeu.moliselva.application.shared;

/**
 * Entrega el usuario autenticado de la petición en curso. Lo implementa la
 * infraestructura de seguridad a partir del token validado; nunca sale del
 * cuerpo de la petición.
 */
public interface ActorActual {
    Actor obtener();
}
