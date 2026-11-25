package com.parkingsolvers.smartpark.servidor.service

import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.ValidadorFormulario
import com.parkingsolvers.smartpark.servidor.dto.ServidorRequest
import com.parkingsolvers.smartpark.servidor.dto.ServidorResponse
import com.parkingsolvers.smartpark.servidor.dto.toResponse
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ServidorService(private val repository: ServidorRepository) {

    @Transactional(readOnly = true)
    fun listar(): List<ServidorResponse> =
        repository.findAll(Sort.by("nome")).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): ServidorResponse = buscarEntidade(id).toResponse()

    fun buscarEntidade(id: Long): Servidor =
        repository.findById(id).orElseThrow { RecursoNaoEncontradoException("Servidor", id) }

    fun cadastrar(request: ServidorRequest): ServidorResponse {
        validar(request)
        val prontuario = request.prontuario.trim().uppercase()
        if (repository.findByProntuario(prontuario) != null) {
            throw ConflitoException("Já existe um servidor cadastrado com o prontuário $prontuario.")
        }
        return repository.save(
            Servidor(
                nome = request.nome.trim(),
                prontuario = prontuario,
                email = request.email.trim().lowercase(),
                telefone = request.telefone.trim(),
                setor = request.setor.trim()
            )
        ).toResponse()
    }

    fun alternarAtivo(id: Long): ServidorResponse {
        val servidor = buscarEntidade(id)
        servidor.ativo = !servidor.ativo
        return repository.save(servidor).toResponse()
    }

    private fun validar(r: ServidorRequest) {
        val erros = mutableMapOf<String, String>()
        if (r.nome.isBlank()) erros["nome"] = "Informe o nome completo do servidor."
        if (r.prontuario.trim().length < 4) erros["prontuario"] = "Informe um prontuário/SIAPE válido (mínimo 4 caracteres)."
        if (!ValidadorFormulario.validarEmail(r.email)) erros["email"] = "Informe um e-mail institucional válido."
        if (!ValidadorFormulario.validarTelefone(r.telefone)) erros["telefone"] = "Informe um telefone válido (ex.: (11) 99999-8888)."
        if (r.setor.isBlank()) erros["setor"] = "Informe o setor ou coordenação."
        ValidadorFormulario.lancarSeHouverErros(erros)
    }
}
