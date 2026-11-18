package br.com.fiap.resolveai.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Entrada do "Eu tambem vi": quem apoia e onde a pessoa esta agora. */
public class ApoioRequestDTO {

    @NotNull(message = "Informe quem está apoiando.")
    @Positive(message = "Informe um usuário válido.")
    private Long cdUsuario;

    @NotNull(message = "Não recebemos sua localização. Ative o GPS e tente de novo.")
    @DecimalMin(value = "-90", message = "Latitude fora do mapa.")
    @DecimalMax(value = "90", message = "Latitude fora do mapa.")
    private Double latitude;

    @NotNull(message = "Não recebemos sua localização. Ative o GPS e tente de novo.")
    @DecimalMin(value = "-180", message = "Longitude fora do mapa.")
    @DecimalMax(value = "180", message = "Longitude fora do mapa.")
    private Double longitude;

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
