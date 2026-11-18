package br.com.fiap.resolveai.controller;

import br.com.fiap.resolveai.dto.DecisaoDenunciaRequestDTO;
import br.com.fiap.resolveai.dto.DenunciaRequestDTO;
import br.com.fiap.resolveai.dto.DenunciaResponseDTO;
import br.com.fiap.resolveai.model.StatusDenuncia;
import br.com.fiap.resolveai.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Rotas de denuncia (morador) e de moderacao (console de moderacao).
 * Nao existe DELETE: a decisao da moderacao nao pode ser apagada, entao o metodo responde 405.
 */
@RestController
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping("/ocorrencias/{id}/denuncias")
    public ResponseEntity<DenunciaResponseDTO> denunciar(@PathVariable Long id,
                                                         @Valid @RequestBody DenunciaRequestDTO dto) {
        DenunciaResponseDTO criada = denunciaService.denunciar(id, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/denuncias/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping("/denuncias")
    public ResponseEntity<List<DenunciaResponseDTO>> listar(@RequestParam(required = false) StatusDenuncia status) {
        return ResponseEntity.ok(denunciaService.listar(status));
    }

    @GetMapping("/denuncias/{id}")
    public ResponseEntity<DenunciaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(denunciaService.buscarPorId(id));
    }

    @PutMapping("/denuncias/{id}")
    public ResponseEntity<DenunciaResponseDTO> decidir(@PathVariable Long id,
                                                       @Valid @RequestBody DecisaoDenunciaRequestDTO dto) {
        return ResponseEntity.ok(denunciaService.decidir(id, dto));
    }
}
