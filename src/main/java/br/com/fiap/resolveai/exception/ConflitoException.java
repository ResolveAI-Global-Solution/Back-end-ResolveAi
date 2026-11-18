package br.com.fiap.resolveai.exception;

/** Dado repetido ou registro ligado a outros (e-mail ja usado, item com filhos...). Vira 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
