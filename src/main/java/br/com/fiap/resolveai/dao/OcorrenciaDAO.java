package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.Ocorrencia;
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

/**
 * Acesso a tb_ocorrencia com JDBC puro. As leituras trazem categoria, bairro e municipio por JOIN
 * e a quantidade de apoios por subconsulta COUNT em tb_apoio.
 * Metodos que recebem Connection participam de uma transacao aberta pelo service.
 */
@Repository
public class OcorrenciaDAO {

    private static final String SELECT_BASE =
            "SELECT o.cd_ocorrencia, o.cd_usuario, o.cd_categoria, o.cd_bairro, o.ds_titulo, o.ds_descricao, " +
            "o.ds_endereco, o.nr_latitude, o.nr_longitude, o.st_ocorrencia, o.dt_registro, o.dt_resolucao, " +
            "o.ic_oculta, c.nm_categoria, b.nm_bairro, m.nm_municipio, " +
            "(SELECT COUNT(*) FROM tb_apoio a WHERE a.cd_ocorrencia = o.cd_ocorrencia) AS qt_apoios " +
            "FROM tb_ocorrencia o " +
            "JOIN tb_categoria c ON c.cd_categoria = o.cd_categoria " +
            "JOIN tb_bairro b ON b.cd_bairro = o.cd_bairro " +
            "JOIN tb_municipio m ON m.cd_municipio = b.cd_municipio";

    private final DataSource dataSource;

    public OcorrenciaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT da ocorrencia com id de sq_tb_ocorrencia, status ABERTA e data de registro do banco (SYSDATE). */
    public long inserir(Connection conn, Ocorrencia ocorrencia) throws SQLException {
        String sql = "INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_usuario, cd_categoria, cd_bairro, ds_titulo, " +
                     "ds_descricao, ds_endereco, nr_latitude, nr_longitude, st_ocorrencia) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ABERTA')";
        long id = JdbcUtil.proximoId(conn, "sq_tb_ocorrencia");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, ocorrencia.getCdUsuario());
            ps.setLong(3, ocorrencia.getCdCategoria());
            ps.setLong(4, ocorrencia.getCdBairro());
            ps.setString(5, ocorrencia.getTitulo());
            ps.setString(6, ocorrencia.getDescricao());
            ps.setString(7, ocorrencia.getEndereco());
            ps.setDouble(8, ocorrencia.getLatitude());
            ps.setDouble(9, ocorrencia.getLongitude());
            ps.executeUpdate();
        }
        return id;
    }

    /**
     * SELECT das ocorrencias visiveis no mapa (nunca ocultas nem REJEITADA), com filtros opcionais
     * de status, categoria e bairro; os filtros entram como parametros do PreparedStatement.
     */
    public List<Ocorrencia> listar(StatusOcorrencia status, Long cdCategoria, Long cdBairro) {
        StringBuilder sql = new StringBuilder(SELECT_BASE)
                .append(" WHERE o.ic_oculta = 'N' AND o.st_ocorrencia <> 'REJEITADA'");
        List<Object> parametros = new ArrayList<>();
        if (status != null) {
            sql.append(" AND o.st_ocorrencia = ?");
            parametros.add(status.name());
        }
        if (cdCategoria != null) {
            sql.append(" AND o.cd_categoria = ?");
            parametros.add(cdCategoria);
        }
        if (cdBairro != null) {
            sql.append(" AND o.cd_bairro = ?");
            parametros.add(cdBairro);
        }
        sql.append(" ORDER BY o.dt_registro DESC, o.cd_ocorrencia DESC");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            return executarLista(ps);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar as ocorrencias.", e);
        }
    }

    /** SELECT das ocorrencias registradas por um usuario (tela Perfil: "Minhas ocorrencias"). */
    public List<Ocorrencia> listarPorUsuario(Long cdUsuario) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + " WHERE o.cd_usuario = ? ORDER BY o.dt_registro DESC, o.cd_ocorrencia DESC")) {
            ps.setLong(1, cdUsuario);
            return executarLista(ps);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar as ocorrencias do usuario.", e);
        }
    }

    /**
     * SELECT das ocorrencias ainda em aberto (ABERTA, CONFIRMADA, REABERTA) e nao ocultas da mesma categoria.
     * O service calcula a distancia de cada uma para achar as parecidas a ate 30 m.
     */
    public List<Ocorrencia> buscarParecidas(Long cdCategoria) {
        String sql = SELECT_BASE + " WHERE o.cd_categoria = ? AND o.ic_oculta = 'N' " +
                     "AND o.st_ocorrencia IN ('ABERTA', 'CONFIRMADA', 'REABERTA')";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cdCategoria);
            return executarLista(ps);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar ocorrencias parecidas.", e);
        }
    }

    /** SELECT por cd_ocorrencia com categoria, bairro, municipio e quantidade de apoios. */
    public Optional<Ocorrencia> buscarPorId(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            return buscarPorId(conn, id);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar a ocorrencia.", e);
        }
    }

    /** Mesmo SELECT por id, dentro de uma transacao ja aberta. */
    public Optional<Ocorrencia> buscarPorId(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE o.cd_ocorrencia = ?")) {
            ps.setLong(1, id);
            List<Ocorrencia> lista = executarLista(ps);
            return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
        }
    }

    /** UPDATE dos dados editaveis (categoria, bairro, textos e coordenadas); autor e status nao mudam aqui. */
    public void atualizar(Ocorrencia ocorrencia) {
        String sql = "UPDATE tb_ocorrencia SET cd_categoria = ?, cd_bairro = ?, ds_titulo = ?, ds_descricao = ?, " +
                     "ds_endereco = ?, nr_latitude = ?, nr_longitude = ? WHERE cd_ocorrencia = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ocorrencia.getCdCategoria());
            ps.setLong(2, ocorrencia.getCdBairro());
            ps.setString(3, ocorrencia.getTitulo());
            ps.setString(4, ocorrencia.getDescricao());
            ps.setString(5, ocorrencia.getEndereco());
            ps.setDouble(6, ocorrencia.getLatitude());
            ps.setDouble(7, ocorrencia.getLongitude());
            ps.setLong(8, ocorrencia.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao atualizar a ocorrencia.", e);
        }
    }

    /**
     * UPDATE de st_ocorrencia, sempre junto com o INSERT no historico (mesma transacao).
     * RESOLVIDA grava dt_resolucao com o SYSDATE do banco (mesmo relogio de dt_registro);
     * REABERTA limpa dt_resolucao; os demais status mantem o valor atual.
     */
    public void atualizarStatus(Connection conn, Long id, StatusOcorrencia status) throws SQLException {
        String sql = "UPDATE tb_ocorrencia SET st_ocorrencia = ?, " +
                     "dt_resolucao = CASE ? WHEN 'RESOLVIDA' THEN SYSDATE WHEN 'REABERTA' THEN NULL ELSE dt_resolucao END " +
                     "WHERE cd_ocorrencia = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, status.name());
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    /** UPDATE de ic_oculta: tira ('S') ou devolve ('N') a ocorrencia do mapa e do placar. */
    public void marcarOculta(Connection conn, Long id, boolean oculta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tb_ocorrencia SET ic_oculta = ? WHERE cd_ocorrencia = ?")) {
            ps.setString(1, oculta ? "S" : "N");
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /** DELETE da ocorrencia (o historico inicial e apagado antes, na mesma transacao). */
    public void remover(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_ocorrencia WHERE cd_ocorrencia = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Conta o que esta ligado a ocorrencia: apoios + denuncias + historico alem do registro inicial ABERTA.
     * Se der mais que zero, a ocorrencia ja teve acompanhamento e nao pode ser apagada.
     */
    public int contarFilhos(Long id) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_apoio WHERE cd_ocorrencia = ?) " +
                     "     + (SELECT COUNT(*) FROM tb_denuncia WHERE cd_ocorrencia = ?) " +
                     "     + GREATEST((SELECT COUNT(*) FROM tb_historico_status WHERE cd_ocorrencia = ?) - 1, 0) " +
                     "FROM dual";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar a ocorrencia.", e);
        }
    }

    private List<Ocorrencia> executarLista(PreparedStatement ps) throws SQLException {
        List<Ocorrencia> ocorrencias = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ocorrencias.add(mapear(rs));
            }
        }
        return ocorrencias;
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        return new Ocorrencia(
                rs.getLong("cd_ocorrencia"),
                rs.getLong("cd_usuario"),
                rs.getLong("cd_categoria"),
                rs.getLong("cd_bairro"),
                rs.getString("ds_titulo"),
                rs.getString("ds_descricao"),
                rs.getString("ds_endereco"),
                rs.getDouble("nr_latitude"),
                rs.getDouble("nr_longitude"),
                StatusOcorrencia.valueOf(rs.getString("st_ocorrencia")),
                JdbcUtil.paraData(rs.getTimestamp("dt_registro")),
                JdbcUtil.paraData(rs.getTimestamp("dt_resolucao")),
                "S".equals(rs.getString("ic_oculta")),
                rs.getString("nm_categoria"),
                rs.getString("nm_bairro"),
                rs.getString("nm_municipio"),
                rs.getInt("qt_apoios"));
    }
}
