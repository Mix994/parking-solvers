package com.parkingsolvers.smartpark.compartilhado

object ValidadorFormulario {
    private val PADRAO_EMAIL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PADRAO_TELEFONE = Regex("^(\\(?\\d{2}\\)?[\\s-]?)?\\d{4,5}[\\s-]?\\d{4}$")

    fun validarEmail(email: String) = PADRAO_EMAIL.matches(email.trim())

    fun validarTelefone(telefone: String) = PADRAO_TELEFONE.matches(telefone.trim())

    private val PADRAO_DOCUMENTO = Regex("^[A-Za-z0-9.\\-/]{5,20}$")

    fun validarDocumento(documento: String): Boolean {
        val limpo = documento.trim()
        return PADRAO_DOCUMENTO.matches(limpo) && limpo.count { it.isDigit() } >= 5
    }

    fun senhasCoincidem(senha: String, confirmacao: String) = senha.isNotEmpty() && senha == confirmacao

    fun lancarSeHouverErros(erros: Map<String, String>) {
        if (erros.isNotEmpty()) throw ValidacaoException(erros)
    }
}
