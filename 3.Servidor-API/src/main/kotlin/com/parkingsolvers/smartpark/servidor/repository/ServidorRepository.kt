package com.parkingsolvers.smartpark.servidor.repository

import com.parkingsolvers.smartpark.servidor.model.Servidor
import org.springframework.data.jpa.repository.JpaRepository

interface ServidorRepository : JpaRepository<Servidor, Long> {
    fun findByProntuario(prontuario: String): Servidor?
}
