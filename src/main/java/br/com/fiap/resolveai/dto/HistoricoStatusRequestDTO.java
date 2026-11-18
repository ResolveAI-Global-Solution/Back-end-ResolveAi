package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.StatusOcorrencia;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entrada para mudar o status de uma ocorrencia. Quem informa RESOLVIDA ("Foi resolvido") ou
 * REABERTA ("Contestar com foto") tambem manda cdUsuario e a posicao da foto; o service confere.
 */
public class HistoricoStatusRequestDTO {

    @NotNull(message = "Informe o novo status da ocorrência.")
    private StatusOcorrencia status;

    @Size(max = 200, message = "A observação pode ter até 200 caracteres.")
    private String observacao;

    @Positive(message = "Informe um usuário válido.")
    private Long cdUsuario;

    @DecimalMin(value = "-90", message = "Latitude fora do mapa.")
    @DecimalMax(value = "90", message = "Latitude fora do mapa.")
    private Double latitude;

    @DecimalMin(value = "-180", message = "Longitude fora do mapa.")
    @DecimalMax(value = "180", message = "Longitude fora do mapa.")
    private Double longitude;

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
