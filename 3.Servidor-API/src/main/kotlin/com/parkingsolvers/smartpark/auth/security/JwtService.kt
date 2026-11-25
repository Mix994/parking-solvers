package com.parkingsolvers.smartpark.auth.security

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.Date

enum class TipoToken { ACCESS, REFRESH }

@Service
class JwtService(
    @Value("\${smartpark.jwt.secret}") secret: String,
    @Value("\${smartpark.jwt.access-minutos}") private val accessMinutos: Long,
    @Value("\${smartpark.jwt.refresh-dias}") private val refreshDias: Long,
    private val clock: Clock
) {
    private val chave = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8).also {
        require(it.size >= 32) { "smartpark.jwt.secret deve ter ao menos 32 caracteres" }
    })

    val accessSegundos: Long get() = accessMinutos * 60

    fun gerar(sessao: UsuarioSessao, tipo: TipoToken): String {
        val agora = clock.millis()
        val duracao = if (tipo == TipoToken.ACCESS) accessSegundos * 1000 else refreshDias * 24 * 3600 * 1000
        return Jwts.builder()
            .subject(sessao.usuarioId.toString())
            .claim("typ", tipo.name)
            .claim("nome", sessao.nome)
            .claim("identificacao", sessao.identificacao)
            .claim("perfil", sessao.perfil.name)
            .claim("servidorId", sessao.servidorId)
            .issuedAt(Date(agora))
            .expiration(Date(agora + duracao))
            .signWith(chave)
            .compact()
    }

    /** Retorna a sessão se o token for válido, do tipo esperado e não expirado; senão null. */
    fun validar(token: String, tipoEsperado: TipoToken): UsuarioSessao? = try {
        val claims: Claims = Jwts.parser().verifyWith(chave).clock { Date(clock.millis()) }.build()
            .parseSignedClaims(token).payload
        if (claims["typ"] != tipoEsperado.name) null
        else UsuarioSessao(
            usuarioId = claims.subject.toLong(),
            nome = claims["nome"] as String,
            identificacao = claims["identificacao"] as String,
            perfil = PerfilUsuario.valueOf(claims["perfil"] as String),
            servidorId = (claims["servidorId"] as? Number)?.toLong()
        )
    } catch (e: JwtException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
