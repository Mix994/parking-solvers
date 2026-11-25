package com.parkingsolvers.smartpark.usuario.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RecursoInativoException
import com.parkingsolvers.smartpark.compartilhado.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.ValidadorFormulario
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.dto.AtualizarUsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.dto.UsuarioResponse
import com.parkingsolvers.smartpark.usuario.dto.toResponse
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val servidorRepository: ServidorRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional(readOnly = true)
    fun listar(): List<UsuarioResponse> {
        val servidores = servidorRepository.findAll().associateBy { it.id }
        return usuarioRepository.findAll(Sort.by("nome")).map { it.toResponse(it.servidorId?.let(servidores::get)) }
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): UsuarioResponse = resposta(buscarEntidade(id))

    fun cadastrar(request: UsuarioRequest): UsuarioResponse {
        val erros = mutableMapOf<String, String>()
        val nome = request.nome.trim()
        val login = request.login.trim().lowercase()
        if (nome.length < 3) erros["nome"] = "Informe o nome completo (mínimo 3 caracteres)."
        if (login.length < 4 || login.contains(" ")) erros["login"] = "Informe um login ou e-mail válido (mínimo 4 caracteres, sem espaços)."
        if (request.senha.length < 6) erros["senha"] = "A senha deve ter no mínimo 6 caracteres."
        else if (!ValidadorFormulario.senhasCoincidem(request.senha, request.confirmacaoSenha))
            erros["confirmacaoSenha"] = "A confirmação de senha não confere."
        if (request.perfil == PerfilUsuario.SERVIDOR && request.servidorId == null)
            erros["servidor"] = "Selecione o servidor vinculado para contas de nível Servidor."
        ValidadorFormulario.lancarSeHouverErros(erros)

        if (usuarioRepository.findByLogin(login) != null)
            throw ConflitoException("Já existe uma conta cadastrada com o login '$login'.")

        val servidor = if (request.perfil == PerfilUsuario.SERVIDOR) {
            val id = request.servidorId!!
            val s = servidorRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Servidor", id) }
            if (!s.ativo) throw RecursoInativoException("Não é possível criar conta para servidor inativo.")
            if (usuarioRepository.findByServidorId(id) != null)
                throw ConflitoException("Este servidor já possui uma conta de acesso cadastrada.")
            s
        } else null

        val salvo = usuarioRepository.save(
            Usuario(
                nome = nome, login = login, senhaHash = passwordEncoder.encode(request.senha),
                perfil = request.perfil, servidorId = servidor?.id, ativo = true
            )
        )
        return salvo.toResponse(servidor)
    }

    fun atualizarProprioPerfil(usuarioId: Long, request: AtualizarUsuarioRequest): UsuarioResponse {
        val usuario = buscarEntidade(usuarioId)
        val erros = mutableMapOf<String, String>()
        val nome = request.nome.trim()
        val login = request.login.trim().lowercase()
        if (nome.length < 3) erros["nome"] = "Informe o nome completo (mínimo 3 caracteres)."
        if (login.length < 4 || login.contains(" ")) erros["login"] = "Informe um login ou e-mail válido."
        if (request.novaSenha.isNotEmpty()) {
            if (request.novaSenha.length < 6) erros["novaSenha"] = "A nova senha deve ter no mínimo 6 caracteres."
            else if (!ValidadorFormulario.senhasCoincidem(request.novaSenha, request.confirmacaoNovaSenha))
                erros["confirmacaoNovaSenha"] = "As senhas informadas não coincidem."
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        val existente = usuarioRepository.findByLogin(login)
        if (existente != null && existente.id != usuarioId)
            throw ConflitoException("O login '$login' já está em uso por outra conta.")

        usuario.nome = nome
        usuario.login = login
        if (request.novaSenha.isNotEmpty()) usuario.senhaHash = passwordEncoder.encode(request.novaSenha)
        return resposta(usuarioRepository.save(usuario))
    }

    fun alternarAtivo(usuarioId: Long, usuarioLogadoId: Long): UsuarioResponse {
        if (usuarioId == usuarioLogadoId) throw RegraDeNegocioException("Não é possível inativar a própria conta em uso.")
        val usuario = buscarEntidade(usuarioId)
        usuario.ativo = !usuario.ativo
        return resposta(usuarioRepository.save(usuario))
    }

    fun alterarPerfil(usuarioId: Long, novoPerfil: PerfilUsuario, usuarioLogadoId: Long, servidorId: Long?): UsuarioResponse {
        if (usuarioId == usuarioLogadoId)
            throw RegraDeNegocioException("Não é possível alterar o nível de acesso da própria conta em uso.")
        val usuario = buscarEntidade(usuarioId)
        val servidor: Servidor? = if (novoPerfil == PerfilUsuario.SERVIDOR) {
            val id = servidorId ?: usuario.servidorId
                ?: throw RegraDeNegocioException("Selecione um servidor para o nível Servidor / Docente.")
            val s = servidorRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Servidor", id) }
            val vinculado = usuarioRepository.findByServidorId(id)
            if (vinculado != null && vinculado.id != usuarioId) throw ConflitoException("Este servidor já está vinculado a outra conta.")
            s
        } else null
        usuario.perfil = novoPerfil
        usuario.servidorId = servidor?.id
        return usuarioRepository.save(usuario).toResponse(servidor)
    }

    private fun buscarEntidade(id: Long): Usuario =
        usuarioRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Usuário", id) }

    private fun resposta(u: Usuario) = u.toResponse(u.servidorId?.let { servidorRepository.findById(it).orElse(null) })
}
