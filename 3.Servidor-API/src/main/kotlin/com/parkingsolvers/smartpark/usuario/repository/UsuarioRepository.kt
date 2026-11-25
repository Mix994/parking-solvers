package com.parkingsolvers.smartpark.usuario.repository

import com.parkingsolvers.smartpark.usuario.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByLogin(login: String): Usuario?
    fun findByServidorId(servidorId: Long): Usuario?
}
