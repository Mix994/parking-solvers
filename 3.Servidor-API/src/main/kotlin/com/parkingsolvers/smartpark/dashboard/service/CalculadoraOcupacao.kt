package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.Turno
import com.parkingsolvers.smartpark.dashboard.dto.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.dto.OcupacaoPorTurnoItem
import com.parkingsolvers.smartpark.dashboard.dto.UsoVagaItem
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.visitante.model.Visitante
import java.time.Instant
import java.time.ZoneId

enum class PeriodoDashboard(val dias: Int?) {
    HOJE(1), ULTIMOS_7_DIAS(7), TUDO(null);

    /** Início do período (meia-noite no fuso do campus), ou null para TUDO. */
    fun inicio(agora: Instant, zone: ZoneId): Instant? {
        val qtd = dias ?: return null
        return agora.atZone(zone).toLocalDate().minusDays((qtd - 1).toLong()).atStartOfDay(zone).toInstant()
    }
}

/** Cálculo puro das métricas de ocupação (porte de CalculadoraOcupacao do app). */
class CalculadoraOcupacao(private val zone: ZoneId) {

    fun calcular(
        servidores: List<Servidor>,
        veiculos: List<Veiculo>,
        movimentacoes: List<RegistroMovimentacao>,
        visitantes: List<Visitante>,
        configuracao: ConfiguracaoEstacionamento,
        agora: Instant,
        inicioPeriodo: Instant?
    ): DashboardOcupacaoResponse {
        val capTotal = configuracao.capacidadeTotal
        val capExclusivas = configuracao.capacidadeExclusivas
        val capEletricas = configuracao.capacidadeCarregamentoEletrico

        val movAbertas = movimentacoes.filter { it.dataHoraSaida == null }
        val visAbertos = visitantes.filter { it.dataHoraSaida == null }
        val estacionadosAgora = movAbertas.size + visAbertos.size
        val exclusivasAgora = movAbertas.count { it.tipoVaga.exclusiva } + visAbertos.count { it.tipoVaga.exclusiva }
        val eletricasAgora = movAbertas.count { it.tipoVaga.carregamentoEletrico } + visAbertos.count { it.tipoVaga.carregamentoEletrico }

        val movPeriodo = movimentacoes.filter { inicioPeriodo == null || !it.dataHoraEntrada.isBefore(inicioPeriodo) }
        val visPeriodo = visitantes.filter { inicioPeriodo == null || !it.dataHoraEntrada.isBefore(inicioPeriodo) }

        val dias = (movPeriodo.map { it.dataHoraEntrada } + visPeriodo.map { it.dataHoraEntrada })
            .map { it.atZone(zone).toLocalDate() }.toSet().size.coerceAtLeast(1)

        val porTurno = Turno.entries.map { turno ->
            val entradas = movPeriodo.count { it.turno == turno } + visPeriodo.count { it.turno == turno }
            val agoraTurno = movAbertas.count { it.turno == turno } + visAbertos.count { it.turno == turno }
            val media = entradas.toDouble() / dias
            OcupacaoPorTurnoItem(turno, entradas, agoraTurno, media, percentual(media, capTotal))
        }

        val porTipo = TipoVaga.entries.map { tipo ->
            UsoVagaItem(
                tipo,
                movAbertas.count { it.tipoVaga == tipo } + visAbertos.count { it.tipoVaga == tipo },
                movPeriodo.count { it.tipoVaga == tipo } + visPeriodo.count { it.tipoVaga == tipo }
            )
        }

        return DashboardOcupacaoResponse(
            capacidadeTotal = capTotal,
            capacidadeExclusivas = capExclusivas,
            capacidadeCarregamentoEletrico = capEletricas,
            veiculosNoCampusAgora = estacionadosAgora,
            visitantesNoCampusAgora = visAbertos.size,
            vagasExclusivasOcupadasAgora = exclusivasAgora,
            vagasEletricasOcupadasAgora = eletricasAgora,
            percentualOcupacaoAtual = percentual(estacionadosAgora.toDouble(), capTotal),
            percentualExclusivasAtual = percentual(exclusivasAgora.toDouble(), capExclusivas),
            percentualEletricasAtual = percentual(eletricasAgora.toDouble(), capEletricas),
            totalServidoresAtivos = servidores.count { it.ativo },
            totalVeiculosAtivos = veiculos.count { it.ativo },
            totalTemporariosVigentes = veiculos.count {
                it.ativo && it.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO && it.estaDentroDaValidade(agora)
            },
            totalEntradasPeriodo = movPeriodo.size + visPeriodo.size,
            totalVisitantesPeriodo = visPeriodo.size,
            diasComMovimento = dias,
            ocupacaoPorTurno = porTurno,
            usoPorTipoVaga = porTipo
        )
    }

    private fun percentual(atual: Double, capacidade: Int): Double =
        if (capacidade <= 0) 0.0 else (atual / capacidade).coerceIn(0.0, 1.0)
}
