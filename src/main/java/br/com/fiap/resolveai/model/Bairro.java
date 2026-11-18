package br.com.fiap.resolveai.model;

/** Bairro ou comunidade de um municipio (Municipio 1:N Bairro). */
public class Bairro {

    private Long id;
    private Long cdMunicipio;
    private String nome;
    private Boolean comunidade;
    private String nomeMunicipio;

    public Bairro() {
    }

    public Bairro(Long id, Long cdMunicipio, String nome, Boolean comunidade, String nomeMunicipio) {
        this.id = id;
        this.cdMunicipio = cdMunicipio;
        this.nome = nome;
        this.comunidade = comunidade;
        this.nomeMunicipio = nomeMunicipio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCdMunicipio() { return cdMunicipio; }
    public void setCdMunicipio(Long cdMunicipio) { this.cdMunicipio = cdMunicipio; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Boolean getComunidade() { return comunidade; }
    public void setComunidade(Boolean comunidade) { this.comunidade = comunidade; }

    public String getNomeMunicipio() { return nomeMunicipio; }
    public void setNomeMunicipio(String nomeMunicipio) { this.nomeMunicipio = nomeMunicipio; }
}
