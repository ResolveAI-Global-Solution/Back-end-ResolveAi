package br.com.fiap.resolveai.model;

import java.time.LocalDateTime;

/** "Eu tambem vi": apoio de um morador a uma ocorrencia (associativa N:N usuario x ocorrencia). */
public class Apoio {

    private Long cdUsuario;
    private Long cdOcorrencia;
    private LocalDateTime dataApoio;

    public Apoio() {
    }

    public Apoio(Long cdUsuario, Long cdOcorrencia, LocalDateTime dataApoio) {
        this.cdUsuario = cdUsuario;
        this.cdOcorrencia = cdOcorrencia;
        this.dataApoio = dataApoio;
    }

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public Long getCdOcorrencia() { return cdOcorrencia; }
    public void setCdOcorrencia(Long cdOcorrencia) { this.cdOcorrencia = cdOcorrencia; }

    public LocalDateTime getDataApoio() { return dataApoio; }
    public void setDataApoio(LocalDateTime dataApoio) { this.dataApoio = dataApoio; }
}
