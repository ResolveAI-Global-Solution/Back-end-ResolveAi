package br.com.fiap.resolveai.model;

import java.time.LocalDateTime;

/** Registro de uma mudanca de status de uma ocorrencia (Ocorrencia 1:N HistoricoStatus). */
public class HistoricoStatus {

    private Long id;
    private Long cdOcorrencia;
    private StatusOcorrencia status;
    private LocalDateTime dataStatus;
    private String observacao;

    public HistoricoStatus() {
    }

    public HistoricoStatus(Long id, Long cdOcorrencia, StatusOcorrencia status,
                           LocalDateTime dataStatus, String observacao) {
        this.id = id;
        this.cdOcorrencia = cdOcorrencia;
        this.status = status;
        this.dataStatus = dataStatus;
        this.observacao = observacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCdOcorrencia() { return cdOcorrencia; }
    public void setCdOcorrencia(Long cdOcorrencia) { this.cdOcorrencia = cdOcorrencia; }

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public LocalDateTime getDataStatus() { return dataStatus; }
    public void setDataStatus(LocalDateTime dataStatus) { this.dataStatus = dataStatus; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
