package com.parkingsolvers.smartpark.movimentacao.controller

import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.movimentacao.dto.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/movimentacoes")
class MovimentacaoController(private val service: MovimentacaoService) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
    fun historico(): List<MovimentacaoResponse> = service.historico()

    @GetMapping("/no-estacionamento")
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
    fun noEstacionamento(): List<MovimentacaoResponse> = service.noEstacionamento()

    @GetMapping("/minhas-abertas")
    @PreAuthorize("hasRole('SERVIDOR')")
    fun minhasAbertas(@AuthenticationPrincipal sessao: UsuarioSessao): List<MovimentacaoResponse> =
        service.minhasAbertas(sessao)

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA','SERVIDOR')")
    fun entrada(
        @RequestBody request: EntradaRequest,
        @AuthenticationPrincipal sessao: UsuarioSessao
    ): MovimentacaoResponse = service.registrarEntrada(request, sessao)

    @PostMapping("/{id}/saida")
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA','SERVIDOR')")
    fun saida(
        @PathVariable id: Long,
        @AuthenticationPrincipal sessao: UsuarioSessao
    ): MovimentacaoResponse = service.registrarSaida(id, sessao)
}
