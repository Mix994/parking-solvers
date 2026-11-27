package com.parkingsolvers.smartpark.eventos

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SseEventosServiceTest {

    private val agora = Instant.parse("2026-03-10T15:00:00Z")
    private val service = SseEventosService(Clock.fixed(agora, ZoneOffset.UTC))
    private val mockMvc = MockMvcBuilders.standaloneSetup(EventoController(service)).build()

    @Test
    fun `stream SSE envia evento conectado, atualizacoes sequenciais, heartbeat e encerra conexoes`() {
        val mvcResult1 = mockMvc.perform(get("/eventos/stream").accept(MediaType.TEXT_EVENT_STREAM))
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
            .andExpect(header().string("X-Accel-Buffering", "no"))
            .andReturn()

        val mvcResult2 = mockMvc.perform(get("/eventos/stream").accept(MediaType.TEXT_EVENT_STREAM))
            .andExpect(status().isOk)
            .andExpect(request().asyncStarted())
            .andReturn()

        assertEquals(2, service.clientesConectados())

        service.publicar(
            tipo = TipoEventoTempoReal.MOVIMENTACAO_ENTRADA,
            placa = "ABC1D23",
            modeloCor = "Gol • Prata",
            responsavel = "Ana Paula",
            tipoVaga = "EXCLUSIVA_IDOSO",
            tipoVagaLabel = "Exclusiva Idoso",
            origemUsuarioId = 10L,
            origemPerfil = "SERVIDOR",
            simulacaoServidor = true
        )
        service.enviarHeartbeat()
        service.publicar(
            tipo = TipoEventoTempoReal.MOVIMENTACAO_SAIDA,
            placa = "ABC1D23",
            responsavel = "Ana Paula"
        )
        service.encerrarConexoes()
        assertEquals(0, service.clientesConectados())

        listOf(mvcResult1, mvcResult2).forEach { mvcResult ->
            val resposta = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString

            assertTrue(resposta.contains("event:conectado"), "Esperado evento conectado em:\n$resposta")
            assertTrue(resposta.contains("id:1"), "Esperado id:1 em:\n$resposta")
            assertTrue(resposta.contains("id:2"), "Esperado id:2 em:\n$resposta")
            assertEquals(2, Regex("event:atualizacao").findAll(resposta).count(), "Esperado exatamente 2 eventos atualizacao em:\n$resposta")
            assertTrue(resposta.contains("MOVIMENTACAO_ENTRADA"), "Esperado tipo entrada em:\n$resposta")
            assertTrue(resposta.contains("MOVIMENTACAO_SAIDA"), "Esperado tipo saida em:\n$resposta")
            assertTrue(resposta.contains("ABC1D23"), "Esperado placa em:\n$resposta")
            assertTrue(resposta.contains("Exclusiva Idoso"), "Esperado label da vaga em:\n$resposta")
            assertEquals(1, Regex(":keepalive").findAll(resposta).count(), "Esperado exatamente 1 keepalive em:\n$resposta")
            assertTrue(!resposta.contains("id:1\n\n\n"), "Não deve duplicar quebras de linha em:\n$resposta")
        }
    }
}
