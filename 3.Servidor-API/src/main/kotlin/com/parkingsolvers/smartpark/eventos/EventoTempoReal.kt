package com.parkingsolvers.smartpark.eventos

import java.time.Instant

enum class TipoEventoTempoReal {
    MOVIMENTACAO_ENTRADA,
    MOVIMENTACAO_SAIDA,
    VISITANTE_ENTRADA,
    VISITANTE_SAIDA,
    CONFIGURACAO_ATUALIZADA,
    CADASTRO_ATUALIZADO
}

data class EventoTempoReal(
    val id: Long,
    val tipo: TipoEventoTempoReal,
    val placa: String? = null,
    val modeloCor: String? = null,
    val responsavel: String? = null,
    val tipoVaga: String? = null,
    val tipoVagaLabel: String? = null,
    val origemUsuarioId: Long? = null,
    val origemPerfil: String? = null,
    val simulacaoServidor: Boolean = false,
    val ocorridoEm: Instant = Instant.now()
)
