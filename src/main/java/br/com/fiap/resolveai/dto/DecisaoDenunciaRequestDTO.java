package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.StatusDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Entrada da decisao do moderador sobre uma denuncia: MANTIDA ou REMOVIDA. */
public class DecisaoDenunciaRequestDTO {

    @NotNull(message = "Informe a decisão: MANTIDA ou REMOVIDA.")
    private StatusDenuncia decisao;

    @NotBlank(message = "Informe o nome de quem está moderando.")
    @Size(max = 100, message = "O nome do moderador pode ter até 100 caracteres.")
    private String moderador;

    @NotBlank(message = "Explique o motivo da decisão.")
    @Size(max = 200, message = "O motivo pode ter até 200 caracteres.")
    private String motivo;

    public StatusDenuncia getDecisao() { return decisao; }
    public void setDecisao(StatusDenuncia decisao) { this.decisao = decisao; }

    public String getModerador() { return moderador; }
    public void setModerador(String moderador) { this.moderador = moderador; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
