package com.parkingsolvers.smartpark.compartilhado

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ErroResponse(
    val status: Int,
    val erro: String,
    val mensagem: String,
    val campos: Map<String, String>? = null,
    val timestamp: Instant = Instant.now()
)

@RestControllerAdvice
class ApiExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    private fun resposta(status: HttpStatus, msg: String, campos: Map<String, String>? = null) =
        ResponseEntity.status(status).body(ErroResponse(status.value(), status.reasonPhrase, msg, campos))

    @ExceptionHandler(RecursoNaoEncontradoException::class)
    fun naoEncontrado(e: RecursoNaoEncontradoException) = resposta(HttpStatus.NOT_FOUND, e.message.orEmpty())

    @ExceptionHandler(ConflitoException::class)
    fun conflito(e: ConflitoException) = resposta(HttpStatus.CONFLICT, e.message.orEmpty())

    @ExceptionHandler(CredenciaisInvalidasException::class)
    fun credenciais(e: CredenciaisInvalidasException) = resposta(HttpStatus.UNAUTHORIZED, e.message.orEmpty())

    @ExceptionHandler(BloqueioTentativasException::class)
    fun bloqueio(e: BloqueioTentativasException) = resposta(HttpStatus.TOO_MANY_REQUESTS, e.message.orEmpty())

    @ExceptionHandler(ValidacaoException::class)
    fun validacao(e: ValidacaoException) =
        resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.message.orEmpty(), e.campos.ifEmpty { null })

    @ExceptionHandler(RegraDeNegocioException::class)
    fun regra(e: RegraDeNegocioException) = resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.message.orEmpty())

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun beanValidation(e: MethodArgumentNotValidException): ResponseEntity<ErroResponse> {
        val campos = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "inválido") }
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun acessoNegado(e: AccessDeniedException) = resposta(HttpStatus.FORBIDDEN, "Acesso negado")

    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    fun requisicaoMalFormada(e: Exception) = resposta(HttpStatus.BAD_REQUEST, "Requisição malformada ou com valor inválido")

    @ExceptionHandler(NoResourceFoundException::class)
    fun semRecurso(e: NoResourceFoundException) = resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun metodoNaoSuportado(e: HttpRequestMethodNotSupportedException) =
        resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado")

    @ExceptionHandler(Exception::class)
    fun inesperado(e: Exception): ResponseEntity<ErroResponse> {
        log.error("Erro inesperado", e)
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor")
    }
}
