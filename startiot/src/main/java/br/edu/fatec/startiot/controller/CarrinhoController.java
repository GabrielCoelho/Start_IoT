package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.CarrinhoRequest;
import br.edu.fatec.startiot.dto.request.VistoriaRequest;
import br.edu.fatec.startiot.dto.response.CarrinhoResponse;
import br.edu.fatec.startiot.service.CarrinhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Carrinhos", description = "Cadastro e vistoria dos carrinhos IoT de cada equipe")
@RestController
@RequestMapping("/api/carrinhos")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @Operation(
        summary = "Cadastrar carrinho",
        description = "Registra o carrinho IoT de uma equipe com suas especificações técnicas (modelo, sensores, etc.). " +
                      "Cada equipe deve ter exatamente um carrinho cadastrado. Este cadastro é pré-requisito para a vistoria técnica."
    )
    @PostMapping
    public ResponseEntity<CarrinhoResponse> cadastrar(@Valid @RequestBody CarrinhoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrinhoService.cadastrar(request));
    }

    @Operation(
        summary = "Buscar carrinho da equipe",
        description = "Retorna os dados do carrinho vinculado a uma equipe, incluindo o resultado da vistoria técnica. " +
                      "Use antes de escalar a equipe em uma corrida para confirmar que o carrinho foi aprovado na vistoria."
    )
    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<CarrinhoResponse> buscarPorEquipe(
            @Parameter(description = "ID da equipe") @PathVariable Long equipeId) {
        return ResponseEntity.ok(carrinhoService.buscarPorEquipe(equipeId));
    }

    @Operation(
        summary = "Registrar resultado da vistoria",
        description = "Registra o resultado da vistoria técnica realizada pela comissão antes do início das corridas. " +
                      "Um carrinho reprovado na vistoria impede a equipe de competir. Informe observações detalhadas em caso de reprovação."
    )
    @PatchMapping("/equipe/{equipeId}/vistoria")
    public ResponseEntity<CarrinhoResponse> registrarVistoria(
            @Parameter(description = "ID da equipe dona do carrinho") @PathVariable Long equipeId,
            @Valid @RequestBody VistoriaRequest request) {
        return ResponseEntity.ok(carrinhoService.registrarVistoria(equipeId, request));
    }
}
