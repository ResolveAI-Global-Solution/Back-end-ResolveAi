package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.StatusOcorrencia;

import java.time.LocalDateTime;

/** Saida de um registro do historico de status. */
public class HistoricoStatusResponseDTO {

    private Long id;
    private Long cdOcorrencia;
    private StatusOcorrencia status;
    private LocalDateTime dataStatus;
    private String observacao;

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
