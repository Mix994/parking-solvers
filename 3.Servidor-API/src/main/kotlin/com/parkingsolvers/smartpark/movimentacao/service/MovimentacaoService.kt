package com.parkingsolvers.smartpark.movimentacao.service

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RecursoNaoEncontradoException
import com.parkingsolvers.smartpark.compartilhado.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.RelogioNegocio
import com.parkingsolvers.smartpark.compartilhado.ValidadorPlaca
import com.parkingsolvers.smartpark.dashboard.repository.ConfiguracaoEstacionamentoRepository
import com.parkingsolvers.smartpark.movimentacao.dto.EntradaRequest
import com.parkingsolvers.smartpark.movimentacao.dto.MovimentacaoResponse
import com.parkingsolvers.smartpark.movimentacao.dto.toResponse
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.movimentacao.repository.MovimentacaoRepository
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.veiculo.repository.VeiculoRepository
import com.parkingsolvers.smartpark.veiculo.service.VeiculoService
import com.parkingsolvers.smartpark.visitante.repository.VisitanteRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MovimentacaoService(
    private val movimentacaoRepository: MovimentacaoRepository,
    private val veiculoRepository: VeiculoRepository,
    private val servidorRepository: ServidorRepository,
    private val visitanteRepository: VisitanteRepository,
    private val configuracaoRepository: ConfiguracaoEstacionamentoRepository,
    private val veiculoService: VeiculoService,
    private val relogio: RelogioNegocio
) {

    @Transactional(readOnly = true)
    fun historico(): List<MovimentacaoResponse> = mapear(movimentacaoRepository.findAllByOrderByDataHoraEntradaDesc())

    @Transactional(readOnly = true)
    fun noEstacionamento(): List<MovimentacaoResponse> =
        mapear(movimentacaoRepository.findByDataHoraSaidaIsNullOrderByDataHoraEntradaDesc())

    @Transactional(readOnly = true)
    fun minhasAbertas(sessao: UsuarioSessao): List<MovimentacaoResponse> {
        val servidorId = sessao.servidorId ?: return emptyList()
        val veiculoIds = veiculoRepository.findByServidorId(servidorId).map { it.id }
        if (veiculoIds.isEmpty()) return emptyList()
        return mapear(movimentacaoRepository.findByVeiculoIdInAndDataHoraSaidaIsNullOrderByDataHoraEntradaDesc(veiculoIds))
    }

    fun registrarEntrada(request: EntradaRequest, sessao: UsuarioSessao? = null): MovimentacaoResponse {
        val placa = ValidadorPlaca.validarENormalizar(request.placa)

        // Mutex por linha de configuração: serializa entradas concorrentes (duplicidade e vagas elétricas).
        val configuracao = configuracaoRepository.buscarComLock()

        val consulta = veiculoService.consultarPlaca(placa)
        val ehServidor = sessao?.perfil == PerfilUsuario.SERVIDOR
        if (ehServidor) {
            if (sessao.servidorId == null || consulta.veiculo == null || consulta.veiculo.servidorId != sessao.servidorId) {
                throw AccessDeniedException("Você só pode simular estacionamento para seus próprios veículos.")
            }
            if (!consulta.autorizado) throw RegraDeNegocioException(consulta.mensagem)
        } else if (!consulta.autorizado && !request.permitirNaoAutorizado) {
            throw RegraDeNegocioException(consulta.mensagem)
        }

        if (movimentacaoRepository.findFirstByPlacaRegistradaAndDataHoraSaidaIsNull(placa) != null)
            throw ConflitoException("O veículo ${ValidadorPlaca.formatar(placa)} já está dentro do estacionamento.")

        if (request.tipoVaga == TipoVaga.CARREGAMENTO_ELETRICO) {
            val capacidade = configuracao.capacidadeCarregamentoEletrico
            val ocupadas = movimentacaoRepository.countByTipoVagaAndDataHoraSaidaIsNull(TipoVaga.CARREGAMENTO_ELETRICO) +
                visitanteRepository.countByTipoVagaAndDataHoraSaidaIsNull(TipoVaga.CARREGAMENTO_ELETRICO)
            if (ocupadas >= capacidade)
                throw ConflitoException("Todas as $capacidade vagas de carregamento elétrico estão ocupadas no momento.")
        }

        val observacaoFinal = if (ehServidor) {
            request.observacao.trim().ifBlank { "Estacionamento simulado pelo servidor" }
        } else {
            request.observacao.trim()
        }

        val agora = relogio.agora()
        val salvo = movimentacaoRepository.save(
            RegistroMovimentacao(
                veiculoId = consulta.veiculo?.id, placaRegistrada = placa, autorizado = consulta.autorizado,
                dataHoraEntrada = agora, turno = relogio.turnoDe(agora), tipoVaga = request.tipoVaga,
                observacao = observacaoFinal
            )
        )
        return mapear(listOf(salvo)).first()
    }

    fun registrarSaida(id: Long, sessao: UsuarioSessao? = null): MovimentacaoResponse {
        val registro = movimentacaoRepository.findById(id).orElseThrow { RecursoNaoEncontradoException("Movimentação", id) }
        if (sessao?.perfil == PerfilUsuario.SERVIDOR) {
            val veiculo = registro.veiculoId?.let { veiculoRepository.findById(it).orElse(null) }
            if (sessao.servidorId == null || veiculo == null || veiculo.servidorId != sessao.servidorId) {
                throw AccessDeniedException("Você só pode registrar saída dos seus próprios veículos.")
            }
        }
        if (registro.dataHoraSaida != null) throw ConflitoException("Saída já foi registrada para esta movimentação.")
        registro.dataHoraSaida = relogio.agora()
        return mapear(listOf(movimentacaoRepository.save(registro))).first()
    }

    private fun mapear(registros: List<RegistroMovimentacao>): List<MovimentacaoResponse> {
        if (registros.isEmpty()) return emptyList()
        val veiculos = veiculoRepository.findAllById(registros.mapNotNull { it.veiculoId }.toSet()).associateBy { it.id }
        val servidores = servidorRepository.findAllById(veiculos.values.map { it.servidorId }.toSet()).associateBy { it.id }
        return registros.map { r ->
            val veiculo = r.veiculoId?.let(veiculos::get)
            r.toResponse(veiculo, veiculo?.let { servidores[it.servidorId] })
        }
    }
}
