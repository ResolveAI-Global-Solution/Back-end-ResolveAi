package br.com.fiap.resolveai.dto;

import java.time.LocalDateTime;

/** Saida de um apoio a uma ocorrencia. */
public class ApoioResponseDTO {

    private Long cdUsuario;
    private Long cdOcorrencia;
    private LocalDateTime dataApoio;

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public Long getCdOcorrencia() { return cdOcorrencia; }
    public void setCdOcorrencia(Long cdOcorrencia) { this.cdOcorrencia = cdOcorrencia; }

    public LocalDateTime getDataApoio() { return dataApoio; }
    public void setDataApoio(LocalDateTime dataApoio) { this.dataApoio = dataApoio; }
}
