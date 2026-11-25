package com.parkingsolvers.smartpark.visitante.model

import com.parkingsolvers.smartpark.compartilhado.Turno
import com.parkingsolvers.smartpark.movimentacao.model.TipoVaga
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "visitante")
class Visitante(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var nome: String = "",
    var documento: String = "",
    var instituicaoOrigem: String = "",
    var telefone: String = "",
    var placa: String = "",
    var modeloVeiculo: String = "",
    var corVeiculo: String = "",
    var motivoVisita: String = "",
    var setorDestino: String = "",
    @Enumerated(EnumType.STRING) var tipoVaga: TipoVaga = TipoVaga.COMUM,
    @Enumerated(EnumType.STRING) var turno: Turno = Turno.MANHA,
    var registradoPor: String = "",
    var dataHoraEntrada: Instant = Instant.EPOCH,
    var dataHoraSaida: Instant? = null,
    var observacao: String = ""
) {
    val noCampus: Boolean get() = dataHoraSaida == null
}
