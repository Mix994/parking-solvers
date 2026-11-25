package com.parkingsolvers.smartpark.auth.service

import com.parkingsolvers.smartpark.auth.dto.TokenResponse
import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.auth.security.JwtService
import com.parkingsolvers.smartpark.auth.security.TipoToken
import com.parkingsolvers.smartpark.compartilhado.BloqueioTentativasException
import com.parkingsolvers.smartpark.compartilhado.CredenciaisInvalidasException
import com.parkingsolvers.smartpark.compartilhado.RecursoInativoException
import com.parkingsolvers.smartpark.compartilhado.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val servidorRepository: ServidorRepository,
    private val loginAttemptService: LoginAttemptService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional(readOnly = true)
    fun login(loginEntrada: String, senha: String): TokenResponse {
        val login = loginEntrada.trim()

        val restante = loginAttemptService.segundosRestantes(login)
        if (restante > 0) {
            throw BloqueioTentativasException(
                "Muitas tentativas. Tente novamente em ${(restante + 59) / 60} minuto(s).", restante
            )
        }

        val usuario = localizarUsuario(login)
        if (usuario == null || !passwordEncoder.matches(senha, usuario.senhaHash)) {
            loginAttemptService.registrarFalha(login)
            throw CredenciaisInvalidasException()
        }
        val sessao = montarSessao(usuario)
        loginAttemptService.registrarSucesso(login)
        return emitir(sessao)
    }

    @Transactional(readOnly = true)
    fun renovar(refreshToken: String): TokenResponse {
        val atual = jwtService.validar(refreshToken, TipoToken.REFRESH) ?: throw CredenciaisInvalidasException("Sessão expirada")
        val usuario = usuarioRepository.findById(atual.usuarioId).orElse(null)
            ?: throw CredenciaisInvalidasException("Sessão expirada")
        return emitir(montarSessao(usuario))
    }

    private fun emitir(sessao: UsuarioSessao) = TokenResponse(
        accessToken = jwtService.gerar(sessao, TipoToken.ACCESS),
        refreshToken = jwtService.gerar(sessao, TipoToken.REFRESH),
        expiraEmSegundos = jwtService.accessSegundos,
        sessao = sessao
    )

    private fun montarSessao(usuario: Usuario): UsuarioSessao {
        if (!usuario.ativo) throw RecursoInativoException("Esta conta está inativa. Procure a Administração.")
        val servidor = usuario.servidorId?.let { servidorRepository.findById(it).orElse(null) }
        if (usuario.perfil == PerfilUsuario.SERVIDOR) {
            if (servidor == null) throw RecursoNaoEncontradoException("Servidor vinculado não encontrado para esta conta.")
            if (!servidor.ativo) throw RecursoInativoException("O servidor vinculado a esta conta está inativo.")
        }
        return UsuarioSessao(
            usuarioId = usuario.id,
            nome = usuario.nome,
            identificacao = servidor?.prontuario ?: usuario.login,
            perfil = usuario.perfil,
            servidorId = servidor?.id
        )
    }

    private fun localizarUsuario(login: String): Usuario? {
        usuarioRepository.findByLogin(login)?.let { return it }
        val servidor = servidorRepository.findByProntuario(login.uppercase()) ?: return null
        return usuarioRepository.findByServidorId(servidor.id)
    }
}
