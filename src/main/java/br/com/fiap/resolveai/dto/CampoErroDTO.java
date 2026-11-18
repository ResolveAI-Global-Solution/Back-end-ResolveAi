package br.com.fiap.resolveai.dto;

/** Campo que nao passou na validacao e a mensagem para o usuario. */
public class CampoErroDTO {

    private String campo;
    private String mensagem;

    public CampoErroDTO() {
    }

    public CampoErroDTO(String campo, String mensagem) {
        this.campo = campo;
        this.mensagem = mensagem;
    }

    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
