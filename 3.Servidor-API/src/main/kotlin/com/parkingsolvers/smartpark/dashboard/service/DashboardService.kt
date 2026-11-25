package com.parkingsolvers.smartpark.dashboard.service

import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.ValidadorFormulario
import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoRequest
import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoResponse
import com.parkingsolvers.smartpark.dashboard.dto.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.model.ConfiguracaoEstacionamento
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DashboardService(
    private val servidorRepository: ServidorRepository,
    private val veiculoRepository: VeiculoRepository,
    private val movimentacaoRepository: MovimentacaoRepository,
    private val visitanteRepository: VisitanteRepository,
    private val configuracaoRepository: ConfiguracaoEstacionamentoRepository,
    private val relogio: RelogioNegocio
) {
    private val calculadora = CalculadoraOcupacao(relogio.zone)

    @Transactional(readOnly = true)
    fun ocupacao(periodo: PeriodoDashboard): DashboardOcupacaoResponse {
        val agora = relogio.agora()
        val inicio = periodo.inicio(agora, relogio.zone)
        val movimentacoes = if (inicio == null) movimentacaoRepository.findAll()
        else movimentacaoRepository.findByDataHoraEntradaGreaterThanEqualOrDataHoraSaidaIsNull(inicio)
        val visitantes = if (inicio == null) visitanteRepository.findAll()
        else visitanteRepository.findByDataHoraEntradaGreaterThanEqualOrDataHoraSaidaIsNull(inicio)
        return calculadora.calcular(
            servidores = servidorRepository.findAll(),
            veiculos = veiculoRepository.findAll(),
            movimentacoes = movimentacoes,
            visitantes = visitantes,
            configuracao = configuracaoRepository.findById(ConfiguracaoEstacionamento.ID_PADRAO).orElseGet { ConfiguracaoEstacionamento() },
            agora = agora,
            inicioPeriodo = inicio
        )
    }

    @Transactional(readOnly = true)
    fun configuracao(): ConfiguracaoResponse =
        (configuracaoRepository.findById(ConfiguracaoEstacionamento.ID_PADRAO).orElseGet { ConfiguracaoEstacionamento() }).toResponse()

    fun atualizarConfiguracao(r: ConfiguracaoRequest): ConfiguracaoResponse {
        val erros = mutableMapOf<String, String>()
        if (r.capacidadeTotal < 1) erros["capacidadeTotal"] = "A capacidade total deve ser de pelo menos 1 vaga."
        if (r.capacidadeExclusivas < 0) erros["capacidadeExclusivas"] = "Informe um número válido de vagas exclusivas."
        if (r.capacidadeCarregamentoEletrico < 0) erros["capacidadeCarregamentoEletrico"] = "Informe um número válido de vagas elétricas."
        if (erros.isEmpty() && r.capacidadeExclusivas + r.capacidadeCarregamentoEletrico > r.capacidadeTotal)
            erros["capacidadeTotal"] = "A soma das vagas exclusivas e de carregamento elétrico não pode ultrapassar a capacidade total."
        ValidadorFormulario.lancarSeHouverErros(erros)

        // Lock: não conflita com entradas em andamento que leem a capacidade elétrica.
        val atual = configuracaoRepository.buscarComLock()
        atual.capacidadeTotal = r.capacidadeTotal
        atual.capacidadeExclusivas = r.capacidadeExclusivas
        atual.capacidadeCarregamentoEletrico = r.capacidadeCarregamentoEletrico
        return configuracaoRepository.save(atual).toResponse()
    }

    private fun ConfiguracaoEstacionamento.toResponse() =
        ConfiguracaoResponse(capacidadeTotal, capacidadeExclusivas, capacidadeCarregamentoEletrico)
}
