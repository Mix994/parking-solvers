package com.parkingsolvers.smartpark.auth.model

enum class PerfilUsuario { ADMIN, PORTARIA, SERVIDOR }

/** Dados da sessão extraídos do token JWT (equivale ao UsuarioSessao do app). */
data class UsuarioSessao(
    val usuarioId: Long,
    val nome: String,
    val identificacao: String,
    val perfil: PerfilUsuario,
    val servidorId: Long? = null
)
