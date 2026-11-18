package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.HistoricoStatus;
import br.com.fiap.resolveai.model.StatusOcorrencia;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a tb_historico_status (Ocorrencia 1:N HistoricoStatus) com JDBC puro. */
@Repository
public class HistoricoStatusDAO {

    private static final String SELECT_BASE =
            "SELECT cd_historico, cd_ocorrencia, st_status, dt_status, ds_observacao FROM tb_historico_status";

    private final DataSource dataSource;

    public HistoricoStatusDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT de um registro de historico com id de sq_tb_historico_status e data do banco (SYSDATE). */
    public long inserir(Connection conn, Long cdOcorrencia, StatusOcorrencia status, String observacao)
            throws SQLException {
        String sql = "INSERT INTO tb_historico_status (cd_historico, cd_ocorrencia, st_status, ds_observacao) " +
                     "VALUES (?, ?, ?, ?)";
        long id = JdbcUtil.proximoId(conn, "sq_tb_historico_status");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, cdOcorrencia);
            ps.setString(3, status.name());
            ps.setString(4, observacao);
            ps.executeUpdate();
        }
        return id;
    }

    /** SELECT do historico de uma ocorrencia, do mais antigo para o mais recente (linha do tempo do Detalhe). */
    public List<HistoricoStatus> listarPorOcorrencia(Long cdOcorrencia) {
        List<HistoricoStatus> historico = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + " WHERE cd_ocorrencia = ? ORDER BY dt_status, cd_historico")) {
            ps.setLong(1, cdOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historico.add(mapear(rs));
                }
            }
            return historico;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar o historico.", e);
        }
    }

    /** SELECT por cd_historico; vazio quando nao existe. */
    public Optional<HistoricoStatus> buscarPorId(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE cd_historico = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar o historico.", e);
        }
    }

    /** UPDATE so da observacao: o status registrado nao muda depois de gravado. */
    public void atualizar(HistoricoStatus historico) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE tb_historico_status SET ds_observacao = ? WHERE cd_historico = ?")) {
            ps.setString(1, historico.getObservacao());
            ps.setLong(2, historico.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao atualizar o historico.", e);
        }
    }

    /** DELETE de um registro de historico. */
    public void remover(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_historico_status WHERE cd_historico = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao remover o historico.", e);
        }
    }

    /** DELETE de todo o historico de uma ocorrencia, antes de remover a propria ocorrencia. */
    public void removerPorOcorrencia(Connection conn, Long cdOcorrencia) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tb_historico_status WHERE cd_ocorrencia = ?")) {
            ps.setLong(1, cdOcorrencia);
            ps.executeUpdate();
        }
    }

    private HistoricoStatus mapear(ResultSet rs) throws SQLException {
        return new HistoricoStatus(
                rs.getLong("cd_historico"),
                rs.getLong("cd_ocorrencia"),
                StatusOcorrencia.valueOf(rs.getString("st_status")),
                JdbcUtil.paraData(rs.getTimestamp("dt_status")),
                rs.getString("ds_observacao"));
    }
}
