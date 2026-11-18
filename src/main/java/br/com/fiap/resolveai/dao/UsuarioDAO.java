package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.TipoPlano;
import br.com.fiap.resolveai.model.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a tabela tb_usuario com JDBC puro. */
@Repository
public class UsuarioDAO {

    private static final String SELECT_BASE =
            "SELECT u.cd_usuario, u.cd_bairro, u.nm_usuario, u.ds_email, u.ds_senha_hash, u.tp_plano, " +
            "u.dt_cadastro, b.nm_bairro " +
            "FROM tb_usuario u JOIN tb_bairro b ON b.cd_bairro = u.cd_bairro";

    private final DataSource dataSource;

    public UsuarioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT do morador com id de sq_tb_usuario; a senha ja chega aqui como hash BCrypt. */
    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO tb_usuario (cd_usuario, cd_bairro, nm_usuario, ds_email, ds_senha_hash, tp_plano) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            long id = JdbcUtil.proximoId(conn, "sq_tb_usuario");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, usuario.getCdBairro());
                ps.setString(3, usuario.getNome());
                ps.setString(4, usuario.getEmail());
                ps.setString(5, usuario.getSenhaHash());
                ps.setString(6, usuario.getPlano().name());
                ps.executeUpdate();
            }
            return buscarPorId(id).orElseThrow();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao cadastrar o usuario.", e);
        }
    }

    /** SELECT de todos os usuarios com o nome do bairro, por nome. */
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " ORDER BY u.nm_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar os usuarios.", e);
        }
    }

    /** SELECT por cd_usuario; vazio quando nao existe. */
    public Optional<Usuario> buscarPorId(Long id) {
        return buscarUm(SELECT_BASE + " WHERE u.cd_usuario = ?", id, null);
    }

    /** SELECT por e-mail (sem diferenciar maiusculas), para impedir conta repetida. */
    public Optional<Usuario> buscarPorEmail(String email) {
        return buscarUm(SELECT_BASE + " WHERE UPPER(u.ds_email) = UPPER(?)", null, email);
    }

    /** UPDATE de bairro, nome, e-mail, hash da senha e plano. */
    public void atualizar(Usuario usuario) {
        String sql = "UPDATE tb_usuario SET cd_bairro = ?, nm_usuario = ?, ds_email = ?, ds_senha_hash = ?, tp_plano = ? " +
                     "WHERE cd_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuario.getCdBairro());
            ps.setString(2, usuario.getNome());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getSenhaHash());
            ps.setString(5, usuario.getPlano().name());
            ps.setLong(6, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao atualizar o usuario.", e);
        }
    }

    /** DELETE do usuario (o service so chama quando ele nao tem ocorrencias, apoios nem denuncias). */
    public void remover(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_usuario WHERE cd_usuario = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao remover o usuario.", e);
        }
    }

    /** Soma dos COUNTs de ocorrencias, apoios e denuncias do usuario, para bloquear a remocao. */
    public int contarVinculos(Long id) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_ocorrencia WHERE cd_usuario = ?) " +
                     "     + (SELECT COUNT(*) FROM tb_apoio WHERE cd_usuario = ?) " +
                     "     + (SELECT COUNT(*) FROM tb_denuncia WHERE cd_usuario = ?) FROM dual";
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
            throw new PersistenciaException("Falha ao consultar o usuario.", e);
        }
    }

    private Optional<Usuario> buscarUm(String sql, Long id, String email) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (id != null) {
                ps.setLong(1, id);
            } else {
                ps.setString(1, email);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar o usuario.", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("cd_usuario"),
                rs.getLong("cd_bairro"),
                rs.getString("nm_usuario"),
                rs.getString("ds_email"),
                rs.getString("ds_senha_hash"),
                TipoPlano.valueOf(rs.getString("tp_plano")),
                JdbcUtil.paraData(rs.getTimestamp("dt_cadastro")),
                rs.getString("nm_bairro"));
    }
}
