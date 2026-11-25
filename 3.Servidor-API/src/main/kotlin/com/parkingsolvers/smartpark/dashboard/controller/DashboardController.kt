package com.parkingsolvers.smartpark.dashboard.controller

import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoRequest
import com.parkingsolvers.smartpark.dashboard.dto.ConfiguracaoResponse
import com.parkingsolvers.smartpark.dashboard.dto.DashboardOcupacaoResponse
import com.parkingsolvers.smartpark.dashboard.service.DashboardService
import com.parkingsolvers.smartpark.dashboard.service.PeriodoDashboard
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController(private val service: DashboardService) {

    /** Qualquer usuário autenticado (a tela de vagas disponíveis é aberta a todos os perfis). */
    @GetMapping("/dashboard/ocupacao")
    fun ocupacao(@RequestParam(defaultValue = "HOJE") periodo: PeriodoDashboard): DashboardOcupacaoResponse =
        service.ocupacao(periodo)

    @GetMapping("/configuracao-estacionamento")
    fun configuracao(): ConfiguracaoResponse = service.configuracao()

    @PutMapping("/configuracao-estacionamento")
    @PreAuthorize("hasRole('ADMIN')")
    fun atualizar(@RequestBody request: ConfiguracaoRequest): ConfiguracaoResponse = service.atualizarConfiguracao(request)
}
