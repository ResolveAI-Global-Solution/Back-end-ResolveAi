package br.com.fiap.resolveai.model;

/** Linha do placar: resultado agregado das ocorrencias de um bairro. */
public class PlacarItem {

    private String regiao;
    private String municipio;
    private Integer abertas;
    private Integer resolvidas;
    private Double tempoMedioResolucaoDias;

    public PlacarItem() {
    }

    public PlacarItem(String regiao, String municipio, Integer abertas, Integer resolvidas,
                      Double tempoMedioResolucaoDias) {
        this.regiao = regiao;
        this.municipio = municipio;
        this.abertas = abertas;
        this.resolvidas = resolvidas;
        this.tempoMedioResolucaoDias = tempoMedioResolucaoDias;
    }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public Integer getAbertas() { return abertas; }
    public void setAbertas(Integer abertas) { this.abertas = abertas; }

    public Integer getResolvidas() { return resolvidas; }
    public void setResolvidas(Integer resolvidas) { this.resolvidas = resolvidas; }

    public Double getTempoMedioResolucaoDias() { return tempoMedioResolucaoDias; }
    public void setTempoMedioResolucaoDias(Double tempoMedioResolucaoDias) { this.tempoMedioResolucaoDias = tempoMedioResolucaoDias; }
}
