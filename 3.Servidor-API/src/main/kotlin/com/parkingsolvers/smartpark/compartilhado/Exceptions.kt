package com.parkingsolvers.smartpark.compartilhado

/** Exceções de domínio, equivalentes às do app (compartilhado/exceptions). */
open class RegraDeNegocioException(message: String) : RuntimeException(message)

class ValidacaoException(val campos: Map<String, String>, message: String = "Dados inválidos") :
    RegraDeNegocioException(message)

class PlacaInvalidaException(message: String = "Placa inválida") : RegraDeNegocioException(message)

class RecursoNaoEncontradoException(message: String) : RuntimeException(message) {
    constructor(entidade: String, id: Long) : this("$entidade não encontrado(a) (id $id).")
}

class RecursoInativoException(message: String) : RegraDeNegocioException(message)

class ConflitoException(message: String) : RuntimeException(message)

class CredenciaisInvalidasException(message: String = "Login ou senha inválidos") :
    RuntimeException(message)

class BloqueioTentativasException(message: String, val segundosRestantes: Long = 0) :
    RuntimeException(message)

