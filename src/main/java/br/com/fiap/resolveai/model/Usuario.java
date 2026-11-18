package br.com.fiap.resolveai.model;

import java.time.LocalDateTime;

/** Morador cadastrado no aplicativo. A senha so existe aqui como hash BCrypt. */
public class Usuario {

    private Long id;
    private Long cdBairro;
    private String nome;
    private String email;
    private String senhaHash;
    private TipoPlano plano;
    private LocalDateTime dataCadastro;
    private String nomeBairro;

    public Usuario() {
    }

    public Usuario(Long id, Long cdBairro, String nome, String email, String senhaHash,
                   TipoPlano plano, LocalDateTime dataCadastro, String nomeBairro) {
        this.id = id;
        this.cdBairro = cdBairro;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.plano = plano;
        this.dataCadastro = dataCadastro;
        this.nomeBairro = nomeBairro;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCdBairro() { return cdBairro; }
    public void setCdBairro(Long cdBairro) { this.cdBairro = cdBairro; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public TipoPlano getPlano() { return plano; }
    public void setPlano(TipoPlano plano) { this.plano = plano; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getNomeBairro() { return nomeBairro; }
    public void setNomeBairro(String nomeBairro) { this.nomeBairro = nomeBairro; }
}
