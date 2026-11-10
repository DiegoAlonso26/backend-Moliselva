package pe.edu.upeu.moliselva.comun;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pe.edu.upeu.moliselva.produccion.adapter.in.web.LoteController;
import pe.edu.upeu.moliselva.produccion.application.port.in.ConsultarLotesUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.in.PlanificarLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.in.GestionarInsumosLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.in.CambiarEstadoLoteUseCase;
import pe.edu.upeu.moliselva.produccion.application.port.in.FinalizarLoteUseCase;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;
import pe.edu.upeu.moliselva.auth.AuthController;
import pe.edu.upeu.moliselva.auth.AuthDto;
import pe.edu.upeu.moliselva.auth.AuthRepository;
import pe.edu.upeu.moliselva.auth.AuthService;
import pe.edu.upeu.moliselva.inventario.InventarioController;
import pe.edu.upeu.moliselva.inventario.InventarioDto;
import pe.edu.upeu.moliselva.inventario.InventarioService;
import pe.edu.upeu.moliselva.recepcion.adapter.in.web.RecepcionController;
import pe.edu.upeu.moliselva.recepcion.application.port.in.ConsultarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.CorregirRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.DecidirRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.EliminarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.in.RegistrarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RecepcionController.class, InventarioController.class,
    BitacoraController.class, AuthController.class, LoteController.class}, properties = {
        "app.jwt.secret=secreto-ficticio-exclusivo-de-pruebas-po01-2026",
        "app.jwt.issuer=moliselva-demo"
    })
@Import({SecurityConfig.class, ApiErrors.class})
class Po01SecurityTest {
    // Material ficticio: no procede de .env ni de credenciales de usuarios demo.
    private static final String OTRO_SECRETO = "otra-firma-ficticia-exclusiva-de-pruebas-po01";
    private static final String CUERPO_RECEPCION = """
        {"idProveedor":7,"pesoBrutoKg":1000,"taraKg":100,"pesoSacosKg":10,
         "humedadPct":14.5,"mermaLimpiezaKg":10}
        """;
    private static final String CUERPO_DECISION =
        "{\"decision\":\"ACEPTAR\",\"motivo\":\"Revisión de prueba\"}";

    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @MockitoBean AuthRepository usuarios;
    @MockitoBean AuthService autenticacion;
    @MockitoBean RegistrarRecepcionUseCase registrar;
    @MockitoBean DecidirRecepcionUseCase decidir;
    @MockitoBean CorregirRecepcionUseCase corregir;
    @MockitoBean EliminarRecepcionUseCase eliminar;
    @MockitoBean ConsultarRecepcionUseCase consultar;
    @MockitoBean InventarioService inventario;
    @MockitoBean BitacoraService bitacora;

    @MockitoBean ConsultarLotesUseCase consultarLotes;
    @MockitoBean PlanificarLoteUseCase planificarLotes;
    @MockitoBean GestionarInsumosLoteUseCase insumosLotes;
    @MockitoBean CambiarEstadoLoteUseCase estadoLotes;
    @MockitoBean FinalizarLoteUseCase finalizarLotes;

    enum TokenInvalido { FIRMA_INCORRECTA, VENCIDO, USUARIO_INEXISTENTE, USUARIO_INACTIVO, SUJETO_DISTINTO }

    @BeforeEach
    void prepararRespuestasSinBaseDeDatos() {
        var recepcion = new Recepcion(11, LocalDateTime.of(2026,11,3,12,0),7,"Proveedor de prueba",
            new BigDecimal("1000"),new BigDecimal("100"),new BigDecimal("10"),
            new BigDecimal("890.00"),new BigDecimal("14.5"),new BigDecimal("10"),
            EstadoRecepcion.ACEPTADA,new BigDecimal("880.000"),"usuario_prueba",null);
        when(registrar.registrar(any(),any())).thenReturn(recepcion);
        when(decidir.decidir(anyLong(),any(),anyString(),any())).thenReturn(recepcion);
        when(corregir.corregir(anyLong(),any(),any())).thenReturn(recepcion);
        when(consultar.listar(any())).thenReturn(List.of(recepcion));
        when(consultar.obtener(11)).thenReturn(recepcion);
        when(inventario.kardex()).thenReturn(List.of());
        when(inventario.stock()).thenReturn(new InventarioDto.Stock(new BigDecimal("880.000")));
        when(inventario.exportarCsv(7)).thenReturn("cantidadKg\n880.000\n".getBytes(StandardCharsets.UTF_8));
        when(bitacora.recientes()).thenReturn(List.of());
    }

    static Stream<Arguments> permisos() {
        String[] roles = {"ADMINISTRADOR","JEFE_PLANTA","OPERARIO","CONTADORA"};
        return Stream.of(roles).flatMap(rol -> Stream.of(
            caso(rol,"POST","/api/recepciones",CUERPO_RECEPCION,201,
                rol.equals("JEFE_PLANTA") || rol.equals("OPERARIO")),
            caso(rol,"PATCH","/api/recepciones/11/decision",CUERPO_DECISION,200,rol.equals("JEFE_PLANTA")),
            caso(rol,"PUT","/api/recepciones/11",CUERPO_RECEPCION,200,
                rol.equals("JEFE_PLANTA") || rol.equals("OPERARIO")),
            caso(rol,"DELETE","/api/recepciones/11",null,204,
                rol.equals("JEFE_PLANTA") || rol.equals("OPERARIO")),
            caso(rol,"GET","/api/recepciones",null,200,true),
            caso(rol,"GET","/api/recepciones/11",null,200,true),
            caso(rol,"GET","/api/inventario/maiz/kardex",null,200,true),
            caso(rol,"GET","/api/inventario/maiz/stock",null,200,true),
            caso(rol,"GET","/api/inventario/maiz/kardex/exportar",null,200,!rol.equals("OPERARIO")),
            caso(rol,"GET","/api/bitacora",null,200,
                rol.equals("ADMINISTRADOR") || rol.equals("JEFE_PLANTA"))
        ));
    }

    private static Arguments caso(String rol, String metodo, String ruta, String cuerpo, int exito, boolean permitido) {
        return Arguments.of(rol,metodo,ruta,cuerpo,permitido ? exito : 403);
    }

    @ParameterizedTest(name = "{0}: {1} {2} devuelve {4}")
    @MethodSource("permisos")
    void aplicaPermisosDelUsuarioEnCadaRuta(String rol, String metodo, String ruta,
            String cuerpo, int esperado) throws Exception {
        when(usuarios.findById(7)).thenReturn(Optional.of(usuario(rol,true)));
        var peticion = request(HttpMethod.valueOf(metodo),ruta)
            .header(HttpHeaders.AUTHORIZATION,"Bearer "+token(encoder,"usuario_prueba",Instant.now().plusSeconds(300)))
            .contentType(MediaType.APPLICATION_JSON);
        if (cuerpo != null) peticion.content(cuerpo);

        var respuesta = mvc.perform(peticion).andExpect(status().is(esperado));
        if (esperado == 403) {
            respuesta.andExpect(content().json("""
                {"codigo":"SIN_PERMISO","mensaje":"No tiene permiso para esta acción"}
                """,JsonCompareMode.STRICT));
            sinInteraccionesDeNegocio();
        } else {
            // Verificar que el permiso alcanzó el colaborador de la ruta concreta.
            switch (metodo+" "+ruta) {
                case "POST /api/recepciones" -> verify(registrar).registrar(any(),any());
                case "PATCH /api/recepciones/11/decision" -> verify(decidir).decidir(eq(11L),any(),anyString(),any());
                case "PUT /api/recepciones/11" -> verify(corregir).corregir(eq(11L),any(),any());
                case "DELETE /api/recepciones/11" -> verify(eliminar).eliminar(eq(11L),any());
                case "GET /api/recepciones" -> verify(consultar).listar(any());
                case "GET /api/recepciones/11" -> verify(consultar).obtener(11);
                case "GET /api/inventario/maiz/kardex" -> verify(inventario).kardex();
                case "GET /api/inventario/maiz/stock" -> verify(inventario).stock();
                case "GET /api/inventario/maiz/kardex/exportar" -> verify(inventario).exportarCsv(7);
                case "GET /api/bitacora" -> verify(bitacora).recientes();
                default -> throw new AssertionError("Ruta de prueba sin verificación");
            }
        }
    }

    @Test
    void rechazaPeticionSinTokenConErrorJson() throws Exception {
        noAutenticado(mvc.perform(get("/api/recepciones")));
        verifyNoInteractions(usuarios);
        sinInteraccionesDeNegocio();
    }

    @ParameterizedTest
    @EnumSource(TokenInvalido.class)
    void rechazaTokenInvalidoConErrorJson(TokenInvalido caso) throws Exception {
        when(usuarios.findById(7)).thenReturn(caso == TokenInvalido.USUARIO_INEXISTENTE
            ? Optional.empty() : Optional.of(usuario("OPERARIO",caso != TokenInvalido.USUARIO_INACTIVO)));
        JwtEncoder firmante = encoder;
        if (caso == TokenInvalido.FIRMA_INCORRECTA) {
            var clave = new SecretKeySpec(OTRO_SECRETO.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
            firmante = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        }
        String sujeto = caso == TokenInvalido.SUJETO_DISTINTO ? "otro_usuario" : "usuario_prueba";
        Instant vencimiento = caso == TokenInvalido.VENCIDO
            ? Instant.now().minusSeconds(300) : Instant.now().plusSeconds(300);

        noAutenticado(mvc.perform(get("/api/recepciones")
            .header(HttpHeaders.AUTHORIZATION,"Bearer "+token(firmante,sujeto,vencimiento))));
        sinInteraccionesDeNegocio();
        if (caso == TokenInvalido.FIRMA_INCORRECTA || caso == TokenInvalido.VENCIDO) {
            verifyNoInteractions(usuarios);
        }
    }

    @Test
    void loginEsAccesibleSinAuthorization() throws Exception {
        var entrada = new AuthDto.LoginRequest("usuario_prueba","clave-ficticia-solo-de-prueba");
        when(autenticacion.login(entrada)).thenReturn(new AuthDto.LoginResponse("token-simulado",
            new AuthDto.Usuario(7,"Usuario de prueba","usuario_prueba","OPERARIO",true)));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"usuario":"usuario_prueba","password":"clave-ficticia-solo-de-prueba"}
                """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.usuario").value("usuario_prueba"));
        verify(autenticacion).login(entrada);
        verifyNoInteractions(usuarios);
    }

    static Stream<Arguments> accesoComposicionLote() {
        return Stream.of("ADMINISTRADOR","JEFE_PLANTA","OPERARIO","CONTADORA")
            .flatMap(rol -> Stream.of("/api/lotes/11/insumos","/api/lotes/11/insumos/4")
                .map(ruta -> Arguments.of(rol,ruta)));
    }

    @ParameterizedTest(name = "Composición: {0} {1}")
    @MethodSource("accesoComposicionLote")
    void soloJefePuedeConsultarComposicionDeLote(String rol, String ruta) throws Exception {
        when(usuarios.findById(7)).thenReturn(Optional.of(usuario(rol,true)));
        when(consultarLotes.listarInsumos(11)).thenReturn(List.of());
        when(consultarLotes.obtenerInsumo(11,4)).thenReturn(new LoteInsumo(11,4,BigDecimal.ONE,null));
        var resultado = mvc.perform(get(ruta).header(HttpHeaders.AUTHORIZATION,
            "Bearer "+token(encoder,"usuario_prueba",Instant.now().plusSeconds(300))));
        if (rol.equals("JEFE_PLANTA")) resultado.andExpect(status().isOk());
        else {
            resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
            verifyNoInteractions(consultarLotes);
        }
    }

    @Test
    void operarioConservaConsultaDelResumenDeLotes() throws Exception {
        when(usuarios.findById(7)).thenReturn(Optional.of(usuario("OPERARIO",true)));
        when(consultarLotes.listar()).thenReturn(List.of());
        mvc.perform(get("/api/lotes").header(HttpHeaders.AUTHORIZATION,
            "Bearer "+token(encoder,"usuario_prueba",Instant.now().plusSeconds(300))))
            .andExpect(status().isOk());
        verify(consultarLotes).listar();
        verify(consultarLotes,never()).listarInsumos(anyLong());
    }

    private static AuthRepository.UsuarioRow usuario(String rol, boolean activo) {
        return new AuthRepository.UsuarioRow(7,"Usuario de prueba","usuario_prueba","hash-ficticio",rol,activo);
    }

    private static String token(JwtEncoder firmante, String sujeto, Instant vencimiento) {
        var claims = JwtClaimsSet.builder().issuer("moliselva-demo").subject(sujeto)
            .issuedAt(Instant.now().minusSeconds(600)).expiresAt(vencimiento)
            .claim("uid",7L)
            // El rol vigente debe obtenerse del repositorio, incluso si el token anuncia otro.
            .claim("rol","ADMINISTRADOR").build();
        return firmante.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
    }

    private void noAutenticado(ResultActions respuesta) throws Exception {
        respuesta.andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
            {"codigo":"NO_AUTENTICADO","mensaje":"Token ausente o inválido"}
            """,JsonCompareMode.STRICT));
    }

    private void sinInteraccionesDeNegocio() {
        verifyNoInteractions(registrar,decidir,corregir,eliminar,consultar,inventario,bitacora,autenticacion);
    }
}
