package com.parkingsolvers.smartpark.servidor.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "servidor")
class Servidor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var nome: String = "",
    @Column(unique = true) var prontuario: String = "",
    var email: String = "",
    var telefone: String = "",
    var setor: String = "",
    var ativo: Boolean = true
)
