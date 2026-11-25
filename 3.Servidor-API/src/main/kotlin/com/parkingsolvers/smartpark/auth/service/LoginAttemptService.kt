package com.parkingsolvers.smartpark.auth.service

import org.springframework.stereotype.Service
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap

/** Bloqueia o login por 15 minutos após 5 falhas consecutivas (mesma regra do app). */
@Service
class LoginAttemptService(private val clock: Clock = Clock.systemUTC()) {

    private class Info(var contagem: Int = 0, var bloqueadoAteMillis: Long? = null)

    private val tentativas = ConcurrentHashMap<String, Info>()

    fun estaBloqueado(identificador: String): Boolean = segundosRestantes(identificador) > 0

    fun segundosRestantes(identificador: String): Long {
        val chave = normalizar(identificador)
        val info = tentativas[chave] ?: return 0
        val ate = info.bloqueadoAteMillis ?: return 0
        val restante = ate - clock.millis()
        if (restante <= 0) {
            tentativas.remove(chave)
            return 0
        }
        return (restante + 999) / 1000
    }

    fun registrarFalha(identificador: String) {
        if (tentativas.size >= MAX_ENTRIES) limpar()
        val agora = clock.millis()
        tentativas.compute(normalizar(identificador)) { _, atual ->
            val info = if (atual == null || (atual.bloqueadoAteMillis ?: Long.MAX_VALUE) <= agora) Info() else atual
            info.contagem++
            if (info.contagem >= MAX_TENTATIVAS) info.bloqueadoAteMillis = agora + TEMPO_BLOQUEIO_MILLIS
            info
        }
    }

    fun registrarSucesso(identificador: String) {
        tentativas.remove(normalizar(identificador))
    }

    private fun limpar() {
        val agora = clock.millis()
        tentativas.entries.removeIf { (it.value.bloqueadoAteMillis ?: 0) <= agora }
    }

    private fun normalizar(id: String) = id.trim().lowercase()

    companion object {
        const val MAX_TENTATIVAS = 5
        const val TEMPO_BLOQUEIO_MILLIS = 15L * 60 * 1000
        const val MAX_ENTRIES = 10_000
    }
}
