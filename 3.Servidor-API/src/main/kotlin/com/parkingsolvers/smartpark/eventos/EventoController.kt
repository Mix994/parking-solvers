package com.parkingsolvers.smartpark.eventos

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/eventos")
class EventoController(private val sseEventosService: SseEventosService) {

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @PreAuthorize("hasAnyRole('ADMIN','PORTARIA','SERVIDOR')")
    fun stream(response: HttpServletResponse): SseEmitter {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
        response.setHeader("X-Accel-Buffering", "no")
        return sseEventosService.conectar()
    }
}
