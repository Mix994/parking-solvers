package com.parkingsolvers.smartpark.auth.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.parkingsolvers.smartpark.compartilhado.ErroResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val token = req.getHeader("Authorization")?.takeIf { it.startsWith("Bearer ") }?.substring(7)
        val sessao = token?.let { jwtService.validar(it, TipoToken.ACCESS) }
        if (sessao != null) {
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                sessao, null, listOf(SimpleGrantedAuthority("ROLE_${sessao.perfil.name}"))
            )
        }
        chain.doFilter(req, res)
    }
}

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,
        mapper: ObjectMapper
    ): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/auth/login",
                    "/auth/refresh",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, res, _ -> escrever(mapper, res, HttpStatus.UNAUTHORIZED, "Não autenticado") }
                    .accessDeniedHandler { _, res, _ -> escrever(mapper, res, HttpStatus.FORBIDDEN, "Acesso negado") }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    private fun escrever(mapper: ObjectMapper, res: HttpServletResponse, status: HttpStatus, msg: String) {
        res.status = status.value()
        res.contentType = MediaType.APPLICATION_JSON_VALUE
        res.characterEncoding = "UTF-8"
        mapper.writeValue(res.writer, ErroResponse(status.value(), status.reasonPhrase, msg))
    }
}
