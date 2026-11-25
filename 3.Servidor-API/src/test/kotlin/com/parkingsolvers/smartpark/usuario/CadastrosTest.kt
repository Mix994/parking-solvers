package com.parkingsolvers.smartpark.usuario

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.compartilhado.ConflitoException
import com.parkingsolvers.smartpark.compartilhado.RegraDeNegocioException
import com.parkingsolvers.smartpark.compartilhado.ValidacaoException
import com.parkingsolvers.smartpark.servidor.dto.ServidorRequest
import com.parkingsolvers.smartpark.servidor.model.Servidor
import com.parkingsolvers.smartpark.servidor.repository.ServidorRepository
import com.parkingsolvers.smartpark.servidor.service.ServidorService
import com.parkingsolvers.smartpark.usuario.dto.UsuarioRequest
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import com.parkingsolvers.smartpark.usuario.service.UsuarioService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CadastrosTest {

    private val usuarios = mock(UsuarioRepository::class.java)
    private val servidores = mock(ServidorRepository::class.java)
    private val encoder = BCryptPasswordEncoder(4)
    private val usuarioService = UsuarioService(usuarios, servidores, encoder)
    private val servidorService = ServidorService(servidores)

    init {
        `when`(usuarios.save(any(Usuario::class.java))).thenAnswer { it.arguments[0] }
        `when`(servidores.save(any(Servidor::class.java))).thenAnswer { it.arguments[0] }
    }

    @Test
    fun `cadastro de usuario valida campos`() {
        val e = assertFailsWith<ValidacaoException> {
            usuarioService.cadastrar(UsuarioRequest(nome = "A", login = "a b", senha = "123", perfil = PerfilUsuario.SERVIDOR))
        }
        assertTrue(e.campos.keys.containsAll(listOf("nome", "login", "senha", "servidor")))
    }

    @Test
    fun `cadastro de usuario normaliza login e gera hash`() {
        val r = usuarioService.cadastrar(
            UsuarioRequest(nome = "Maria Silva", login = " Maria@Ifsp.edu.br ", senha = "segredo1", perfil = PerfilUsuario.PORTARIA)
        )
        assertEquals("maria@ifsp.edu.br", r.login)
    }

    @Test
    fun `login duplicado gera conflito`() {
        `when`(usuarios.findByLogin("admin2")).thenReturn(Usuario(login = "admin2"))
        assertFailsWith<ConflitoException> {
            usuarioService.cadastrar(UsuarioRequest(nome = "Fulano", login = "admin2", senha = "segredo1", perfil = PerfilUsuario.ADMIN))
        }
    }

    @Test
    fun `nao pode inativar nem mudar perfil da propria conta`() {
        assertFailsWith<RegraDeNegocioException> { usuarioService.alternarAtivo(1, 1) }
        assertFailsWith<RegraDeNegocioException> { usuarioService.alterarPerfil(1, PerfilUsuario.ADMIN, 1, null) }
    }

    @Test
    fun `servidor valida campos e rejeita prontuario duplicado`() {
        assertFailsWith<ValidacaoException> { servidorService.cadastrar(ServidorRequest()) }
        `when`(servidores.findByProntuario("SP1234")).thenReturn(Servidor(prontuario = "SP1234"))
        assertFailsWith<ConflitoException> {
            servidorService.cadastrar(ServidorRequest("Ana Souza", "sp1234", "ana@ifsp.edu.br", "(11) 99999-8888", "TI"))
        }
    }

    @Test
    fun `alternar ativo do servidor inverte o flag`() {
        `when`(servidores.findById(5L)).thenReturn(Optional.of(Servidor(id = 5, ativo = true)))
        assertEquals(false, servidorService.alternarAtivo(5).ativo)
    }
}
