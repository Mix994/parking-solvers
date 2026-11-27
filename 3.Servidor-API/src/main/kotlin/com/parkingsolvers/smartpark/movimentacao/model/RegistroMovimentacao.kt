package com.parkingsolvers.smartpark.movimentacao.model

import com.parkingsolvers.smartpark.compartilhado.Turno
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

enum class TipoVaga(
    val exclusiva: Boolean,
    val carregamentoEletrico: Boolean = false,
    val label: String
) {
    COMUM(false, label = "Vaga Comum"),
    CARREGAMENTO_ELETRICO(false, carregamentoEletrico = true, label = "Carregamento Elétrico"),
    EXCLUSIVA_PCD(true, label = "Exclusiva PCD"),
    EXCLUSIVA_IDOSO(true, label = "Exclusiva Idoso"),
    EXCLUSIVA_OFICIAL(true, label = "Viatura / Oficial")
}

@Entity
@Table(name = "registro_movimentacao")
class RegistroMovimentacao(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "veiculo_id") var veiculoId: Long? = null,
    var placaRegistrada: String = "",
    var autorizado: Boolean = false,
    var dataHoraEntrada: Instant = Instant.EPOCH,
    var dataHoraSaida: Instant? = null,
    @Enumerated(EnumType.STRING) var turno: Turno = Turno.MANHA,
    @Enumerated(EnumType.STRING) var tipoVaga: TipoVaga = TipoVaga.COMUM,
    var observacao: String = ""
)
