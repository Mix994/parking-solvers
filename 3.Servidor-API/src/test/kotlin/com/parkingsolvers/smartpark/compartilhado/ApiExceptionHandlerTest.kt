package com.parkingsolvers.smartpark.compartilhado

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class ApiExceptionHandlerTest {

    private val handler = ApiExceptionHandler()

    @Test
    fun `mapeia excecoes de dominio para status HTTP`() {
        assertEquals(HttpStatus.NOT_FOUND, handler.naoEncontrado(RecursoNaoEncontradoException("x")).statusCode)
        assertEquals(HttpStatus.CONFLICT, handler.conflito(ConflitoException("x")).statusCode)
        assertEquals(HttpStatus.UNAUTHORIZED, handler.credenciais(CredenciaisInvalidasException()).statusCode)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, handler.bloqueio(BloqueioTentativasException("x")).statusCode)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, handler.validacao(ValidacaoException(mapOf("a" to "b"))).statusCode)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, handler.regra(RegraDeNegocioException("x")).statusCode)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.inesperado(RuntimeException("x")).statusCode)
    }
}

