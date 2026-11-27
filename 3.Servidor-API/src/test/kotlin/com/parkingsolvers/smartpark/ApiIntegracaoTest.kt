package com.parkingsolvers.smartpark

import com.parkingsolvers.smartpark.auth.dto.LoginRequest
import com.parkingsolvers.smartpark.auth.dto.RefreshRequest
import com.parkingsolvers.smartpark.auth.dto.TokenResponse
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoRequest
import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoResponse
import com.parkingsolvers.smartpark.dashboard.dto.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.movimentacao.dto.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.servidor.dto.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.ServidorResponse
import com.parkingsolvers.smartpark.usuario.dto.AtualizarUsuarioAdminRequest
import com.parkingsolvers.smartpark.usuario.dto.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.UsuarioResponse
import com.parkingsolvers.smartpark.veiculo.dto.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoResponse
import com.parkingsolvers.smartpark.visitante.dto.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.VisitanteResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Teste de integração ponta a ponta contra PostgreSQL real (Testcontainers):
 * valida migração Flyway V1, hibernate ddl-auto=validate, segurança JWT, perfis e todos os domínios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Testcontainers
class ApiIntegracaoTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
    }

    @Autowired
    lateinit var rest: TestRestTemplate

    @LocalServerPort
    var port: Int = 0

    private fun <T> authEntity(token: String, body: T? = null): HttpEntity<T> {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, headers)
    }

    private fun login(login: String, senha: String): TokenResponse {
        val resp = rest.postForEntity("/auth/login", LoginRequest(login, senha), TokenResponse::class.java)
        assertEquals(HttpStatus.OK, resp.statusCode, "Falha no login de $login")
        return requireNotNull(resp.body)
    }

    @Test
    fun `fluxo completo da API com Flyway e PostgreSQL real`() {
        // 1. Saúde e OpenAPI acessíveis sem autenticação
        assertEquals(HttpStatus.OK, rest.getForEntity("/actuator/health", String::class.java).statusCode)
        assertEquals(HttpStatus.OK, rest.getForEntity("/v3/api-docs", String::class.java).statusCode)

        // Rota protegida sem token -> 401
        assertEquals(HttpStatus.UNAUTHORIZED, rest.getForEntity("/servidores", String::class.java).statusCode)

        // 2. Login do admin criado pelo AdminInicialDev + /auth/me + /auth/refresh
        val adminTokens = login("admin", "admin123")
        val adminToken = adminTokens.accessToken

        val meResp = rest.exchange("/auth/me", HttpMethod.GET, authEntity<Unit>(adminToken), UsuarioSessao::class.java)
        assertEquals(HttpStatus.OK, meResp.statusCode)
        assertEquals(PerfilUsuario.ADMIN, meResp.body?.perfil)

        val refreshResp = rest.postForEntity("/auth/refresh", RefreshRequest(adminTokens.refreshToken), TokenResponse::class.java)
        assertEquals(HttpStatus.OK, refreshResp.statusCode)

        // 3. Configuração do estacionamento (padrão 60/10/2 inserido pelo V1__schema.sql)
        val cfgInicial = rest.exchange(
            "/configuracao-estacionamento", HttpMethod.GET, authEntity<Unit>(adminToken), ConfiguracaoResponse::class.java
        )
        assertEquals(HttpStatus.OK, cfgInicial.statusCode)
        assertEquals(60, cfgInicial.body?.capacidadeTotal)
        assertEquals(2, cfgInicial.body?.capacidadeCarregamentoEletrico)

        // Reduz vaga elétrica para 2 (mantém) e valida PUT
        val cfgAtualizada = rest.exchange(
            "/configuracao-estacionamento",
            HttpMethod.PUT,
            authEntity(adminToken, ConfiguracaoRequest(50, 8, 2)),
            ConfiguracaoResponse::class.java
        )
        assertEquals(HttpStatus.OK, cfgAtualizada.statusCode)
        assertEquals(50, cfgAtualizada.body?.capacidadeTotal)

        // 4. Criar e editar servidor (PUT /servidores/{id})
        val servidorResp = rest.exchange(
            "/servidores",
            HttpMethod.POST,
            authEntity(adminToken, ServidorRequest("Ana Paula", "sp3010", "ana@ifsp.edu.br", "(11) 98888-7777", "TI")),
            ServidorResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, servidorResp.statusCode)
        val servidor = requireNotNull(servidorResp.body)
        assertEquals("SP3010", servidor.prontuario)

        val servidorEditado = rest.exchange(
            "/servidores/${servidor.id}",
            HttpMethod.PUT,
            authEntity(adminToken, ServidorRequest("Ana Paula Costa", "sp3010", "ana.costa@ifsp.edu.br", "(11) 98888-9999", "Redes")),
            ServidorResponse::class.java
        )
        assertEquals(HttpStatus.OK, servidorEditado.statusCode)
        assertEquals("ana.costa@ifsp.edu.br", servidorEditado.body?.email)
        assertEquals("Redes", servidorEditado.body?.setor)

        // 5. Criar usuários PORTARIA e SERVIDOR + Admin editar email/senha de porteiro (PUT /usuarios/{id})
        val portariaUser = rest.exchange(
            "/usuarios",
            HttpMethod.POST,
            authEntity(adminToken, UsuarioRequest("Carlos Porteiro", "portaria1", "senha123", "senha123", PerfilUsuario.PORTARIA)),
            UsuarioResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, portariaUser.statusCode)
        val porteiroId = requireNotNull(portariaUser.body?.id)

        val porteiroEditado = rest.exchange(
            "/usuarios/$porteiroId",
            HttpMethod.PUT,
            authEntity(
                adminToken,
                AtualizarUsuarioAdminRequest(
                    nome = "Carlos Porteiro",
                    login = "carlos.portaria@ifsp.edu.br",
                    novaSenha = "novaSenha456",
                    confirmacaoNovaSenha = "novaSenha456",
                    perfil = PerfilUsuario.PORTARIA
                )
            ),
            UsuarioResponse::class.java
        )
        assertEquals(HttpStatus.OK, porteiroEditado.statusCode)
        assertEquals("carlos.portaria@ifsp.edu.br", porteiroEditado.body?.login)

        val servidorUser = rest.exchange(
            "/usuarios",
            HttpMethod.POST,
            authEntity(adminToken, UsuarioRequest("Ana Paula", "ana.paula", "senha123", "senha123", PerfilUsuario.SERVIDOR, servidor.id)),
            UsuarioResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, servidorUser.statusCode)

        // Login do servidor usando o prontuário + atualização em /usuarios/me
        val servidorToken = login("sp3010", "senha123").accessToken
        val meusDados = rest.exchange(
            "/usuarios/me",
            HttpMethod.PUT,
            authEntity(servidorToken, AtualizarUsuarioRequest("Ana Paula Silva", "ana.paula")),
            UsuarioResponse::class.java
        )
        assertEquals(HttpStatus.OK, meusDados.statusCode)
        assertEquals("Ana Paula Silva", meusDados.body?.nome)

        // Servidor não pode listar todos os usuários (403)
        assertEquals(
            HttpStatus.FORBIDDEN,
            rest.exchange("/usuarios", HttpMethod.GET, authEntity<Unit>(servidorToken), String::class.java).statusCode
        )

        // 6. Servidor cadastra seu próprio veículo
        val veiculoResp = rest.exchange(
            "/veiculos",
            HttpMethod.POST,
            authEntity(servidorToken, VeiculoRequest(servidor.id, "ABC1D23", "Onix", "Prata")),
            VeiculoResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, veiculoResp.statusCode)
        val veiculo = requireNotNull(veiculoResp.body)
        assertEquals("ABC1D23", veiculo.placa)

        // Portaria entra com novo email/senha alterados pelo admin e faz busca parcial + consulta de placa
        val portariaToken = login("carlos.portaria@ifsp.edu.br", "novaSenha456").accessToken
        val buscaParcial = rest.exchange(
            "/veiculos/buscar?termo=1d2",
            HttpMethod.GET,
            authEntity<Unit>(portariaToken),
            Array<VeiculoResponse>::class.java
        )
        assertEquals(HttpStatus.OK, buscaParcial.statusCode)
        assertEquals(1, buscaParcial.body?.size)
        assertEquals("ABC1D23", buscaParcial.body?.first()?.placa)

        val consultaResp = rest.exchange(
            "/veiculos/placa/abc1d23",
            HttpMethod.GET,
            authEntity<Unit>(portariaToken),
            ResultadoConsultaPlaca::class.java
        )
        assertEquals(HttpStatus.OK, consultaResp.statusCode)
        assertEquals(StatusAutorizacao.AUTORIZADO_PERMANENTE, consultaResp.body?.status)

        // 7. Portaria registra entrada do servidor em vaga elétrica (1 de 2)
        val entradaResp = rest.exchange(
            "/movimentacoes/entrada",
            HttpMethod.POST,
            authEntity(portariaToken, EntradaRequest("ABC1D23", TipoVaga.CARREGAMENTO_ELETRICO)),
            MovimentacaoResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, entradaResp.statusCode)
        val movimentacao = requireNotNull(entradaResp.body)
        assertTrue(movimentacao.emAberto)

        // Entrada duplicada da mesma placa -> 409
        assertEquals(
            HttpStatus.CONFLICT,
            rest.exchange(
                "/movimentacoes/entrada",
                HttpMethod.POST,
                authEntity(portariaToken, EntradaRequest("ABC1D23")),
                String::class.java
            ).statusCode
        )

        // 8. Portaria registra entrada de visitante em vaga elétrica (2 de 2)
        val vis1Resp = rest.exchange(
            "/visitantes",
            HttpMethod.POST,
            authEntity(
                portariaToken,
                VisitanteRequest(
                    "João Visitante", "12345678900", "Empresa Z", "(11) 97777-6666",
                    "XYZ-9876", "Civic", "Preto", "Reunião", "Diretoria", TipoVaga.CARREGAMENTO_ELETRICO
                )
            ),
            VisitanteResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, vis1Resp.statusCode)
        val vis1 = requireNotNull(vis1Resp.body)
        assertEquals("Carlos Porteiro", vis1.registradoPor)

        // 3º veículo tentando vaga elétrica (lotada: 2 de 2) -> 409
        val visLotadoResp = rest.exchange(
            "/visitantes",
            HttpMethod.POST,
            authEntity(
                portariaToken,
                VisitanteRequest(
                    "Maria Visitante", "98765432100", "Empresa W", "(11) 96666-5555",
                    "QWE-4321", "Kwid", "Branco", "Entrega", "Almoxarifado", TipoVaga.CARREGAMENTO_ELETRICO
                )
            ),
            String::class.java
        )
        assertEquals(HttpStatus.CONFLICT, visLotadoResp.statusCode)

        // 9. Dashboard reflete os 2 veículos (1 servidor + 1 visitante, 2 vagas elétricas ocupadas)
        val dashResp = rest.exchange(
            "/dashboard/ocupacao?periodo=HOJE",
            HttpMethod.GET,
            authEntity<Unit>(servidorToken),
            DashboardOcupacaoResponse::class.java
        )
        assertEquals(HttpStatus.OK, dashResp.statusCode)
        val dash = requireNotNull(dashResp.body)
        assertEquals(2, dash.veiculosNoCampusAgora)
        assertEquals(1, dash.visitantesNoCampusAgora)
        assertEquals(2, dash.vagasEletricasOcupadasAgora)
        assertEquals(1, dash.totalServidoresAtivos)
        assertEquals(1, dash.totalVeiculosAtivos)

        // 10. Registrar saídas e conferir ocupação zerada
        val saidaMov = rest.exchange(
            "/movimentacoes/${movimentacao.id}/saida",
            HttpMethod.POST,
            authEntity<Unit>(portariaToken),
            MovimentacaoResponse::class.java
        )
        assertEquals(HttpStatus.OK, saidaMov.statusCode)
        assertNotNull(saidaMov.body?.dataHoraSaida)

        val saidaVis = rest.exchange(
            "/visitantes/${vis1.id}/saida",
            HttpMethod.POST,
            authEntity<Unit>(portariaToken),
            VisitanteResponse::class.java
        )
        assertEquals(HttpStatus.OK, saidaVis.statusCode)
        assertNotNull(saidaVis.body?.dataHoraSaida)

        val dashFinal = rest.exchange(
            "/dashboard/ocupacao?periodo=HOJE",
            HttpMethod.GET,
            authEntity<Unit>(adminToken),
            DashboardOcupacaoResponse::class.java
        ).body
        assertEquals(0, dashFinal?.veiculosNoCampusAgora)

        // 11. Servidor simula estacionamento no próprio veículo e dá baixa na saída
        val simEntrada = rest.exchange(
            "/movimentacoes/entrada",
            HttpMethod.POST,
            authEntity(servidorToken, EntradaRequest("ABC1D23", TipoVaga.EXCLUSIVA_PCD)),
            MovimentacaoResponse::class.java
        )
        assertEquals(HttpStatus.CREATED, simEntrada.statusCode)
        val simMov = requireNotNull(simEntrada.body)
        assertTrue(simMov.emAberto)
        assertEquals(TipoVaga.EXCLUSIVA_PCD, simMov.tipoVaga)

        // GET /movimentacoes/minhas-abertas retorna para SERVIDOR; /no-estacionamento continua 403
        val minhasAbertas = rest.exchange(
            "/movimentacoes/minhas-abertas",
            HttpMethod.GET,
            authEntity<Unit>(servidorToken),
            Array<MovimentacaoResponse>::class.java
        )
        assertEquals(HttpStatus.OK, minhasAbertas.statusCode)
        assertEquals(1, minhasAbertas.body?.size)
        assertEquals(simMov.id, minhasAbertas.body?.first()?.id)

        assertEquals(
            HttpStatus.FORBIDDEN,
            rest.exchange("/movimentacoes/no-estacionamento", HttpMethod.GET, authEntity<Unit>(servidorToken), String::class.java).statusCode
        )

        // Portaria enxerga a entrada simulada em /movimentacoes/no-estacionamento
        val portariaAbertas = rest.exchange(
            "/movimentacoes/no-estacionamento",
            HttpMethod.GET,
            authEntity<Unit>(portariaToken),
            Array<MovimentacaoResponse>::class.java
        )
        assertEquals(HttpStatus.OK, portariaAbertas.statusCode)
        assertTrue(portariaAbertas.body?.any { it.id == simMov.id && it.placa == "ABC1D23" } == true)

        // Servidor não pode simular entrada de placa não pertencente a ele -> 403
        assertEquals(
            HttpStatus.FORBIDDEN,
            rest.exchange(
                "/movimentacoes/entrada",
                HttpMethod.POST,
                authEntity(servidorToken, EntradaRequest("ZZZ9999", TipoVaga.COMUM, permitirNaoAutorizado = true)),
                String::class.java
            ).statusCode
        )

        // Servidor registra saída da simulação
        val simSaida = rest.exchange(
            "/movimentacoes/${simMov.id}/saida",
            HttpMethod.POST,
            authEntity<Unit>(servidorToken),
            MovimentacaoResponse::class.java
        )
        assertEquals(HttpStatus.OK, simSaida.statusCode)
        assertNotNull(simSaida.body?.dataHoraSaida)

        // 12. Stream SSE em tempo real: exige JWT e entrega evento imediato ao simular nova entrada
        val sseSemAuth = rest.getForEntity("/eventos/stream", String::class.java)
        assertEquals(HttpStatus.UNAUTHORIZED, sseSemAuth.statusCode)

        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build()
        val reqSse = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/eventos/stream"))
            .header("Authorization", "Bearer $portariaToken")
            .header("Accept", "text/event-stream")
            .GET()
            .build()
        val sseResp = httpClient.send(reqSse, HttpResponse.BodyHandlers.ofInputStream())
        assertEquals(200, sseResp.statusCode())

        val executor = Executors.newSingleThreadExecutor()
        try {
            val reader = BufferedReader(InputStreamReader(sseResp.body(), StandardCharsets.UTF_8))
            val conectadoOk = executor.submit<Boolean> {
                while (true) {
                    val linha = reader.readLine() ?: return@submit false
                    if (linha.startsWith("event:conectado")) return@submit true
                }
                @Suppress("UNREACHABLE_CODE")
                false
            }.get(5, TimeUnit.SECONDS)
            assertTrue(conectadoOk, "Stream SSE não enviou evento inicial conectado")

            val simEntrada2 = rest.exchange(
                "/movimentacoes/entrada",
                HttpMethod.POST,
                authEntity(servidorToken, EntradaRequest("ABC1D23", TipoVaga.EXCLUSIVA_IDOSO)),
                MovimentacaoResponse::class.java
            )
            assertEquals(HttpStatus.CREATED, simEntrada2.statusCode)

            val linhasRecebidas = executor.submit<String> {
                val sb = StringBuilder()
                while (true) {
                    val linha = reader.readLine() ?: break
                    sb.append(linha).append('\n')
                    if (linha.contains("MOVIMENTACAO_ENTRADA") && linha.contains("ABC1D23")) {
                        break
                    }
                }
                sb.toString()
            }.get(5, TimeUnit.SECONDS)
            assertTrue(linhasRecebidas.contains("event:atualizacao"), "Esperado evento atualizacao em:\n$linhasRecebidas")
            assertTrue(linhasRecebidas.contains("MOVIMENTACAO_ENTRADA"))
            assertTrue(linhasRecebidas.contains("ABC1D23"))
        } finally {
            runCatching { sseResp.body().close() }
            executor.shutdownNow()
        }
    }
}
