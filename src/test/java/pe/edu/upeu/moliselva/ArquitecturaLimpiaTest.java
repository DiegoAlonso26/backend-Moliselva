package pe.edu.upeu.moliselva;

import java.util.HashMap;
import java.util.Map;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.core.ResolvableType;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;
import pe.edu.upeu.moliselva.application.shared.cqrs.Mediador;
import pe.edu.upeu.moliselva.application.shared.cqrs.Query;
import pe.edu.upeu.moliselva.application.shared.cqrs.QueryHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regla de dependencias de Clean Architecture para todo el backend:
 * presentation → application → domain y infrastructure → application → domain.
 * Toda clase vive en una de las cuatro capas (salvo MoliselvaApplication).
 */
@AnalyzeClasses(packages = "pe.edu.upeu.moliselva", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaLimpiaTest {
    private static final String RAIZ = "pe.edu.upeu.moliselva";
    private static final String DOMAIN = RAIZ + ".domain..";
    private static final String APPLICATION = RAIZ + ".application..";
    private static final String PRESENTATION = RAIZ + ".presentation..";
    private static final String INFRASTRUCTURE = RAIZ + ".infrastructure..";
    private static final String[] FRAMEWORKS = { "org.springframework..", "jakarta..", "io.swagger..", "org.hibernate.." };
    /** Módulos con CQRS completo: entidades con comportamiento y value objects sin id. */
    private static final String[] DOMINIO_CQRS = { RAIZ + ".domain.recepcion..", RAIZ + ".domain.shared.." };

    @ArchTest
    static final ArchRule dominioSinFrameworks = noClasses().that().resideInAPackage(DOMAIN)
        .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
        .because("el dominio es Java puro");

    @ArchTest
    static final ArchRule dominioNoConoceCapasExternas = noClasses().that().resideInAPackage(DOMAIN)
        .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, PRESENTATION, INFRASTRUCTURE)
        .because("el dominio es el centro: no depende de ninguna otra capa");

    @ArchTest
    static final ArchRule aplicacionSinFrameworks = noClasses().that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
        .because("los casos de uso son Java puro; la transacción se aplica en infrastructure.config");

    @ArchTest
    static final ArchRule aplicacionSoloConoceDominio = noClasses().that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(PRESENTATION, INFRASTRUCTURE)
        .because("application solo depende de domain");

    @ArchTest
    static final ArchRule todoEnUnaCapa = classes().should()
        .resideInAnyPackage(DOMAIN, APPLICATION, PRESENTATION, INFRASTRUCTURE, RAIZ)
        .because("no quedan módulos fuera de las cuatro capas; en la raíz solo está MoliselvaApplication");

    @ArchTest
    static final ArchRule raizSoloConLaAplicacion = classes().that().resideInAPackage(RAIZ)
        .should().haveSimpleName("MoliselvaApplication")
        .because("la raíz del paquete solo arranca Spring Boot");

    @ArchTest
    static final ArchRule presentacionNoConoceInfraestructura = noClasses().that().resideInAPackage(PRESENTATION)
        .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
        .because("presentation llega a los datos solo a través de los casos de uso");

    @ArchTest
    static final ArchRule controladoresSinGateways = noClasses().that().resideInAPackage(PRESENTATION)
        .should().dependOnClassesThat().resideInAnyPackage(RAIZ + ".application..gateway..", "org.springframework.jdbc..")
        .because("los controladores llaman casos de uso, nunca gateways ni JDBC");

    @ArchTest
    static final ArchRule infraestructuraNoConocePresentacion = noClasses().that().resideInAPackage(INFRASTRUCTURE)
        .should().dependOnClassesThat().resideInAPackage(PRESENTATION)
        .because("infrastructure y presentation son capas externas independientes");

    // ---- CQRS: Command / Query + Handler + Response, enviados por el Mediador ----

    @ArchTest
    static final ArchRule presentacionSoloConoceElMediador = noClasses().that().resideInAPackage(PRESENTATION)
        .should().dependOnClassesThat().areAssignableTo(CommandHandler.class)
        .orShould().dependOnClassesThat().areAssignableTo(QueryHandler.class)
        .because("los controladores envían commands y queries al Mediador; nunca llaman a un handler");

    @ArchTest
    static final ArchRule commandsSonRecordsInmutables = classes().that().implement(Command.class)
        .should().haveSimpleNameEndingWith("Command").andShould(serRecord())
        .because("un Command es un mensaje inmutable con la entrada del caso de uso");

    @ArchTest
    static final ArchRule queriesSonRecordsInmutables = classes().that().implement(Query.class)
        .should().haveSimpleNameEndingWith("Query").andShould(serRecord())
        .because("una Query es un mensaje inmutable con los criterios de la consulta");

    @ArchTest
    static final ArchRule handlersEnAplicacion = classes().that().implement(CommandHandler.class)
        .or().implement(QueryHandler.class)
        .should().haveSimpleNameEndingWith("Handler").andShould().resideInAPackage(APPLICATION)
        .because("cada caso de uso tiene su handler en application, sin Spring");

    @ArchTest
    static final ArchRule mediadorSoloSeImplementaEnInfraestructura = classes().that().implement(Mediador.class)
        .should().resideInAPackage(INFRASTRUCTURE)
        .because("el mediador aplica las transacciones, que son un detalle técnico");

    @ArchTest
    static final ArchRule valueObjectsSinId = classes().that().resideInAnyPackage(DOMINIO_CQRS).and().areRecords()
        .should(noTenerComponentesId())
        .because("un value object se identifica por su valor; lo que tiene id es una entidad");

    @ArchTest
    static final ArchRule entidadesNoSonRecords = classes().that().resideInAnyPackage(DOMINIO_CQRS)
        .and().haveSimpleName("Recepcion").should().notBeRecords()
        .because("una entidad tiene identidad y ciclo de vida: cambia por sus métodos, no se copia");

    @ArchTest
    static void cadaCommandYQueryTieneExactamenteUnHandler(JavaClasses clases) {
        Map<Class<?>, Integer> handlers = new HashMap<>();
        for (JavaClass clase : clases) {
            Class<?> tipo = clase.reflect();
            for (Class<?> contrato : new Class<?>[]{CommandHandler.class, QueryHandler.class}) {
                if (contrato.isAssignableFrom(tipo) && !tipo.isInterface()) {
                    Class<?> atendido = ResolvableType.forClass(tipo).as(contrato).getGeneric(0).resolve();
                    handlers.merge(atendido, 1, Integer::sum);
                }
            }
        }
        int peticiones = 0;
        for (JavaClass clase : clases) {
            Class<?> tipo = clase.reflect();
            if ((Command.class.isAssignableFrom(tipo) || Query.class.isAssignableFrom(tipo)) && !tipo.isInterface()) {
                peticiones++;
                assertEquals(1, handlers.getOrDefault(tipo, 0), tipo.getSimpleName() + " debe tener exactamente un handler");
            }
        }
        assertTrue(peticiones > 0, "no se encontraron commands ni queries");
        assertEquals(peticiones, handlers.values().stream().mapToInt(Integer::intValue).sum(),
            "hay handlers de peticiones que no existen");
    }

    private static ArchCondition<JavaClass> serRecord() {
        return new ArchCondition<>("ser un record") {
            @Override
            public void check(JavaClass clase, ConditionEvents eventos) {
                eventos.add(new SimpleConditionEvent(clase, clase.reflect().isRecord(),
                    clase.getName() + (clase.reflect().isRecord() ? " es" : " no es") + " un record"));
            }
        };
    }

    private static ArchCondition<JavaClass> noTenerComponentesId() {
        return new ArchCondition<>("no tener componentes id") {
            @Override
            public void check(JavaClass clase, ConditionEvents eventos) {
                for (var componente : clase.reflect().getRecordComponents()) {
                    if (componente.getName().startsWith("id")) {
                        eventos.add(SimpleConditionEvent.violated(clase,
                            clase.getName() + " tiene el componente " + componente.getName()));
                    }
                }
            }
        };
    }
}
