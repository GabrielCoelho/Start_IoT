package br.edu.fatec.startiot.exception;

import br.edu.fatec.startiot.dto.request.EventoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RestController
    static class FakeController {
        @GetMapping("/test/not-found")
        void notFound() { throw new NotFoundException("entidade não encontrada"); }

        @GetMapping("/test/business")
        void business() { throw new BusinessException("regra de negócio violada"); }

        @GetMapping("/test/conflict")
        void conflict() { throw new ConflictException("dado duplicado"); }

        @PostMapping("/test/validation")
        void validation(@RequestBody @Validated EventoRequest body) {}

        @GetMapping("/test/generic")
        void generic() { throw new RuntimeException("erro inesperado"); }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar404ParaNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("entidade não encontrada"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deveRetornar422ParaBusinessException() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Violação de regra de negócio"))
                .andExpect(jsonPath("$.detail").value("regra de negócio violada"));
    }

    @Test
    void deveRetornar409ParaConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"))
                .andExpect(jsonPath("$.detail").value("dado duplicado"));
    }

    @Test
    void deveRetornar400ComCamposParaValidationException() throws Exception {
        String bodyInvalido = objectMapper.writeValueAsString(new EventoRequest("", null));

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.campos").exists())
                .andExpect(jsonPath("$.campos.nome").exists());
    }

    @Test
    void deveRetornar500ParaExcecaoGenerica() throws Exception {
        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Erro interno do servidor"));
    }
}
