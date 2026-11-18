package br.com.fiap.resolveai.dto;

/** Saida com os dados de um bairro e o nome do municipio. */
public class BairroResponseDTO {

    private Long id;
    private String nome;
    private Boolean comunidade;
    private Long cdMunicipio;
    private String municipio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Boolean getComunidade() { return comunidade; }
    public void setComunidade(Boolean comunidade) { this.comunidade = comunidade; }

    public Long getCdMunicipio() { return cdMunicipio; }
    public void setCdMunicipio(Long cdMunicipio) { this.cdMunicipio = cdMunicipio; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
}
