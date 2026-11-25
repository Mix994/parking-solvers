package com.parkingsolvers.smartpark.veiculo

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.PlacaInvalidaException
import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.Turno
import com.parkingsolvers.smartpark.compartilhado.ValidacaoException
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.dto.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import com.parkingsolvers.smartpark.visitante.dto.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.model.Visitante
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import com.parkingsolvers.smartpark.visitante.service.VisitanteService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.access.AccessDeniedException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VeiculoVisitanteTest {

    private val agora = Instant.parse("2026-03-10T15:00:00Z") // 12:00 em São Paulo -> TARDE
    private val relogio = RelogioNegocio(Clock.fixed(agora, ZoneOffset.UTC), "America/Sao_Paulo")

    private val veiculos = mock(VeiculoRepository::class.java)
    private val servidores = mock(ServidorRepository::class.java)
    private val visitantes = mock(VisitanteRepository::class.java)
    private val movimentacoes = mock(MovimentacaoRepository::class.java)
    private val configuracao = mock(ConfiguracaoEstacionamentoRepository::class.java)

    private val veiculoService = VeiculoService(veiculos, servidores, relogio)
    private val visitanteService = VisitanteService(visitantes, movimentacoes, configuracao, relogio)

    private val servidorAtivo = Servidor(id = 1, nome = "Ana", prontuario = "SP1", ativo = true)
    private val admin = UsuarioSessao(9, "Adm", "adm", PerfilUsuario.ADMIN)

    init {
        `when`(servidores.findById(1L)).thenReturn(Optional.of(servidorAtivo))
        `when`(veiculos.save(any(Veiculo::class.java))).thenAnswer { it.arguments[0] }
        `when`(visitantes.save(any(Visitante::class.java))).thenAnswer { it.arguments[0] }
        `when`(configuracao.buscarComLock()).thenReturn(ConfiguracaoEstacionamento())
    }

    private fun temporario(inicio: Instant?, fim: Instant?) =
        Veiculo(1, 1, "ABC1234", "Gol", "Prata", TipoCadastroVeiculo.TEMPORARIO, inicio, fim)

    @Test
    fun `turno por horario de Sao Paulo`() {
        assertEquals(Turno.TARDE, relogio.turnoDe(agora))
        assertEquals(Turno.NOITE, relogio.turnoDe(Instant.parse("2026-03-10T02:00:00Z"))) // 23h do dia anterior
        assertEquals(Turno.MANHA, RelogioNegocio.turnoPorHora(6))
        assertEquals(Turno.NOITE, RelogioNegocio.turnoPorHora(5))
    }

    @Test
    fun `autorizacao de veiculos`() {
        fun status(v: Veiculo, s: Servidor? = servidorAtivo) = VeiculoService.avaliarAutorizacao(v, s, agora, relogio).status

        assertEquals(StatusAutorizacao.AUTORIZADO_PERMANENTE, status(Veiculo(1, 1, "ABC1234", "G", "P")))
        assertEquals(StatusAutorizacao.VEICULO_INATIVO, status(Veiculo(1, 1, "ABC1234", "G", "P", ativo = false)))
        assertEquals(StatusAutorizacao.SERVIDOR_INATIVO, status(Veiculo(1, 1, "ABC1234", "G", "P"), Servidor(ativo = false)))
        assertEquals(StatusAutorizacao.AUTORIZADO_TEMPORARIO, status(temporario(agora.minusSeconds(60), agora.plusSeconds(60))))
        assertEquals(StatusAutorizacao.TEMPORARIO_EXPIRADO, status(temporario(agora.minusSeconds(120), agora.minusSeconds(60))))
        assertEquals(StatusAutorizacao.TEMPORARIO_FORA_VIGENCIA, status(temporario(agora.plusSeconds(60), agora.plusSeconds(120))))
    }

    @Test
    fun `consulta de placa nao cadastrada e placa invalida`() {
        assertEquals(StatusAutorizacao.NAO_CADASTRADO, veiculoService.consultarPlaca("xyz-9876").status)
        assertFailsWith<PlacaInvalidaException> { veiculoService.consultarPlaca("123") }
    }

    @Test
    fun `cadastro de veiculo valida, normaliza placa e checa duplicidade`() {
        assertFailsWith<ValidacaoException> { veiculoService.cadastrar(VeiculoRequest(servidorId = 1), admin) }
        val ok = veiculoService.cadastrar(VeiculoRequest(1, "abc-1234", "Gol", "Prata"), admin)
        assertEquals("ABC1234", ok.placa)
        `when`(veiculos.findByPlaca("ABC1234")).thenReturn(Veiculo())
        assertFailsWith<ConflitoException> { veiculoService.cadastrar(VeiculoRequest(1, "ABC1234", "Gol", "Prata"), admin) }
    }

    @Test
    fun `periodo temporario precisa ser coerente`() {
        val req = VeiculoRequest(1, "ABC1D23", "Gol", "Prata", TipoCadastroVeiculo.TEMPORARIO, agora, agora.minusSeconds(1))
        val e = assertFailsWith<ValidacaoException> { veiculoService.cadastrar(req, admin) }
        assertEquals(setOf("validade"), e.campos.keys)
    }

    @Test
    fun `servidor so gerencia os proprios veiculos e portaria nao gerencia`() {
        val req = VeiculoRequest(1, "ABC1234", "Gol", "Prata")
        val outro = UsuarioSessao(2, "Outro", "SP2", PerfilUsuario.SERVIDOR, servidorId = 2)
        val dono = UsuarioSessao(3, "Ana", "SP1", PerfilUsuario.SERVIDOR, servidorId = 1)
        val portaria = UsuarioSessao(4, "Port", "p", PerfilUsuario.PORTARIA)
        assertFailsWith<AccessDeniedException> { veiculoService.cadastrar(req, outro) }
        assertFailsWith<AccessDeniedException> { veiculoService.cadastrar(req, portaria) }
        assertEquals("ABC1234", veiculoService.cadastrar(req, dono).placa)
    }

    private fun visitanteReq(tipo: TipoVaga = TipoVaga.COMUM) = VisitanteRequest(
        "Joao Silva", "12345678900", "Empresa X", "(11) 99999-8888", "ABC-1234", "Uno", "Branco",
        "Reuniao", "Diretoria", tipo
    )

    @Test
    fun `entrada de visitante registra turno e bloqueia placa ja no campus`() {
        val r = visitanteService.registrarEntrada(visitanteReq(), "")
        assertEquals("ABC1234", r.placa)
        assertEquals(Turno.TARDE, r.turno)
        assertEquals("Portaria", r.registradoPor)

        `when`(visitantes.findFirstByPlacaAndDataHoraSaidaIsNull("ABC1234")).thenReturn(Visitante())
        assertFailsWith<ConflitoException> { visitanteService.registrarEntrada(visitanteReq(), "Port") }
    }

    @Test
    fun `vagas eletricas somam visitantes e movimentacoes`() {
        val tipo = TipoVaga.CARREGAMENTO_ELETRICO
        `when`(visitantes.countByTipoVagaAndDataHoraSaidaIsNull(tipo)).thenReturn(1)
        `when`(movimentacoes.countByTipoVagaAndDataHoraSaidaIsNull(tipo)).thenReturn(0)
        visitanteService.registrarEntrada(visitanteReq(tipo), "Port") // 1 de 2 ocupadas

        `when`(movimentacoes.countByTipoVagaAndDataHoraSaidaIsNull(tipo)).thenReturn(1) // agora 2 de 2
        assertFailsWith<ConflitoException> { visitanteService.registrarEntrada(visitanteReq(tipo), "Port") }
    }

    @Test
    fun `saida de visitante so uma vez`() {
        val v = Visitante(id = 5, dataHoraEntrada = agora.minusSeconds(3600))
        `when`(visitantes.findById(5L)).thenReturn(Optional.of(v))
        assertEquals(agora, visitanteService.registrarSaida(5).dataHoraSaida)
        assertFailsWith<ConflitoException> { visitanteService.registrarSaida(5) }
    }
}
