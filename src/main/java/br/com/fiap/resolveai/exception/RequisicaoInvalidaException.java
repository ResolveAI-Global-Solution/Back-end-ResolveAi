package br.com.fiap.resolveai.exception;

/** Dado obrigatorio faltando ou filtro inconsistente que so o service consegue checar (vira 400). */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
