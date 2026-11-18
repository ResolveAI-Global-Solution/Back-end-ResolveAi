package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Executa varias operacoes de DAO na mesma conexao, com setAutoCommit(false):
 * se tudo der certo faz commit, se qualquer passo falhar faz rollback.
 */
@Component
public class Transacao {

    /** Bloco de codigo que recebe a conexao da transacao. */
    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection conn) throws SQLException;
    }

    private final DataSource dataSource;

    public Transacao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executar(Operacao<T> operacao) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao gravar os dados.", e);
        }
    }
}
