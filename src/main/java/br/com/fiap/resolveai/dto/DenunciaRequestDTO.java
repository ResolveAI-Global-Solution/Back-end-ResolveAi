package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.MotivoDenuncia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Entrada do "Denunciar publicacao". */
public class DenunciaRequestDTO {

    @NotNull(message = "Informe quem está denunciando.")
    @Positive(message = "Informe um usuário válido.")
    private Long cdUsuario;

    @NotNull(message = "Escolha o motivo da denúncia.")
    private MotivoDenuncia motivo;

    @Size(max = 200, message = "O comentário pode ter até 200 caracteres.")
    private String comentario;

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public MotivoDenuncia getMotivo() { return motivo; }
    public void setMotivo(MotivoDenuncia motivo) { this.motivo = motivo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
