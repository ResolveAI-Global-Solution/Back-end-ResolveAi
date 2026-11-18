package br.com.fiap.resolveai.controller;

import br.com.fiap.resolveai.dto.HistoricoStatusResponseDTO;
import br.com.fiap.resolveai.dto.ObservacaoHistoricoRequestDTO;
import br.com.fiap.resolveai.service.HistoricoStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rotas de um registro de historico pelo proprio id (alvo do Location do POST aninhado). */
@RestController
@RequestMapping("/historico")
public class HistoricoStatusController {

    private final HistoricoStatusService historicoService;

    public HistoricoStatusController(HistoricoStatusService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoricoStatusResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistoricoStatusResponseDTO> atualizar(@PathVariable Long id,
                                                                @Valid @RequestBody ObservacaoHistoricoRequestDTO dto) {
        return ResponseEntity.ok(historicoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        historicoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
