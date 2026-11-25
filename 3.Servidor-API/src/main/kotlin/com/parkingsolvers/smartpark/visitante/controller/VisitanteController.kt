package com.parkingsolvers.smartpark.visitante.controller

import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.visitante.dto.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.VisitanteResponse
import com.parkingsolvers.smartpark.visitante.service.VisitanteService
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
@RequestMapping("/visitantes")
@PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
class VisitanteController(private val service: VisitanteService) {

    @GetMapping
    fun historico(): List<VisitanteResponse> = service.historico()

    @GetMapping("/no-campus")
    fun noCampus(): List<VisitanteResponse> = service.noCampus()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun registrarEntrada(@RequestBody request: VisitanteRequest, @AuthenticationPrincipal sessao: UsuarioSessao): VisitanteResponse =
        service.registrarEntrada(request, sessao.nome)

    @PostMapping("/{id}/saida")
    fun registrarSaida(@PathVariable id: Long): VisitanteResponse = service.registrarSaida(id)
}
