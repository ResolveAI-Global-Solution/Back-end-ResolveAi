package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.StatusOcorrencia;

import java.time.LocalDateTime;

/** Saida publica de uma ocorrencia (lista do mapa e Detalhe). Nao traz o autor, por privacidade. */
public class OcorrenciaResponseDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String endereco;
    private String bairro;
    private String municipio;
    private CategoriaResumoDTO categoria;
    private StatusOcorrencia status;
    private Integer quantidadeApoios;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dataRegistro;
    private LocalDateTime dataResolucao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public CategoriaResumoDTO getCategoria() { return categoria; }
    public void setCategoria(CategoriaResumoDTO categoria) { this.categoria = categoria; }

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public Integer getQuantidadeApoios() { return quantidadeApoios; }
    public void setQuantidadeApoios(Integer quantidadeApoios) { this.quantidadeApoios = quantidadeApoios; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    public LocalDateTime getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(LocalDateTime dataResolucao) { this.dataResolucao = dataResolucao; }
}
