package com.parkingsolvers.smartpark.veiculo.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RecursoInativoException
import com.parkingsolvers.smartpark.compartilhado.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.ValidadorFormulario
import com.parkingsolvers.smartpark.compartilhado.ValidadorPlaca
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.dto.ResultadoConsultaPlaca
import com.parkingsolvers.smartpark.veiculo.dto.StatusAutorizacao
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoRequest
import com.parkingsolvers.smartpark.veiculo.dto.VeiculoResponse
import com.parkingsolvers.smartpark.veiculo.dto.toResponse
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import org.springframework.data.domain.Sort
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class VeiculoService(
    private val veiculoRepository: VeiculoRepository,
    private val servidorRepository: ServidorRepository,
    private val relogio: RelogioNegocio
) {

    @Transactional(readOnly = true)
    fun listar(): List<VeiculoResponse> {
        val servidores = servidorRepository.findAll().associateBy { it.id }
        val agora = relogio.agora()
        return veiculoRepository.findAll(Sort.by("placa")).map { it.toResponse(servidores[it.servidorId], agora) }
    }

    @Transactional(readOnly = true)
    fun listarPorServidor(servidorId: Long): List<VeiculoResponse> {
        val servidor = servidorRepository.findById(servidorId).orElse(null)
        val agora = relogio.agora()
        return veiculoRepository.findByServidorId(servidorId).sortedBy { it.placa }.map { it.toResponse(servidor, agora) }
    }

    fun cadastrar(request: VeiculoRequest, sessao: UsuarioSessao): VeiculoResponse {
        exigirAcessoAoServidor(sessao, request.servidorId)
        val servidor = servidorRepository.findById(request.servidorId)
            .orElseThrow { RecursoNaoEncontradoException("Servidor", request.servidorId) }
        if (!servidor.ativo) throw RecursoInativoException("Não é possível cadastrar veículo para servidor inativo.")

        val erros = mutableMapOf<String, String>()
        if (!ValidadorPlaca.ehValida(request.placa)) erros["placa"] = "Formato de placa inválido. Use AAA-1234 ou Mercosul (ABC1D23)."
        if (request.modelo.isBlank()) erros["modelo"] = "Informe o modelo do veículo."
        if (request.cor.isBlank()) erros["cor"] = "Informe a cor do veículo."
        if (request.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO) {
            val inicio = request.validadeInicio
            val fim = request.validadeFim
            when {
                inicio == null || fim == null -> erros["validade"] = "Informe o período de validade do veículo temporário."
                !fim.isAfter(inicio) -> erros["validade"] = "A data final deve ser posterior à data inicial."
            }
        }
        ValidadorFormulario.lancarSeHouverErros(erros)

        val placa = ValidadorPlaca.validarENormalizar(request.placa)
        if (veiculoRepository.findByPlaca(placa) != null)
            throw ConflitoException("Já existe um veículo cadastrado com a placa ${ValidadorPlaca.formatar(placa)}.")

        val temporario = request.tipoCadastro == TipoCadastroVeiculo.TEMPORARIO
        val salvo = veiculoRepository.save(
            Veiculo(
                servidorId = servidor.id, placa = placa, modelo = request.modelo.trim(), cor = request.cor.trim(),
                tipoCadastro = request.tipoCadastro,
                validadeInicio = if (temporario) request.validadeInicio else null,
                validadeFim = if (temporario) request.validadeFim else null
            )
        )
        return salvo.toResponse(servidor, relogio.agora())
    }

    fun alternarAtivo(id: Long, sessao: UsuarioSessao): VeiculoResponse {
        val veiculo = veiculoRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Veículo", id) }
        exigirAcessoAoServidor(sessao, veiculo.servidorId)
        veiculo.ativo = !veiculo.ativo
        val servidor = servidorRepository.findById(veiculo.servidorId).orElse(null)
        return veiculoRepository.save(veiculo).toResponse(servidor, relogio.agora())
    }

    @Transactional(readOnly = true)
    fun buscarPorTrechoPlaca(termoEntrada: String): List<VeiculoResponse> {
        val termo = termoEntrada.trim().uppercase().replace("-", "")
        if (termo.length < 2) return emptyList()
        val encontrados = veiculoRepository.findTop10ByPlacaContainingIgnoreCaseOrderByPlacaAsc(termo)
        if (encontrados.isEmpty()) return emptyList()
        val servidores = servidorRepository.findAllById(encontrados.map { it.servidorId }.toSet()).associateBy { it.id }
        val agora = relogio.agora()
        return encontrados.map { it.toResponse(servidores[it.servidorId], agora) }
    }

    @Transactional(readOnly = true)
    fun consultarPlaca(placaEntrada: String): ResultadoConsultaPlaca {
        val placa = ValidadorPlaca.validarENormalizar(placaEntrada)
        val veiculo = veiculoRepository.findByPlaca(placa)
            ?: return ResultadoConsultaPlaca(placa, StatusAutorizacao.NAO_CADASTRADO, "Placa não encontrada no cadastro do campus.")
        val servidor = servidorRepository.findById(veiculo.servidorId).orElse(null)
        return avaliarAutorizacao(veiculo, servidor, relogio.agora(), relogio)
    }

    /** ADMIN acessa tudo; SERVIDOR só os próprios; PORTARIA não gerencia veículos. */
    private fun exigirAcessoAoServidor(sessao: UsuarioSessao, servidorId: Long) {
        val permitido = sessao.perfil == PerfilUsuario.ADMIN ||
            (sessao.perfil == PerfilUsuario.SERVIDOR && sessao.servidorId == servidorId)
        if (!permitido) throw AccessDeniedException("Acesso negado")
    }

    companion object {
        fun avaliarAutorizacao(veiculo: Veiculo, servidor: Servidor?, agora: Instant, relogio: RelogioNegocio): ResultadoConsultaPlaca {
            val resp = veiculo.toResponse(servidor, agora)
            fun r(status: StatusAutorizacao, msg: String) = ResultadoConsultaPlaca(veiculo.placa, status, msg, resp)

            if (!veiculo.ativo) return r(StatusAutorizacao.VEICULO_INATIVO, "Veículo cadastrado, porém marcado como inativo.")
            if (servidor?.ativo != true) return r(StatusAutorizacao.SERVIDOR_INATIVO, "O servidor vinculado a este veículo está inativo.")
            if (veiculo.tipoCadastro == TipoCadastroVeiculo.PERMANENTE)
                return r(StatusAutorizacao.AUTORIZADO_PERMANENTE, "Acesso liberado — veículo permanente do servidor.")

            val inicio = veiculo.validadeInicio
            val fim = veiculo.validadeFim
            return when {
                inicio == null || fim == null || agora.isAfter(fim) -> r(
                    StatusAutorizacao.TEMPORARIO_EXPIRADO,
                    "Cadastro temporário expirado${fim?.let { " em ${relogio.formatar(it)}" } ?: ""}."
                )
                agora.isBefore(inicio) -> r(
                    StatusAutorizacao.TEMPORARIO_FORA_VIGENCIA,
                    "Cadastro temporário válido somente a partir de ${relogio.formatar(inicio)}."
                )
                else -> r(StatusAutorizacao.AUTORIZADO_TEMPORARIO, "Acesso liberado — temporário válido até ${relogio.formatar(fim)}.")
            }
        }
    }
}
