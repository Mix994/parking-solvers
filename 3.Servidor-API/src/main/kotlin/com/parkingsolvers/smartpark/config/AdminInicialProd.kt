package com.parkingsolvers.smartpark.config

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/**
 * Bootstrap seguro para o primeiro deploy em produção:
 * cria a conta ADMIN inicial apenas se o banco estiver vazio E as variáveis
 * ADMIN_INITIAL_LOGIN e ADMIN_INITIAL_PASSWORD (mínimo 8 caracteres) forem fornecidas.
 */
@Component
@Profile("prod")
class AdminInicialProd(
    private val usuarios: UsuarioRepository,
    private val encoder: PasswordEncoder,
    @Value("\${ADMIN_INITIAL_NAME:Administrador}") private val nome: String,
    @Value("\${ADMIN_INITIAL_LOGIN:}") private val login: String,
    @Value("\${ADMIN_INITIAL_PASSWORD:}") private val senha: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (usuarios.count() > 0L) return
        val loginLimpo = login.trim().lowercase()
        if (loginLimpo.isBlank() || senha.isBlank()) {
            log.warn(
                "Banco sem usuários em produção. Defina ADMIN_INITIAL_LOGIN e ADMIN_INITIAL_PASSWORD " +
                    "(mínimo 8 caracteres) para criar o primeiro administrador."
            )
            return
        }
        require(senha.length >= 8) { "ADMIN_INITIAL_PASSWORD deve ter no mínimo 8 caracteres." }
        usuarios.save(
            Usuario(
                nome = nome.trim().ifBlank { "Administrador" },
                login = loginLimpo,
                senhaHash = encoder.encode(senha),
                perfil = PerfilUsuario.ADMIN,
                ativo = true
            )
        )
        log.info("Administrador inicial de produção criado: {}", loginLimpo)
    }
}
