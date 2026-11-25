package com.parkingsolvers.smartpark.veiculo.dto

import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.veiculo.model.TipoCadastroVeiculo
import com.parkingsolvers.smartpark.veiculo.model.Veiculo
import java.time.Instant

data class VeiculoRequest(
    val servidorId: Long = 0,
    val placa: String = "",
    val modelo: String = "",
    val cor: String = "",
    val tipoCadastro: TipoCadastroVeiculo = TipoCadastroVeiculo.PERMANENTE,
    val validadeInicio: Instant? = null,
    val validadeFim: Instant? = null
)

data class VeiculoResponse(
    val id: Long,
    val servidorId: Long,
    val servidorNome: String,
    val servidorProntuario: String,
    val placa: String,
    val modelo: String,
    val cor: String,
    val tipoCadastro: TipoCadastroVeiculo,
    val validadeInicio: Instant?,
    val validadeFim: Instant?,
    val ativo: Boolean,
    val validoNoMomento: Boolean
)

enum class StatusAutorizacao(val autorizado: Boolean) {
    AUTORIZADO_PERMANENTE(true),
    AUTORIZADO_TEMPORARIO(true),
    TEMPORARIO_EXPIRADO(false),
    TEMPORARIO_FORA_VIGENCIA(false),
    VEICULO_INATIVO(false),
    SERVIDOR_INATIVO(false),
    NAO_CADASTRADO(false)
}

data class ResultadoConsultaPlaca(
    val placaConsultada: String,
    val status: StatusAutorizacao,
    val mensagem: String,
    val veiculo: VeiculoResponse? = null
) {
    val autorizado: Boolean get() = status.autorizado
}

fun Veiculo.toResponse(servidor: Servidor?, agora: Instant) = VeiculoResponse(
    id = id, servidorId = servidorId,
    servidorNome = servidor?.nome.orEmpty(), servidorProntuario = servidor?.prontuario.orEmpty(),
    placa = placa, modelo = modelo, cor = cor, tipoCadastro = tipoCadastro,
    validadeInicio = validadeInicio, validadeFim = validadeFim, ativo = ativo,
    validoNoMomento = ativo && (servidor?.ativo == true) && estaDentroDaValidade(agora)
)
