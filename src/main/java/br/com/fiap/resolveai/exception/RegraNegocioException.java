package br.com.fiap.resolveai.exception;

/** Pedido valido no formato, mas que quebra uma regra do ResolveAI (vira 409). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
