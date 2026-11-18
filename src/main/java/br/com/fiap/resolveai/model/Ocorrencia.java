package br.com.fiap.resolveai.model;

import java.time.LocalDateTime;

/**
 * Problema urbano registrado por um morador: raiz do dominio.
 * Guarda os ids das FKs e, para leitura, os nomes de categoria, bairro e municipio e a quantidade de apoios.
 */
public class Ocorrencia {

    private Long id;
    private Long cdUsuario;
    private Long cdCategoria;
    private Long cdBairro;
    private String titulo;
    private String descricao;
    private String endereco;
    private Double latitude;
    private Double longitude;
    private StatusOcorrencia status;
    private LocalDateTime dataRegistro;
    private LocalDateTime dataResolucao;
    private Boolean oculta;

    private String nomeCategoria;
    private String nomeBairro;
    private String nomeMunicipio;
    private Integer quantidadeApoios;

    public Ocorrencia() {
    }

    public Ocorrencia(Long id, Long cdUsuario, Long cdCategoria, Long cdBairro, String titulo, String descricao,
                      String endereco, Double latitude, Double longitude, StatusOcorrencia status,
                      LocalDateTime dataRegistro, LocalDateTime dataResolucao, Boolean oculta,
                      String nomeCategoria, String nomeBairro, String nomeMunicipio, Integer quantidadeApoios) {
        this.id = id;
        this.cdUsuario = cdUsuario;
        this.cdCategoria = cdCategoria;
        this.cdBairro = cdBairro;
        this.titulo = titulo;
        this.descricao = descricao;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.dataRegistro = dataRegistro;
        this.dataResolucao = dataResolucao;
        this.oculta = oculta;
        this.nomeCategoria = nomeCategoria;
        this.nomeBairro = nomeBairro;
        this.nomeMunicipio = nomeMunicipio;
        this.quantidadeApoios = quantidadeApoios;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    public LocalDateTime getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(LocalDateTime dataResolucao) { this.dataResolucao = dataResolucao; }

    public Boolean getOculta() { return oculta; }
    public void setOculta(Boolean oculta) { this.oculta = oculta; }

    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }

    public String getNomeBairro() { return nomeBairro; }
    public void setNomeBairro(String nomeBairro) { this.nomeBairro = nomeBairro; }

    public String getNomeMunicipio() { return nomeMunicipio; }
    public void setNomeMunicipio(String nomeMunicipio) { this.nomeMunicipio = nomeMunicipio; }

    public Integer getQuantidadeApoios() { return quantidadeApoios; }
    public void setQuantidadeApoios(Integer quantidadeApoios) { this.quantidadeApoios = quantidadeApoios; }
}
