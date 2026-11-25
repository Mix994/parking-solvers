package com.parkingsolvers.smartpark.dashboard.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "configuracao_estacionamento")
class ConfiguracaoEstacionamento(
    @Id var id: Int = ID_PADRAO,
    var capacidadeTotal: Int = 60,
    var capacidadeExclusivas: Int = 10,
    @Column(name = "capacidade_carregamento_eletrico") var capacidadeCarregamentoEletrico: Int = 2
) {
    companion object {
        const val ID_PADRAO = 1
    }
}
