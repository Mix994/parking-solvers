package com.parkingsolvers.smartpark.movimentacao.repository

import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface MovimentacaoRepository : JpaRepository<RegistroMovimentacao, Long> {
    fun countByTipoVagaAndDataHoraSaidaIsNull(tipoVaga: TipoVaga): Long
    fun findAllByOrderByDataHoraEntradaDesc(): List<RegistroMovimentacao>
    fun findByDataHoraSaidaIsNullOrderByDataHoraEntradaDesc(): List<RegistroMovimentacao>
    fun findByVeiculoIdInAndDataHoraSaidaIsNullOrderByDataHoraEntradaDesc(veiculoIds: Collection<Long>): List<RegistroMovimentacao>
    fun findFirstByPlacaRegistradaAndDataHoraSaidaIsNull(placa: String): RegistroMovimentacao?

    /** Entradas do período mais todos os registros em aberto (para o dashboard). */
    fun findByDataHoraEntradaGreaterThanEqualOrDataHoraSaidaIsNull(inicio: Instant): List<RegistroMovimentacao>
}
