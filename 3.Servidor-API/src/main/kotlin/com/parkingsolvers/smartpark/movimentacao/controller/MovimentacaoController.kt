package com.parkingsolvers.smartpark.movimentacao.controller

import com.parkingsolvers.smartpark.movimentacao.dto.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.service.MovimentacaoService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/movimentacoes")
@PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
class MovimentacaoController(private val service: MovimentacaoService) {

    @GetMapping
    fun historico(): List<MovimentacaoResponse> = service.historico()

    @GetMapping("/no-estacionamento")
    fun noEstacionamento(): List<MovimentacaoResponse> = service.noEstacionamento()

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    fun entrada(@RequestBody request: EntradaRequest): MovimentacaoResponse = service.registrarEntrada(request)

    @PostMapping("/{id}/saida")
    fun saida(@PathVariable id: Long): MovimentacaoResponse = service.registrarSaida(id)
}
