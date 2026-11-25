package com.parkingsolvers.smartpark.veiculo.repository

import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import org.springframework.data.jpa.repository.JpaRepository

interface VeiculoRepository : JpaRepository<Veiculo, Long> {
    fun findByPlaca(placa: String): Veiculo?
    fun findByServidorId(servidorId: Long): List<Veiculo>
}
