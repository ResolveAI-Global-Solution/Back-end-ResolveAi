package br.com.fiap.resolveai.model;

import java.time.LocalDateTime;

/**
 * Denuncia de uma publicacao feita por um morador e a decisao do moderador.
 * Para leitura, traz tambem titulo, descricao e status da ocorrencia denunciada.
 */
public class Denuncia {

    private Long id;
    private Long cdOcorrencia;
    private Long cdUsuario;
    private MotivoDenuncia motivo;
    private String comentario;
    private LocalDateTime dataDenuncia;
    private StatusDenuncia status;
    private String moderador;
    private String decisao;
    private LocalDateTime dataDecisao;

    private String tituloOcorrencia;
    private String descricaoOcorrencia;
    private StatusOcorrencia statusOcorrencia;

    public Denuncia() {
    }

    public Denuncia(Long id, Long cdOcorrencia, Long cdUsuario, MotivoDenuncia motivo, String comentario,
                    LocalDateTime dataDenuncia, StatusDenuncia status, String moderador, String decisao,
                    LocalDateTime dataDecisao, String tituloOcorrencia, String descricaoOcorrencia,
                    StatusOcorrencia statusOcorrencia) {
        this.id = id;
        this.cdOcorrencia = cdOcorrencia;
        this.cdUsuario = cdUsuario;
        this.motivo = motivo;
        this.comentario = comentario;
        this.dataDenuncia = dataDenuncia;
        this.status = status;
        this.moderador = moderador;
        this.decisao = decisao;
        this.dataDecisao = dataDecisao;
        this.tituloOcorrencia = tituloOcorrencia;
        this.descricaoOcorrencia = descricaoOcorrencia;
        this.statusOcorrencia = statusOcorrencia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCdOcorrencia() { return cdOcorrencia; }
    public void setCdOcorrencia(Long cdOcorrencia) { this.cdOcorrencia = cdOcorrencia; }

    public Long getCdUsuario() { return cdUsuario; }
    public void setCdUsuario(Long cdUsuario) { this.cdUsuario = cdUsuario; }

    public MotivoDenuncia getMotivo() { return motivo; }
    public void setMotivo(MotivoDenuncia motivo) { this.motivo = motivo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataDenuncia() { return dataDenuncia; }
    public void setDataDenuncia(LocalDateTime dataDenuncia) { this.dataDenuncia = dataDenuncia; }

    public StatusDenuncia getStatus() { return status; }
    public void setStatus(StatusDenuncia status) { this.status = status; }

    public String getModerador() { return moderador; }
    public void setModerador(String moderador) { this.moderador = moderador; }

    public String getDecisao() { return decisao; }
    public void setDecisao(String decisao) { this.decisao = decisao; }

    public LocalDateTime getDataDecisao() { return dataDecisao; }
    public void setDataDecisao(LocalDateTime dataDecisao) { this.dataDecisao = dataDecisao; }

    public String getTituloOcorrencia() { return tituloOcorrencia; }
    public void setTituloOcorrencia(String tituloOcorrencia) { this.tituloOcorrencia = tituloOcorrencia; }

    public String getDescricaoOcorrencia() { return descricaoOcorrencia; }
    public void setDescricaoOcorrencia(String descricaoOcorrencia) { this.descricaoOcorrencia = descricaoOcorrencia; }

    public StatusOcorrencia getStatusOcorrencia() { return statusOcorrencia; }
    public void setStatusOcorrencia(StatusOcorrencia statusOcorrencia) { this.statusOcorrencia = statusOcorrencia; }
}
