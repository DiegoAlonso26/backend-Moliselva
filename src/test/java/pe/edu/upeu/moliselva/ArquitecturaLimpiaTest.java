package pe.edu.upeu.moliselva;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regla de dependencias de Clean Architecture (PO-01, producción y ventas):
 * presentation → application → domain y infrastructure → application → domain.
 * Los módulos legados (auth, comun, inventario, maestros, operaciones, service)
 * quedan fuera de las capas hasta la siguiente fase.
 */
@AnalyzeClasses(packages = "pe.edu.upeu.moliselva", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaLimpiaTest {
    private static final String RAIZ = "pe.edu.upeu.moliselva";
    private static final String DOMAIN = RAIZ + ".domain..";
    private static final String APPLICATION = RAIZ + ".application..";
    private static final String PRESENTATION = RAIZ + ".presentation..";
    private static final String INFRASTRUCTURE = RAIZ + ".infrastructure..";
    private static final String[] LEGADO = {
        RAIZ + ".auth..", RAIZ + ".comun..", RAIZ + ".inventario..", RAIZ + ".maestros..",
        RAIZ + ".operaciones..", RAIZ + ".service.."
    };
    private static final String[] FRAMEWORKS = { "org.springframework..", "jakarta..", "io.swagger.." };

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
    static final ArchRule nucleoSinModulosLegados = noClasses().that().resideInAnyPackage(DOMAIN, APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(LEGADO)
        .because("el núcleo de PO-01 no debe depender del código por migrar");

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
}
