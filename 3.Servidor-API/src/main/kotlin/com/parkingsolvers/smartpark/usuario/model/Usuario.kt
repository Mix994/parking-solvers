package com.parkingsolvers.smartpark.usuario.model

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "usuario")
class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var nome: String = "",
    @Column(unique = true) var login: String = "",
    @Column(name = "senha_hash") var senhaHash: String = "",
    @Enumerated(EnumType.STRING) var perfil: PerfilUsuario = PerfilUsuario.SERVIDOR,
    @Column(name = "servidor_id") var servidorId: Long? = null,
    var ativo: Boolean = true
)
