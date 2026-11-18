package br.com.fiap.resolveai.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entrada para registrar (ou editar) uma ocorrencia. Latitude e longitude sao a posicao do aparelho
 * no momento da foto. O autor (cdUsuario) so entra aqui, nunca aparece na resposta.
 */
public class OcorrenciaRequestDTO {

    @NotNull(message = "Informe quem está registrando a ocorrência.")
    @Positive(message = "Informe um usuário válido.")
    private Long cdUsuario;

    @NotNull(message = "Escolha a categoria do problema.")
    @Positive(message = "Escolha uma categoria válida.")
    private Long cdCategoria;

    @NotNull(message = "Escolha o bairro do problema.")
    @Positive(message = "Escolha um bairro válido.")
    private Long cdBairro;

    @NotBlank(message = "Informe um título de até 120 caracteres.")
    @Size(max = 120, message = "Informe um título de até 120 caracteres.")
    private String titulo;

    @Size(max = 500, message = "A descrição pode ter até 500 caracteres.")
    private String descricao;

    @Size(max = 150, message = "O endereço pode ter até 150 caracteres.")
    private String endereco;

    @NotNull(message = "Não recebemos a localização da foto. Ative o GPS e tente de novo.")
    @DecimalMin(value = "-90", message = "Latitude fora do mapa.")
    @DecimalMax(value = "90", message = "Latitude fora do mapa.")
    private Double latitude;

    @NotNull(message = "Não recebemos a localização da foto. Ative o GPS e tente de novo.")
    @DecimalMin(value = "-180", message = "Longitude fora do mapa.")
    @DecimalMax(value = "180", message = "Longitude fora do mapa.")
    private Double longitude;

    private Boolean registrarMesmoAssim = false;

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public Long getCdCategoria() { return cdCategoria; }
    public void setCdCategoria(Long cdCategoria) { this.cdCategoria = cdCategoria; }

    public Long getCdBairro() { return cdBairro; }
    public void setCdBairro(Long cdBairro) { this.cdBairro = cdBairro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getRegistrarMesmoAssim() { return registrarMesmoAssim; }
    public void setRegistrarMesmoAssim(Boolean registrarMesmoAssim) { this.registrarMesmoAssim = registrarMesmoAssim; }
}
