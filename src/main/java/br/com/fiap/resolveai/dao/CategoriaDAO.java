package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.Categoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso a tabela tb_categoria com JDBC puro. */
@Repository
public class CategoriaDAO {

    private static final String SELECT_BASE =
            "SELECT cd_categoria, nm_categoria, ds_categoria FROM tb_categoria";

    private final DataSource dataSource;

    public CategoriaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT em tb_categoria com id vindo de sq_tb_categoria; devolve a categoria com o id gerado. */
    public Categoria inserir(Categoria categoria) {
        String sql = "INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            long id = JdbcUtil.proximoId(conn, "sq_tb_categoria");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, categoria.getNome());
                ps.setString(3, categoria.getDescricao());
                ps.executeUpdate();
            }
            categoria.setId(id);
            return categoria;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao cadastrar a categoria.", e);
        }
    }

    /** SELECT de todas as categorias, em ordem alfabetica, para os chips do app. */
    public List<Categoria> listar() {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " ORDER BY nm_categoria");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(mapear(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar as categorias.", e);
        }
    }

    /** SELECT por cd_categoria; vazio quando nao existe. */
    public Optional<Categoria> buscarPorId(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE cd_categoria = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar a categoria.", e);
        }
    }

    /** SELECT por nome (sem diferenciar maiusculas), para impedir categoria repetida. */
    public Optional<Categoria> buscarPorNome(String nome) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE UPPER(nm_categoria) = UPPER(?)")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar a categoria.", e);
        }
    }

    /** UPDATE de nome e descricao da categoria. */
    public void atualizar(Categoria categoria) {
        String sql = "UPDATE tb_categoria SET nm_categoria = ?, ds_categoria = ? WHERE cd_categoria = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setLong(3, categoria.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao atualizar a categoria.", e);
        }
    }

    /** DELETE da categoria (o service so chama quando ela nao tem ocorrencias). */
    public void remover(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_categoria WHERE cd_categoria = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao remover a categoria.", e);
        }
    }

    /** COUNT de ocorrencias da categoria, para bloquear a remocao quando houver vinculos. */
    public int contarOcorrencias(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM tb_ocorrencia WHERE cd_categoria = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao consultar a categoria.", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getLong("cd_categoria"), rs.getString("nm_categoria"), rs.getString("ds_categoria"));
    }
}
