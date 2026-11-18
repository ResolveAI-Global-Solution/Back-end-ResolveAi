package br.com.fiap.resolveai.exception;

/** Falha ao acessar o banco (vira 500). A mensagem nunca expoe o SQL; a causa so vai para o log. */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
