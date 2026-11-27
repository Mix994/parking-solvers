package com.parkingsolvers.smartpark.visitante.service

import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.ValidadorFormulario
import com.parkingsolvers.smartpark.compartilhado.ValidadorPlaca
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import com.parkingsolvers.smartpark.eventos.PublicadorEventosTempoReal
import com.parkingsolvers.smartpark.eventos.TipoEventoTempoReal
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.visitante.dto.VisitanteRequest
import com.parkingsolvers.smartpark.visitante.dto.VisitanteResponse
import com.parkingsolvers.smartpark.visitante.dto.toResponse
import com.parkingsolvers.smartpark.visitante.model.Visitante
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VisitanteService(
    private val visitanteRepository: VisitanteRepository,
    private val movimentacaoRepository: MovimentacaoRepository,
    private val configuracaoRepository: ConfiguracaoEstacionamentoRepository,
    private val relogio: RelogioNegocio,
    private val eventos: PublicadorEventosTempoReal = PublicadorEventosTempoReal.NOOP
) {

    @Transactional(readOnly = true)
    fun historico(): List<VisitanteResponse> = visitanteRepository.findAllByOrderByDataHoraEntradaDesc().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun noCampus(): List<VisitanteResponse> =
        visitanteRepository.findByDataHoraSaidaIsNullOrderByDataHoraEntradaDesc().map { it.toResponse() }

    fun registrarEntrada(
        request: VisitanteRequest,
        registradoPor: String,
        sessao: UsuarioSessao? = null
    ): VisitanteResponse {
        validarCampos(request)
        val placa = ValidadorPlaca.validarENormalizar(request.placa)

        // A linha de configuração funciona como mutex: serializa entradas concorrentes (placa duplicada e vaga elétrica).
        val configuracao = configuracaoRepository.buscarComLock()

        if (visitanteRepository.findFirstByPlacaAndDataHoraSaidaIsNull(placa) != null)
            throw ConflitoException("O visitante com placa ${ValidadorPlaca.formatar(placa)} já está registrado no campus.")

        if (request.tipoVaga == TipoVaga.CARREGAMENTO_ELETRICO) {
            val capacidade = configuracao.capacidadeCarregamentoEletrico
            val ocupadas = visitanteRepository.countByTipoVagaAndDataHoraSaidaIsNull(TipoVaga.CARREGAMENTO_ELETRICO) +
                movimentacaoRepository.countByTipoVagaAndDataHoraSaidaIsNull(TipoVaga.CARREGAMENTO_ELETRICO)
            if (ocupadas >= capacidade)
                throw ConflitoException("Todas as $capacidade vagas de carregamento elétrico estão ocupadas no momento.")
        }

        val agora = relogio.agora()
        val salvo = visitanteRepository.save(
            Visitante(
                nome = request.nome.trim(), documento = request.documento.trim(),
                instituicaoOrigem = request.instituicaoOrigem.trim(), telefone = request.telefone.trim(),
                placa = placa, modeloVeiculo = request.modeloVeiculo.trim(), corVeiculo = request.corVeiculo.trim(),
                motivoVisita = request.motivoVisita.trim(), setorDestino = request.setorDestino.trim(),
                tipoVaga = request.tipoVaga, turno = relogio.turnoDe(agora),
                registradoPor = registradoPor.ifBlank { "Portaria" },
                dataHoraEntrada = agora, observacao = request.observacao.trim()
            )
        )
        return salvo.toResponse().also { resp ->
            eventos.publicar(
                tipo = TipoEventoTempoReal.VISITANTE_ENTRADA,
                placa = resp.placa,
                modeloCor = "${resp.modeloVeiculo} • ${resp.corVeiculo}",
                responsavel = resp.nome,
                tipoVaga = resp.tipoVaga.name,
                tipoVagaLabel = resp.tipoVaga.label,
                origemUsuarioId = sessao?.usuarioId,
                origemPerfil = sessao?.perfil?.name
            )
        }
    }

    fun registrarSaida(id: Long, sessao: UsuarioSessao? = null): VisitanteResponse {
        val visitante = visitanteRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Visitante", id) }
        if (!visitante.noCampus) throw ConflitoException("A saída deste visitante já foi registrada.")
        visitante.dataHoraSaida = relogio.agora()
        return visitanteRepository.save(visitante).toResponse().also { resp ->
            eventos.publicar(
                tipo = TipoEventoTempoReal.VISITANTE_SAIDA,
                placa = resp.placa,
                modeloCor = "${resp.modeloVeiculo} • ${resp.corVeiculo}",
                responsavel = resp.nome,
                tipoVaga = resp.tipoVaga.name,
                tipoVagaLabel = resp.tipoVaga.label,
                origemUsuarioId = sessao?.usuarioId,
                origemPerfil = sessao?.perfil?.name
            )
        }
    }

    private fun validarCampos(r: VisitanteRequest) {
        val erros = mutableMapOf<String, String>()
        if (r.nome.trim().length < 3) erros["nome"] = "Informe o nome completo do visitante."
        if (!ValidadorFormulario.validarDocumento(r.documento)) erros["documento"] = "Informe um CPF ou RG válido (mínimo 5 dígitos)."
        if (r.instituicaoOrigem.isBlank()) erros["instituicaoOrigem"] = "Informe a instituição, cidade ou empresa de origem."
        if (!ValidadorFormulario.validarTelefone(r.telefone)) erros["telefone"] = "Informe um telefone válido (ex.: (11) 99999-8888)."
        if (!ValidadorPlaca.ehValida(r.placa)) erros["placa"] = "Formato inválido. Use AAA-1234 ou Mercosul (ABC1D23)."
        if (r.modeloVeiculo.isBlank()) erros["modeloVeiculo"] = "Informe o modelo do veículo."
        if (r.corVeiculo.isBlank()) erros["corVeiculo"] = "Informe a cor do veículo."
        if (r.motivoVisita.isBlank()) erros["motivoVisita"] = "Informe o motivo da visita."
        if (r.setorDestino.isBlank()) erros["setorDestino"] = "Informe o setor ou pessoa visitada no campus."
        ValidadorFormulario.lancarSeHouverErros(erros)
    }
}
