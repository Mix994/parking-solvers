package com.parkingsolvers.smartpark.visitante.repository

import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.visitante.model.Visitante
import org.springframework.data.jpa.repository.JpaRepository

interface VisitanteRepository : JpaRepository<Visitante, Long> {
    fun findAllByOrderByDataHoraEntradaDesc(): List<Visitante>
    fun findByDataHoraSaidaIsNullOrderByDataHoraEntradaDesc(): List<Visitante>
    fun findFirstByPlacaAndDataHoraSaidaIsNull(placa: String): Visitante?
    fun countByTipoVagaAndDataHoraSaidaIsNull(tipoVaga: TipoVaga): Long
    fun findByDataHoraEntradaGreaterThanEqualOrDataHoraSaidaIsNull(inicio: java.time.Instant): List<Visitante>
}
