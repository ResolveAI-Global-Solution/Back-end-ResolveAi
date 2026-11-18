package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.Apoio;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a tb_apoio ("Eu tambem vi") com JDBC puro. A chave e o par (usuario, ocorrencia). */
@Repository
public class ApoioDAO {

    private final DataSource dataSource;

    public ApoioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT do apoio com data do banco (SYSDATE), dentro da transacao do service. */
    public void inserir(Connection conn, Apoio apoio) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tb_apoio (cd_usuario, cd_ocorrencia) VALUES (?, ?)")) {
            ps.setLong(1, apoio.getCdUsuario());
            ps.setLong(2, apoio.getCdOcorrencia());
            ps.executeUpdate();
        }
    }

    /** SELECT dos apoios de uma ocorrencia, do mais antigo para o mais recente. */
    public List<Apoio> listarPorOcorrencia(Long cdOcorrencia) {
        List<Apoio> apoios = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT cd_usuario, cd_ocorrencia, dt_apoio FROM tb_apoio WHERE cd_ocorrencia = ? ORDER BY dt_apoio")) {
            ps.setLong(1, cdOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    apoios.add(mapear(rs));
                }
            }
            return apoios;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar os apoios.", e);
        }
    }

    /** SELECT de um apoio pela chave (usuario, ocorrencia), dentro da transacao. */
    public Optional<Apoio> buscar(Connection conn, Long cdUsuario, Long cdOcorrencia) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT cd_usuario, cd_ocorrencia, dt_apoio FROM tb_apoio WHERE cd_usuario = ? AND cd_ocorrencia = ?")) {
            ps.setLong(1, cdUsuario);
            ps.setLong(2, cdOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** SELECT COUNT pela chave: diz se o usuario ja apoia a ocorrencia (um apoio por pessoa). */
    public boolean existe(Long cdUsuario, Long cdOcorrencia) {
        try (Connection conn = dataSource.getConnection()) {
            return buscar(conn, cdUsuario, cdOcorrencia).isPresent();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar o apoio.", e);
        }
    }

    /** COUNT de apoios da ocorrencia, usado para confirmar o problema ao chegar a 2 vizinhos. */
    public int contarPorOcorrencia(Connection conn, Long cdOcorrencia) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tb_apoio WHERE cd_ocorrencia = ?")) {
            ps.setLong(1, cdOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** DELETE do apoio pela chave; devolve false quando nao havia apoio para apagar. */
    public boolean remover(Long cdUsuario, Long cdOcorrencia) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM tb_apoio WHERE cd_usuario = ? AND cd_ocorrencia = ?")) {
            ps.setLong(1, cdUsuario);
            ps.setLong(2, cdOcorrencia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao remover o apoio.", e);
        }
    }

    private Apoio mapear(ResultSet rs) throws SQLException {
        return new Apoio(
                rs.getLong("cd_usuario"),
                rs.getLong("cd_ocorrencia"),
                JdbcUtil.paraData(rs.getTimestamp("dt_apoio")));
    }
}
