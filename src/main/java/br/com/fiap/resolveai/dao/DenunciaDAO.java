package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.Denuncia;
import br.com.fiap.resolveai.model.MotivoDenuncia;
import br.com.fiap.resolveai.model.StatusDenuncia;
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

/** Acesso a tb_denuncia (denuncia de publicacao e decisao da moderacao) com JDBC puro. */
@Repository
public class DenunciaDAO {

    private static final String SELECT_BASE =
            "SELECT d.cd_denuncia, d.cd_ocorrencia, d.cd_usuario, d.tp_motivo, d.ds_comentario, d.dt_denuncia, " +
            "d.st_denuncia, d.nm_moderador, d.ds_decisao, d.dt_decisao, " +
            "o.ds_titulo, o.ds_descricao, o.st_ocorrencia " +
            "FROM tb_denuncia d JOIN tb_ocorrencia o ON o.cd_ocorrencia = d.cd_ocorrencia";

    private final DataSource dataSource;

    public DenunciaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT da denuncia com id de sq_tb_denuncia, status PENDENTE e data do banco (SYSDATE). */
    public long inserir(Connection conn, Denuncia denuncia) throws SQLException {
        String sql = "INSERT INTO tb_denuncia (cd_denuncia, cd_ocorrencia, cd_usuario, tp_motivo, ds_comentario) " +
                     "VALUES (?, ?, ?, ?, ?)";
        long id = JdbcUtil.proximoId(conn, "sq_tb_denuncia");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, denuncia.getCdOcorrencia());
            ps.setLong(3, denuncia.getCdUsuario());
            ps.setString(4, denuncia.getMotivo().name());
            ps.setString(5, denuncia.getComentario());
            ps.executeUpdate();
        }
        return id;
    }

    /** SELECT das denuncias (filtro opcional de status), da mais antiga para a mais nova: fila da moderacao. */
    public List<Denuncia> listar(StatusDenuncia status) {
        String sql = SELECT_BASE + (status != null ? " WHERE d.st_denuncia = ?" : "") +
                     " ORDER BY d.dt_denuncia, d.cd_denuncia";
        List<Denuncia> denuncias = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (status != null) {
                ps.setString(1, status.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    denuncias.add(mapear(rs));
                }
            }
            return denuncias;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar as denuncias.", e);
        }
    }

    /** SELECT por cd_denuncia com os dados da ocorrencia denunciada. */
    public Optional<Denuncia> buscarPorId(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            return buscarPorId(conn, id);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar a denuncia.", e);
        }
    }

    /** Mesmo SELECT por id, dentro de uma transacao ja aberta. */
    public Optional<Denuncia> buscarPorId(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE d.cd_denuncia = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** UPDATE com a decisao do moderador (MANTIDA ou REMOVIDA), quem decidiu, o motivo e a data (SYSDATE). */
    public void registrarDecisao(Connection conn, Long id, StatusDenuncia decisao, String moderador, String motivo)
            throws SQLException {
        String sql = "UPDATE tb_denuncia SET st_denuncia = ?, nm_moderador = ?, ds_decisao = ?, dt_decisao = SYSDATE " +
                     "WHERE cd_denuncia = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, decisao.name());
            ps.setString(2, moderador);
            ps.setString(3, motivo);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    /** SELECT COUNT pelo par (usuario, ocorrencia): cada conta denuncia uma publicacao uma vez so. */
    public boolean existe(Long cdUsuario, Long cdOcorrencia) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM tb_denuncia WHERE cd_usuario = ? AND cd_ocorrencia = ?")) {
            ps.setLong(1, cdUsuario);
            ps.setLong(2, cdOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar a denuncia.", e);
        }
    }

    /** COUNT de denuncias PENDENTE da ocorrencia: com 3 ela sai do mapa ate a moderacao decidir. */
    public int contarPendentesPorOcorrencia(Connection conn, Long cdOcorrencia) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM tb_denuncia WHERE cd_ocorrencia = ? AND st_denuncia = 'PENDENTE'")) {
            ps.setLong(1, cdOcorrencia);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Denuncia mapear(ResultSet rs) throws SQLException {
        return new Denuncia(
                rs.getLong("cd_denuncia"),
                rs.getLong("cd_ocorrencia"),
                rs.getLong("cd_usuario"),
                MotivoDenuncia.valueOf(rs.getString("tp_motivo")),
                rs.getString("ds_comentario"),
                JdbcUtil.paraData(rs.getTimestamp("dt_denuncia")),
                StatusDenuncia.valueOf(rs.getString("st_denuncia")),
                rs.getString("nm_moderador"),
                rs.getString("ds_decisao"),
                JdbcUtil.paraData(rs.getTimestamp("dt_decisao")),
                rs.getString("ds_titulo"),
                rs.getString("ds_descricao"),
                StatusOcorrencia.valueOf(rs.getString("st_ocorrencia")));
    }
}
