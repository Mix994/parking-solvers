package com.parkingsolvers.smartpark.dashboard.dto

import com.parkingsolvers.smartpark.compartilhado.Turno
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga

data class OcupacaoPorTurnoItem(
    val turno: Turno,
    val totalEntradas: Int,
    val atualmenteEstacionados: Int,
    val mediaDiariaEntradas: Double,
    val percentualLotacao: Double
)

data class UsoVagaItem(val tipoVaga: TipoVaga, val ocupadasAgora: Int, val totalRegistros: Int)

data class DashboardOcupacaoResponse(
    val capacidadeTotal: Int,
    val capacidadeExclusivas: Int,
    val capacidadeCarregamentoEletrico: Int,
    val veiculosNoCampusAgora: Int,
    val visitantesNoCampusAgora: Int,
    val vagasExclusivasOcupadasAgora: Int,
    val vagasEletricasOcupadasAgora: Int,
    val percentualOcupacaoAtual: Double,
    val percentualExclusivasAtual: Double,
    val percentualEletricasAtual: Double,
    val totalServidoresAtivos: Int,
    val totalVeiculosAtivos: Int,
    val totalTemporariosVigentes: Int,
    val totalEntradasPeriodo: Int,
    val totalVisitantesPeriodo: Int,
    val diasComMovimento: Int,
    val ocupacaoPorTurno: List<OcupacaoPorTurnoItem>,
    val usoPorTipoVaga: List<UsoVagaItem>
) {
    val capacidadeComuns: Int get() = (capacidadeTotal - capacidadeExclusivas - capacidadeCarregamentoEletrico).coerceAtLeast(0)
    val vagasComunsOcupadasAgora: Int
        get() = (veiculosNoCampusAgora - vagasExclusivasOcupadasAgora - vagasEletricasOcupadasAgora).coerceAtLeast(0)
    val vagasLivresAgora: Int get() = (capacidadeTotal - veiculosNoCampusAgora).coerceAtLeast(0)
    val vagasComunsLivresAgora: Int get() = (capacidadeComuns - vagasComunsOcupadasAgora).coerceAtLeast(0)
    val vagasExclusivasLivresAgora: Int get() = (capacidadeExclusivas - vagasExclusivasOcupadasAgora).coerceAtLeast(0)
    val vagasEletricasLivresAgora: Int get() = (capacidadeCarregamentoEletrico - vagasEletricasOcupadasAgora).coerceAtLeast(0)
    val percentualComunsAtual: Double
        get() = if (capacidadeComuns <= 0) 0.0 else (vagasComunsOcupadasAgora.toDouble() / capacidadeComuns).coerceIn(0.0, 1.0)
}

data class ConfiguracaoRequest(
    val capacidadeTotal: Int = 0,
    val capacidadeExclusivas: Int = 0,
    val capacidadeCarregamentoEletrico: Int = 0
)

data class ConfiguracaoResponse(
    val capacidadeTotal: Int,
    val capacidadeExclusivas: Int,
    val capacidadeCarregamentoEletrico: Int
)
