package br.com.fiap.resolveai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Rota de verificacao de disponibilidade da API. */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, String>> verificarSaude() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
