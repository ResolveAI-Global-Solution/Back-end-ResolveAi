package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.Bairro;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Leitura de tb_bairro (dados de referencia carregados pelo script), com o nome do municipio. */
@Repository
public class BairroDAO {

    private static final String SELECT_BASE =
            "SELECT b.cd_bairro, b.cd_municipio, b.nm_bairro, b.ic_comunidade, m.nm_municipio " +
            "FROM tb_bairro b JOIN tb_municipio m ON m.cd_municipio = b.cd_municipio";

    private final DataSource dataSource;

    public BairroDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** SELECT com JOIN em tb_municipio de todos os bairros, por municipio e nome. */
    public List<Bairro> listar() {
        List<Bairro> bairros = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " ORDER BY m.nm_municipio, b.nm_bairro");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bairros.add(mapear(rs));
            }
            return bairros;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar os bairros.", e);
        }
    }

    /** SELECT por cd_bairro, usado para validar a FK antes de gravar usuario ou ocorrencia. */
    public Optional<Bairro> buscarPorId(Long id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " WHERE b.cd_bairro = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar o bairro.", e);
        }
    }

    private Bairro mapear(ResultSet rs) throws SQLException {
        return new Bairro(
                rs.getLong("cd_bairro"),
                rs.getLong("cd_municipio"),
                rs.getString("nm_bairro"),
                "S".equals(rs.getString("ic_comunidade")),
                rs.getString("nm_municipio"));
    }
}
