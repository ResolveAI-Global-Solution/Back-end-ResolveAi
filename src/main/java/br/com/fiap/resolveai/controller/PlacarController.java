package br.com.fiap.resolveai.controller;

import br.com.fiap.resolveai.dto.PlacarItemResponseDTO;
import br.com.fiap.resolveai.service.PlacarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Rota do placar por bairro, com filtros opcionais de categoria e periodo (datas no formato AAAA-MM-DD). */
@RestController
@RequestMapping("/placar")
public class PlacarController {

    private final PlacarService placarService;

    public PlacarController(PlacarService placarService) {
        this.placarService = placarService;
    }

    @GetMapping
    public ResponseEntity<List<PlacarItemResponseDTO>> listar(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(placarService.listar(categoria, inicio, fim));
    }
}
