package br.com.fiap.resolveai.controller;

import br.com.fiap.resolveai.dto.BairroResponseDTO;
import br.com.fiap.resolveai.service.BairroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rota de leitura dos bairros atendidos (busca e selecao de bairro no app). */
@RestController
@RequestMapping("/bairros")
public class BairroController {

    private final BairroService bairroService;

    public BairroController(BairroService bairroService) {
        this.bairroService = bairroService;
    }

    @GetMapping
    public ResponseEntity<List<BairroResponseDTO>> listar() {
        return ResponseEntity.ok(bairroService.listar());
    }
}
