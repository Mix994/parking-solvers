package com.parkingsolvers.smartpark.servidor.dto

import com.parkingsolvers.smartpark.servidor.model.Servidor

data class ServidorRequest(
    val nome: String = "",
    val prontuario: String = "",
    val email: String = "",
    val telefone: String = "",
    val setor: String = ""
)

data class ServidorResponse(
    val id: Long,
    val nome: String,
    val prontuario: String,
    val email: String,
    val telefone: String,
    val setor: String,
    val ativo: Boolean
)

fun Servidor.toResponse() = ServidorResponse(id, nome, prontuario, email, telefone, setor, ativo)
