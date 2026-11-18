package br.com.fiap.resolveai.exception;

/** Recurso pedido nao existe (vira 404). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
