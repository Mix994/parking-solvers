package com.parkingsolvers.smartpark.eventos

import jakarta.annotation.PreDestroy
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Clock
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

/**
 * Contrato de publicação de eventos em tempo real para desacoplar os serviços de domínio do transporte SSE.
 */
interface PublicadorEventosTempoReal {
    fun publicar(
        tipo: TipoEventoTempoReal,
        placa: String? = null,
        modeloCor: String? = null,
        responsavel: String? = null,
        tipoVaga: String? = null,
        tipoVagaLabel: String? = null,
        origemUsuarioId: Long? = null,
        origemPerfil: String? = null,
        simulacaoServidor: Boolean = false
    )

    companion object {
        val NOOP = object : PublicadorEventosTempoReal {
            override fun publicar(
                tipo: TipoEventoTempoReal,
                placa: String?,
                modeloCor: String?,
                responsavel: String?,
                tipoVaga: String?,
                tipoVagaLabel: String?,
                origemUsuarioId: Long?,
                origemPerfil: String?,
                simulacaoServidor: Boolean
            ) = Unit
        }
    }
}

/**
 * Gerencia conexões SSE ativas e transmite atualizações somente após o commit da transação,
 * garantindo que os clientes já enxerguem o novo estado ao recarregar os endpoints REST.
 */
@Service
class SseEventosService(private val clock: Clock) : PublicadorEventosTempoReal {

    private val emitters = CopyOnWriteArrayList<SseEmitter>()
    private val sequencia = AtomicLong(0L)

    fun conectar(): SseEmitter {
        val emitter = SseEmitter(0L)
        val remover = { emitters.remove(emitter); Unit }
        emitter.onCompletion(remover)
        emitter.onTimeout(remover)
        emitter.onError { remover() }
        emitters.add(emitter)

        val enviado = enviar(
            emitter,
            SseEmitter.event()
                .name("conectado")
                .data(mapOf("status" to "ok"), MediaType.APPLICATION_JSON)
        )
        if (!enviado) {
            emitters.remove(emitter)
        }
        return emitter
    }

    override fun publicar(
        tipo: TipoEventoTempoReal,
        placa: String?,
        modeloCor: String?,
        responsavel: String?,
        tipoVaga: String?,
        tipoVagaLabel: String?,
        origemUsuarioId: Long?,
        origemPerfil: String?,
        simulacaoServidor: Boolean
    ) {
        val evento = EventoTempoReal(
            id = sequencia.incrementAndGet(),
            tipo = tipo,
            placa = placa,
            modeloCor = modeloCor,
            responsavel = responsavel,
            tipoVaga = tipoVaga,
            tipoVagaLabel = tipoVagaLabel,
            origemUsuarioId = origemUsuarioId,
            origemPerfil = origemPerfil,
            simulacaoServidor = simulacaoServidor,
            ocorridoEm = clock.instant()
        )
        if (TransactionSynchronizationManager.isSynchronizationActive() &&
            TransactionSynchronizationManager.isActualTransactionActive()
        ) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    transmitir(evento)
                }
            })
        } else {
            transmitir(evento)
        }
    }

    internal fun transmitir(evento: EventoTempoReal) {
        if (emitters.isEmpty()) return
        emitters.forEach { emitter ->
            val builder = SseEmitter.event()
                .id(evento.id.toString())
                .name("atualizacao")
                .data(evento, MediaType.APPLICATION_JSON)
            if (!enviar(emitter, builder)) {
                emitters.remove(emitter)
            }
        }
    }

    @Scheduled(fixedDelay = 25_000L)
    fun enviarHeartbeat() {
        if (emitters.isEmpty()) return
        emitters.forEach { emitter ->
            val keepalive = SseEmitter.event().comment("keepalive")
            if (!enviar(emitter, keepalive)) {
                emitters.remove(emitter)
            }
        }
    }

    @PreDestroy
    fun encerrarConexoes() {
        val ativos = emitters.toList()
        emitters.clear()
        ativos.forEach { emitter ->
            runCatching {
                synchronized(emitter) {
                    emitter.complete()
                }
            }
        }
    }

    fun clientesConectados(): Int = emitters.size

    private fun enviar(emitter: SseEmitter, evento: SseEmitter.SseEventBuilder): Boolean {
        return try {
            synchronized(emitter) {
                emitter.send(evento)
            }
            true
        } catch (_: Exception) {
            runCatching { emitter.complete() }
            false
        }
    }
}
