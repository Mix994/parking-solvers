package com.parkingsolvers.smartpark.auth

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.auth.model.UsuarioSessao
import com.parkingsolvers.smartpark.auth.security.JwtService
import com.parkingsolvers.smartpark.auth.security.TipoToken
import com.parkingsolvers.smartpark.auth.service.AuthService
import com.parkingsolvers.smartpark.auth.service.LoginAttemptService
import com.parkingsolvers.smartpark.compartilhado.BloqueioTentativasException
import com.parkingsolvers.smartpark.compartilhado.CredenciaisInvalidasException
import com.parkingsolvers.smartpark.compartilhado.RecursoInativoException
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthTest {

    private class RelogioMutavel(var instante: Instant = Instant.parse("2026-01-01T00:00:00Z")) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant() = instante
    }

    private val relogio = RelogioMutavel()
    private val encoder = BCryptPasswordEncoder(4)
    private val jwt = JwtService("0123456789abcdef0123456789abcdef", 60, 7, relogio)
    private val tentativas = LoginAttemptService(relogio)
    private val usuarios = mock(UsuarioRepository::class.java)
    private val servidores = mock(ServidorRepository::class.java)
    private val auth = AuthService(usuarios, servidores, tentativas, encoder, jwt)

    private fun admin(ativo: Boolean = true) =
        Usuario(1, "Admin", "admin", encoder.encode("senha123"), PerfilUsuario.ADMIN, null, ativo)

    @Test
    fun `login valido retorna tokens e sessao`() {
        `when`(usuarios.findByLogin("admin")).thenReturn(admin())
        val r = auth.login(" admin ", "senha123")
        assertEquals(PerfilUsuario.ADMIN, r.sessao.perfil)
        assertEquals("admin", r.sessao.identificacao)
        assertNotNull(jwt.validar(r.accessToken, TipoToken.ACCESS))
    }

    @Test
    fun `senha errada lanca credenciais invalidas e bloqueia apos 5 falhas`() {
        `when`(usuarios.findByLogin("admin")).thenReturn(admin())
        repeat(5) { assertFailsWith<CredenciaisInvalidasException> { auth.login("admin", "errada") } }
        assertFailsWith<BloqueioTentativasException> { auth.login("admin", "senha123") }
        relogio.instante = relogio.instante.plus(Duration.ofMinutes(16))
        assertNotNull(auth.login("admin", "senha123"))
    }

    @Test
    fun `usuario inativo nao entra`() {
        `when`(usuarios.findByLogin("admin")).thenReturn(admin(ativo = false))
        assertFailsWith<RecursoInativoException> { auth.login("admin", "senha123") }
    }

    @Test
    fun `refresh token nao vale como access token e token expirado e rejeitado`() {
        val sessao = UsuarioSessao(1, "Admin", "admin", PerfilUsuario.ADMIN)
        val refresh = jwt.gerar(sessao, TipoToken.REFRESH)
        assertNull(jwt.validar(refresh, TipoToken.ACCESS))
        val access = jwt.gerar(sessao, TipoToken.ACCESS)
        assertEquals(sessao, jwt.validar(access, TipoToken.ACCESS))
        relogio.instante = relogio.instante.plus(Duration.ofMinutes(61))
        assertNull(jwt.validar(access, TipoToken.ACCESS))
        assertNull(jwt.validar("lixo", TipoToken.ACCESS))
    }

    @Test
    fun `sucesso zera contador de falhas`() {
        `when`(usuarios.findByLogin("admin")).thenReturn(admin())
        repeat(4) { assertFailsWith<CredenciaisInvalidasException> { auth.login("admin", "x") } }
        auth.login("admin", "senha123")
        assertFalse(tentativas.estaBloqueado("admin"))
        assertTrue(tentativas.segundosRestantes("admin") == 0L)
    }
}
