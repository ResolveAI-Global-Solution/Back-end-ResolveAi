package br.com.fiap.resolveai.dto;

import java.time.LocalDate;

/** Linha do placar por bairro: abertas, resolvidas, % resolvido, tempo medio e fonte dos dados. */
public class PlacarItemResponseDTO {

    private String regiao;
    private String municipio;
    private Integer abertas;
    private Integer resolvidas;
    private Double percentualResolvido;
    private Double tempoMedioResolucaoDias;
    private String fonte;
    private LocalDate atualizadoEm;

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public Integer getAbertas() { return abertas; }
    public void setAbertas(Integer abertas) { this.abertas = abertas; }

    public Integer getResolvidas() { return resolvidas; }
    public void setResolvidas(Integer resolvidas) { this.resolvidas = resolvidas; }

    public Double getPercentualResolvido() { return percentualResolvido; }
    public void setPercentualResolvido(Double percentualResolvido) { this.percentualResolvido = percentualResolvido; }

    public Double getTempoMedioResolucaoDias() { return tempoMedioResolucaoDias; }
    public void setTempoMedioResolucaoDias(Double tempoMedioResolucaoDias) { this.tempoMedioResolucaoDias = tempoMedioResolucaoDias; }

    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }

    public LocalDate getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDate atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
