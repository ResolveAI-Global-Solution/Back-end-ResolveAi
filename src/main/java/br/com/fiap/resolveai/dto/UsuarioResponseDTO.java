package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.TipoPlano;

import java.time.LocalDateTime;

/** Saida com os dados do morador. Nunca traz senha nem hash. */
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private Long cdBairro;
    private String bairro;
    private TipoPlano plano;
    private LocalDateTime dataCadastro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getCdBairro() { return cdBairro; }
    public void setCdBairro(Long cdBairro) { this.cdBairro = cdBairro; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public TipoPlano getPlano() { return plano; }
    public void setPlano(TipoPlano plano) { this.plano = plano; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
