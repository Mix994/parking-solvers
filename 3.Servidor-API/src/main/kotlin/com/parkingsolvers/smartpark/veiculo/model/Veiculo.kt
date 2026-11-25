package com.parkingsolvers.smartpark.veiculo.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

enum class TipoCadastroVeiculo { PERMANENTE, TEMPORARIO }

@Entity
@Table(name = "veiculo")
class Veiculo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "servidor_id") var servidorId: Long = 0,
    @Column(unique = true) var placa: String = "",
    var modelo: String = "",
    var cor: String = "",
    @Enumerated(EnumType.STRING) var tipoCadastro: TipoCadastroVeiculo = TipoCadastroVeiculo.PERMANENTE,
    var validadeInicio: Instant? = null,
    var validadeFim: Instant? = null,
    var ativo: Boolean = true
) {
    fun estaDentroDaValidade(agora: Instant): Boolean {
        if (tipoCadastro == TipoCadastroVeiculo.PERMANENTE) return true
        val inicio = validadeInicio ?: return false
        val fim = validadeFim ?: return false
        return !agora.isBefore(inicio) && !agora.isAfter(fim)
    }
}
