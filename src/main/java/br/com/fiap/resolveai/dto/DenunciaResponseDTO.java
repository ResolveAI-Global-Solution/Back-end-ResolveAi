package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.MotivoDenuncia;
import br.com.fiap.resolveai.model.StatusDenuncia;
import br.com.fiap.resolveai.model.StatusOcorrencia;

import java.time.LocalDateTime;

/** Saida de uma denuncia com os dados da ocorrencia denunciada. Nunca mostra quem denunciou. */
public class DenunciaResponseDTO {

    private Long id;
    private Long cdOcorrencia;
    private String tituloOcorrencia;
    private String descricaoOcorrencia;
    private StatusOcorrencia statusOcorrencia;
    private MotivoDenuncia motivo;
    private String comentario;
    private LocalDateTime dataDenuncia;
    private StatusDenuncia status;
    private String moderador;
    private String decisao;
    private LocalDateTime dataDecisao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCdOcorrencia() { return cdOcorrencia; }
    public void setCdOcorrencia(Long cdOcorrencia) { this.cdOcorrencia = cdOcorrencia; }

    public String getTituloOcorrencia() { return tituloOcorrencia; }
    public void setTituloOcorrencia(String tituloOcorrencia) { this.tituloOcorrencia = tituloOcorrencia; }

    public String getDescricaoOcorrencia() { return descricaoOcorrencia; }
    public void setDescricaoOcorrencia(String descricaoOcorrencia) { this.descricaoOcorrencia = descricaoOcorrencia; }

    public StatusOcorrencia getStatusOcorrencia() { return statusOcorrencia; }
    public void setStatusOcorrencia(StatusOcorrencia statusOcorrencia) { this.statusOcorrencia = statusOcorrencia; }

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
}
