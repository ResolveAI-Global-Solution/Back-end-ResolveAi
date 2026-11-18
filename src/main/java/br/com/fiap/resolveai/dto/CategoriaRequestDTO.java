package br.com.fiap.resolveai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada para cadastrar ou editar uma categoria. */
public class CategoriaRequestDTO {

    @NotBlank(message = "Informe o nome da categoria.")
    @Size(max = 60, message = "O nome da categoria pode ter até 60 caracteres.")
    private String nome;

    @Size(max = 200, message = "A descrição pode ter até 200 caracteres.")
    private String descricao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
