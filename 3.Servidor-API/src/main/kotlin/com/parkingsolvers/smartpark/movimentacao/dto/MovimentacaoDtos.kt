package com.parkingsolvers.smartpark.movimentacao.dto

import com.parkingsolvers.smartpark.compartilhado.Turno
import com.parkingsolvers.smartpark.movimentacao.model.RegistroMovimentacao
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import java.time.Instant

data class EntradaRequest(
    val placa: String = "",
    val tipoVaga: TipoVaga = TipoVaga.COMUM,
    val observacao: String = "",
    val permitirNaoAutorizado: Boolean = false
)

data class MovimentacaoResponse(
    val id: Long,
    val veiculoId: Long?,
    val placa: String,
    val modeloCor: String,
    val servidorNome: String,
    val autorizado: Boolean,
    val dataHoraEntrada: Instant,
    val dataHoraSaida: Instant?,
    val turno: Turno,
    val tipoVaga: TipoVaga,
    val observacao: String,
    val emAberto: Boolean
)

fun RegistroMovimentacao.toResponse(veiculo: Veiculo?, servidor: Servidor?) = MovimentacaoResponse(
    id = id, veiculoId = veiculoId, placa = placaRegistrada,
    modeloCor = veiculo?.let { "${it.modelo} • ${it.cor}" } ?: "Veículo não cadastrado",
    servidorNome = servidor?.nome ?: "Acesso avulso / visitante",
    autorizado = autorizado, dataHoraEntrada = dataHoraEntrada, dataHoraSaida = dataHoraSaida,
    turno = turno, tipoVaga = tipoVaga, observacao = observacao, emAberto = dataHoraSaida == null
)
