package com.parkingsolvers.smartpark.veiculo.controller

import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.veiculo.dto.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/veiculos")
class VeiculoController(private val service: VeiculoService) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
    fun listar(): List<VeiculoResponse> = service.listar()

    @GetMapping("/servidor/{servidorId}")
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA') or (principal.servidorId != null and principal.servidorId == #servidorId)")
    fun listarPorServidor(@PathVariable servidorId: Long): List<VeiculoResponse> = service.listarPorServidor(servidorId)

    /** Consulta de autorização pela portaria. */
    @GetMapping("/placa/{placa}")
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA')")
    fun consultarPlaca(@PathVariable placa: String): ResultadoConsultaPlaca = service.consultarPlaca(placa)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SERVIDOR')")
    fun cadastrar(@RequestBody request: VeiculoRequest, @AuthenticationPrincipal sessao: UsuarioSessao): VeiculoResponse =
        service.cadastrar(request, sessao)

    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasAnyRole('ADMIN','SERVIDOR')")
    fun alternarAtivo(@PathVariable id: Long, @AuthenticationPrincipal sessao: UsuarioSessao): VeiculoResponse =
        service.alternarAtivo(id, sessao)
}
