package com.parkingsolvers.smartpark.dashboard.repository

import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface ConfiguracaoEstacionamentoRepository : JpaRepository<ConfiguracaoEstacionamento, Int> {

    /**
     * Trava a linha única de configuração até o fim da transação. Serve de mutex para
     * serializar entradas que consomem capacidade (vagas elétricas), evitando corrida entre portarias.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ConfiguracaoEstacionamento c where c.id = 1")
    fun buscarComLock(): ConfiguracaoEstacionamento
}
