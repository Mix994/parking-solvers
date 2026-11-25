package com.parkingsolvers.smartpark.compartilhado

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Turno { MANHA, TARDE, NOITE }

/** Fuso de negócio do campus (turnos e mensagens), configurável via smartpark.timezone. */
@Component
class RelogioNegocio(
    val clock: Clock,
    @Value("\${smartpark.timezone:America/Sao_Paulo}") zona: String
) {
    val zone: ZoneId = ZoneId.of(zona)
    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun agora(): Instant = clock.instant()

    fun turnoDe(instante: Instant): Turno = turnoPorHora(instante.atZone(zone).hour)

    fun formatar(instante: Instant): String = formato.format(instante.atZone(zone))

    companion object {
        fun turnoPorHora(hora: Int): Turno = when (hora) {
            in 6..11 -> Turno.MANHA
            in 12..17 -> Turno.TARDE
            else -> Turno.NOITE
        }
    }
}

object ValidadorPlaca {
    private val PADRAO = Regex("^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$")

    fun ehValida(placa: String?): Boolean = !placa.isNullOrBlank() && PADRAO.matches(placa.trim())

    fun normalizar(placa: String) = placa.trim().uppercase().replace("-", "")

    fun validarENormalizar(placa: String?): String {
        if (!ehValida(placa)) throw PlacaInvalidaException()
        return normalizar(placa!!)
    }

    /** AAA-1234 para placas antigas; Mercosul permanece ABC1D23. */
    fun formatar(placaNormalizada: String): String {
        val limpa = normalizar(placaNormalizada)
        return if (limpa.length == 7 && limpa[4].isDigit()) "${limpa.substring(0, 3)}-${limpa.substring(3)}" else limpa
    }
}
