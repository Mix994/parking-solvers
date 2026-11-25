package com.parkingsolvers.smartpark.dashboard

import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.Turno
import com.parkingsolvers.smartpark.compartilhado.ValidacaoException
import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoRequest
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import com.parkingsolvers.smartpark.dashboard.service.CalculadoraOcupacao
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import com.parkingsolvers.smartpark.dashboard.service.PeriodoDashboard
import com.parkingsolvers.smartpark.movimentacao.dto.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import com.parkingsolvers.smartpark.visitante.model.Visitante
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MovimentacaoDashboardTest {

    private val agora = Instant.parse("2026-03-10T15:00:00Z") // 12h em São Paulo, TARDE
    private val relogio = RelogioNegocio(Clock.fixed(agora, ZoneOffset.UTC), "America/Sao_Paulo")
    private val sp = ZoneId.of("America/Sao_Paulo")

    private val movs = mock(MovimentacaoRepository::class.java)
    private val veiculos = mock(VeiculoRepository::class.java)
    private val servidores = mock(ServidorRepository::class.java)
    private val visitantes = mock(VisitanteRepository::class.java)
    private val config = mock(ConfiguracaoEstacionamentoRepository::class.java)
    private val veiculoService = VeiculoService(veiculos, servidores, relogio)
    private val service = MovimentacaoService(movs, veiculos, servidores, visitantes, config, veiculoService, relogio)

    init {
        `when`(config.buscarComLock()).thenReturn(ConfiguracaoEstacionamento())
        `when`(movs.save(any(RegistroMovimentacao::class.java))).thenAnswer { it.arguments[0] }
        `when`(servidores.findById(1L)).thenReturn(Optional.of(Servidor(id = 1, nome = "Ana", ativo = true)))
        `when`(veiculos.findByPlaca("ABC1234")).thenReturn(Veiculo(id = 7, servidorId = 1, placa = "ABC1234", modelo = "Gol", cor = "Prata"))
        `when`(veiculos.findAllById(setOf(7L))).thenReturn(listOf(Veiculo(id = 7, servidorId = 1, placa = "ABC1234", modelo = "Gol", cor = "Prata")))
        `when`(servidores.findAllById(setOf(1L))).thenReturn(listOf(Servidor(id = 1, nome = "Ana")))
    }

    @Test
    fun `entrada autorizada vincula veiculo e define turno`() {
        val r = service.registrarEntrada(EntradaRequest("abc-1234"))
        assertEquals(true, r.autorizado)
        assertEquals(7L, r.veiculoId)
        assertEquals("Ana", r.servidorNome)
        assertEquals("Gol • Prata", r.modeloCor)
        assertEquals(Turno.TARDE, r.turno)
    }

    @Test
    fun `placa nao cadastrada exige permitirNaoAutorizado`() {
        assertFailsWith<RegraDeNegocioException> { service.registrarEntrada(EntradaRequest("XYZ9876")) }
        val r = service.registrarEntrada(EntradaRequest("XYZ9876", permitirNaoAutorizado = true))
        assertEquals(false, r.autorizado)
        assertEquals("Acesso avulso / visitante", r.servidorNome)
    }

    @Test
    fun `placa ja dentro gera conflito e saida so uma vez`() {
        `when`(movs.findFirstByPlacaRegistradaAndDataHoraSaidaIsNull("ABC1234")).thenReturn(RegistroMovimentacao())
        assertFailsWith<ConflitoException> { service.registrarEntrada(EntradaRequest("ABC1234")) }

        val reg = RegistroMovimentacao(id = 3, placaRegistrada = "ABC1234", dataHoraEntrada = agora.minusSeconds(60))
        `when`(movs.findById(3L)).thenReturn(Optional.of(reg))
        assertEquals(agora, service.registrarSaida(3).dataHoraSaida)
        assertFailsWith<ConflitoException> { service.registrarSaida(3) }
    }

    @Test
    fun `vaga eletrica lotada considera visitantes`() {
        val tipo = TipoVaga.CARREGAMENTO_ELETRICO
        `when`(movs.countByTipoVagaAndDataHoraSaidaIsNull(tipo)).thenReturn(1)
        `when`(visitantes.countByTipoVagaAndDataHoraSaidaIsNull(tipo)).thenReturn(1)
        assertFailsWith<ConflitoException> { service.registrarEntrada(EntradaRequest("ABC1234", tipo)) }
    }

    @Test
    fun `calculadora de ocupacao`() {
        val calc = CalculadoraOcupacao(sp)
        val cfg = ConfiguracaoEstacionamento(capacidadeTotal = 10, capacidadeExclusivas = 2, capacidadeCarregamentoEletrico = 2)
        val movimentacoes = listOf(
            RegistroMovimentacao(1, 7, "AAA1111", true, agora, null, Turno.TARDE, TipoVaga.COMUM),
            RegistroMovimentacao(2, 7, "BBB2222", true, agora, null, Turno.TARDE, TipoVaga.EXCLUSIVA_PCD),
            RegistroMovimentacao(3, 7, "CCC3333", true, agora.minusSeconds(86400), agora, Turno.MANHA, TipoVaga.COMUM)
        )
        val vis = listOf(Visitante(id = 1, tipoVaga = TipoVaga.CARREGAMENTO_ELETRICO, turno = Turno.TARDE, dataHoraEntrada = agora))
        val r = calc.calcular(emptyList(), emptyList(), movimentacoes, vis, cfg, agora, null)

        assertEquals(3, r.veiculosNoCampusAgora)
        assertEquals(1, r.visitantesNoCampusAgora)
        assertEquals(1, r.vagasExclusivasOcupadasAgora)
        assertEquals(1, r.vagasEletricasOcupadasAgora)
        assertEquals(0.3, r.percentualOcupacaoAtual, 1e-9)
        assertEquals(4, r.totalEntradasPeriodo)
        assertEquals(2, r.diasComMovimento)
        assertEquals(7, r.vagasLivresAgora)
        assertEquals(1, r.vagasComunsOcupadasAgora)
    }

    @Test
    fun `periodo hoje comeca a meia-noite do fuso do campus`() {
        // 15:00Z = 12:00 SP -> meia-noite SP = 03:00Z
        assertEquals(Instant.parse("2026-03-10T03:00:00Z"), PeriodoDashboard.HOJE.inicio(agora, sp))
        assertEquals(Instant.parse("2026-03-04T03:00:00Z"), PeriodoDashboard.ULTIMOS_7_DIAS.inicio(agora, sp))
        assertEquals(null, PeriodoDashboard.TUDO.inicio(agora, sp))
    }

    @Test
    fun `configuracao valida soma de vagas`() {
        val dashboard = DashboardService(servidores, veiculos, movs, visitantes, config, relogio)
        assertFailsWith<ValidacaoException> { dashboard.atualizarConfiguracao(ConfiguracaoRequest(0, 0, 0)) }
        assertFailsWith<ValidacaoException> { dashboard.atualizarConfiguracao(ConfiguracaoRequest(10, 8, 3)) }
        `when`(config.save(any(ConfiguracaoEstacionamento::class.java))).thenAnswer { it.arguments[0] }
        assertEquals(20, dashboard.atualizarConfiguracao(ConfiguracaoRequest(20, 5, 2)).capacidadeTotal)
    }
}
