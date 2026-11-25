package com.parkingsolvers.smartpark.servidor.controller

import com.parkingsolvers.smartpark.servidor.dto.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.ServidorResponse
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/servidores")
class ServidorController(private val service: ServidorService) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
    fun listar(): List<ServidorResponse> = service.listar()

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA') or (principal.servidorId != null and principal.servidorId == #id)")
    fun buscar(@PathVariable id: Long): ServidorResponse = service.buscarPorId(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    fun cadastrar(@RequestBody request: ServidorRequest): ServidorResponse = service.cadastrar(request)

    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    fun alternarAtivo(@PathVariable id: Long): ServidorResponse = service.alternarAtivo(id)
}
