package com.parkingsolvers.smartpark.usuario.dto

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.usuario.model.Usuario

data class UsuarioRequest(
    val nome: String = "",
    val login: String = "",
    val senha: String = "",
    val confirmacaoSenha: String = senha,
    val perfil: PerfilUsuario = PerfilUsuario.SERVIDOR,
    val servidorId: Long? = null
)

data class AtualizarUsuarioRequest(
    val nome: String = "",
    val login: String = "",
    val novaSenha: String = "",
    val confirmacaoNovaSenha: String = ""
)

data class AtualizarUsuarioAdminRequest(
    val nome: String = "",
    val login: String = "",
    val novaSenha: String = "",
    val confirmacaoNovaSenha: String = "",
    val perfil: PerfilUsuario? = null,
    val servidorId: Long? = null
)

data class AlterarPerfilRequest(
    val perfil: PerfilUsuario,
    val servidorId: Long? = null
)

data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val login: String,
    val perfil: PerfilUsuario,
    val servidorId: Long?,
    val servidorProntuario: String?,
    val ativo: Boolean
)

fun Usuario.toResponse(servidor: Servidor?) =
    UsuarioResponse(id, nome, login, perfil, servidorId, servidor?.prontuario, ativo)
