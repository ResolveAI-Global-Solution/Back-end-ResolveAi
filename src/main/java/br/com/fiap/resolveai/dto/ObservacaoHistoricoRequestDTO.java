package br.com.fiap.resolveai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada para corrigir a observacao de um registro de historico (o status gravado nao muda). */
public class ObservacaoHistoricoRequestDTO {

    @NotBlank(message = "Escreva a observação corrigida.")
    @Size(max = 200, message = "A observação pode ter até 200 caracteres.")
    private String observacao;

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
