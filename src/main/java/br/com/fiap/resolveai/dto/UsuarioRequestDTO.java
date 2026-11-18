package br.com.fiap.resolveai.dto;

import br.com.fiap.resolveai.model.TipoPlano;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Entrada do cadastro do morador. A senha chega em texto e e guardada so como hash. */
public class UsuarioRequestDTO {

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 100, message = "O nome pode ter até 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Confira o e-mail: ele parece incompleto.")
    @Size(max = 120, message = "O e-mail pode ter até 120 caracteres.")
    private String email;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 8, max = 60, message = "A senha precisa ter de 8 a 60 caracteres.")
    private String senha;

    @NotNull(message = "Escolha o seu bairro.")
    @Positive(message = "Escolha um bairro válido.")
    private Long cdBairro;

    @NotNull(message = "Escolha um plano: GRATUITO ou PREMIUM.")
    private TipoPlano plano;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Long getCdBairro() { return cdBairro; }
    public void setCdBairro(Long cdBairro) { this.cdBairro = cdBairro; }

    public TipoPlano getPlano() { return plano; }
    public void setPlano(TipoPlano plano) { this.plano = plano; }
}
