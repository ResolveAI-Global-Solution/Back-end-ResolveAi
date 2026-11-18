package br.com.fiap.resolveai.controller;

import br.com.fiap.resolveai.dto.ApoioRequestDTO;
import br.com.fiap.resolveai.dto.ApoioResponseDTO;
import br.com.fiap.resolveai.dto.HistoricoStatusRequestDTO;
import br.com.fiap.resolveai.dto.HistoricoStatusResponseDTO;
import br.com.fiap.resolveai.dto.OcorrenciaRequestDTO;
import br.com.fiap.resolveai.dto.OcorrenciaResponseDTO;
import br.com.fiap.resolveai.model.StatusOcorrencia;
import br.com.fiap.resolveai.service.ApoioService;
import br.com.fiap.resolveai.service.HistoricoStatusService;
import br.com.fiap.resolveai.service.OcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas de ocorrencias (raiz do dominio) e das rotas aninhadas 1:N:
 * /ocorrencias/{id}/historico e /ocorrencias/{id}/apoios.
 */
@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;
    private final HistoricoStatusService historicoService;
    private final ApoioService apoioService;

    public OcorrenciaController(OcorrenciaService ocorrenciaService, HistoricoStatusService historicoService,
                                ApoioService apoioService) {
        this.ocorrenciaService = ocorrenciaService;
        this.historicoService = historicoService;
        this.apoioService = apoioService;
    }

    @GetMapping
    public ResponseEntity<List<OcorrenciaResponseDTO>> listar(
            @RequestParam(required = false) StatusOcorrencia status,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Long bairro) {
        return ResponseEntity.ok(ocorrenciaService.listar(status, categoria, bairro));
    }

    @GetMapping("/parecidas")
    public ResponseEntity<List<OcorrenciaResponseDTO>> buscarParecidas(@RequestParam Long cdCategoria,
                                                                       @RequestParam Double latitude,
                                                                       @RequestParam Double longitude) {
        return ResponseEntity.ok(ocorrenciaService.buscarParecidas(cdCategoria, latitude, longitude));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ocorrenciaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponseDTO> registrar(@Valid @RequestBody OcorrenciaRequestDTO dto) {
        OcorrenciaResponseDTO criada = ocorrenciaService.registrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody OcorrenciaRequestDTO dto) {
        return ResponseEntity.ok(ocorrenciaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        ocorrenciaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoStatusResponseDTO>> listarHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(historicoService.listarPorOcorrencia(id));
    }

    @PostMapping("/{id}/historico")
    public ResponseEntity<HistoricoStatusResponseDTO> registrarHistorico(@PathVariable Long id,
                                                                         @Valid @RequestBody HistoricoStatusRequestDTO dto) {
        HistoricoStatusResponseDTO criado = historicoService.registrar(id, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/historico/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/{id}/apoios")
    public ResponseEntity<List<ApoioResponseDTO>> listarApoios(@PathVariable Long id) {
        return ResponseEntity.ok(apoioService.listarPorOcorrencia(id));
    }

    @PostMapping("/{id}/apoios")
    public ResponseEntity<ApoioResponseDTO> apoiar(@PathVariable Long id, @Valid @RequestBody ApoioRequestDTO dto) {
        ApoioResponseDTO criado = apoioService.apoiar(id, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{idUsuario}").buildAndExpand(criado.getCdUsuario()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @DeleteMapping("/{id}/apoios/{idUsuario}")
    public ResponseEntity<Void> removerApoio(@PathVariable Long id, @PathVariable Long idUsuario) {
        apoioService.remover(id, idUsuario);
        return ResponseEntity.noContent().build();
    }
}
