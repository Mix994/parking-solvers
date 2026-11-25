package com.parkingsolvers.smartpark.usuario.controller

import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.usuario.dto.AlterarPerfilRequest
import com.parkingsolvers.smartpark.usuario.dto.AtualizarUsuarioAdminRequest
import com.parkingsolvers.smartpark.usuario.dto.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.UsuarioResponse
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/usuarios")
class UsuarioController(private val service: UsuarioService) {

    /** "Meus dados": qualquer usuário autenticado. */
    @GetMapping("/me")
    fun meusDados(@AuthenticationPrincipal sessao: UsuarioSessao): UsuarioResponse =
        service.buscarPorId(sessao.usuarioId)

    @PutMapping("/me")
    fun atualizarMeusDados(
        @AuthenticationPrincipal sessao: UsuarioSessao,
        @RequestBody request: AtualizarUsuarioRequest
    ): UsuarioResponse = service.atualizarProprioPerfil(sessao.usuarioId, request)

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listar(): List<UsuarioResponse> = service.listar()

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun buscar(@PathVariable id: Long): UsuarioResponse = service.buscarPorId(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    fun cadastrar(@RequestBody request: UsuarioRequest): UsuarioResponse = service.cadastrar(request)

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun atualizarPorAdmin(
        @PathVariable id: Long,
        @RequestBody request: AtualizarUsuarioAdminRequest,
        @AuthenticationPrincipal sessao: UsuarioSessao
    ): UsuarioResponse = service.atualizarPorAdmin(id, request, sessao.usuarioId)

    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    fun alternarAtivo(@PathVariable id: Long, @AuthenticationPrincipal sessao: UsuarioSessao): UsuarioResponse =
        service.alternarAtivo(id, sessao.usuarioId)

    @PatchMapping("/{id}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    fun alterarPerfil(
        @PathVariable id: Long,
        @RequestBody request: AlterarPerfilRequest,
        @AuthenticationPrincipal sessao: UsuarioSessao
    ): UsuarioResponse = service.alterarPerfil(id, request.perfil, sessao.usuarioId, request.servidorId)
}
