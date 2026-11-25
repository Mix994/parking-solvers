package com.parkingsolvers.smartpark.dev

import com.parkingsolvers.smartpark.auth.model.PerfilUsuario
import com.parkingsolvers.smartpark.usuario.model.Usuario
import com.parkingsolvers.smartpark.usuario.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

/** Cria um administrador inicial (admin / admin123) se não houver usuários. Só no perfil dev. */
@Component
@Profile("dev")
class AdminInicialDev(
    private val usuarios: UsuarioRepository,
    private val encoder: PasswordEncoder
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (usuarios.count() > 0) return
        usuarios.save(
            Usuario(nome = "Administrador", login = "admin", senhaHash = encoder.encode("admin123"), perfil = PerfilUsuario.ADMIN)
        )
        LoggerFactory.getLogger(javaClass).warn("Usuário dev criado: admin / admin123 (NÃO use em produção)")
    }
}
