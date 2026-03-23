package br.edu.fatec.startiot.controller;

import br.edu.fatec.startiot.dto.request.CarrinhoRequest;
import br.edu.fatec.startiot.dto.request.VistoriaRequest;
import br.edu.fatec.startiot.dto.response.CarrinhoResponse;
import br.edu.fatec.startiot.service.CarrinhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrinhos")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @PostMapping
    public ResponseEntity<CarrinhoResponse> cadastrar(@Valid @RequestBody CarrinhoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrinhoService.cadastrar(request));
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<CarrinhoResponse> buscarPorEquipe(@PathVariable Long equipeId) {
        return ResponseEntity.ok(carrinhoService.buscarPorEquipe(equipeId));
    }

    @PatchMapping("/equipe/{equipeId}/vistoria")
    public ResponseEntity<CarrinhoResponse> registrarVistoria(
            @PathVariable Long equipeId,
            @Valid @RequestBody VistoriaRequest request) {
        return ResponseEntity.ok(carrinhoService.registrarVistoria(equipeId, request));
    }
}
