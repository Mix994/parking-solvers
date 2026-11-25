package com.parkingsolvers.smartpark.auth.controller

import com.parkingsolvers.smartpark.auth.dto.LoginRequest
import com.parkingsolvers.smartpark.auth.dto.RefreshRequest
import com.parkingsolvers.smartpark.auth.dto.TokenResponse
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): TokenResponse = authService.login(req.login, req.senha)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest): TokenResponse = authService.renovar(req.refreshToken)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal sessao: UsuarioSessao): UsuarioSessao = sessao
}
