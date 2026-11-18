package br.com.fiap.resolveai.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Formato unico de erro da API: nunca HTML, stack trace ou SQL. */
public class ErroResponseDTO {

    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<CampoErroDTO> campos;

    public ErroResponseDTO() {
    }

    public ErroResponseDTO(LocalDateTime timestamp, Integer status, String erro, String mensagem,
                           String caminho, List<CampoErroDTO> campos) {
        this.timestamp = timestamp;
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getCaminho() { return caminho; }
    public void setCaminho(String caminho) { this.caminho = caminho; }

    public List<CampoErroDTO> getCampos() { return campos; }
    public void setCampos(List<CampoErroDTO> campos) { this.campos = campos; }
}
