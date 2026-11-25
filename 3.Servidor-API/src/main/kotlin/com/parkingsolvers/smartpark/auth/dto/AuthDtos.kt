package com.parkingsolvers.smartpark.auth.dto

import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Informe o e-mail, login ou prontuário.") val login: String = "",
    @field:NotBlank(message = "Informe a senha.") val senha: String = ""
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String = ""
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiraEmSegundos: Long,
    val sessao: UsuarioSessao
)
