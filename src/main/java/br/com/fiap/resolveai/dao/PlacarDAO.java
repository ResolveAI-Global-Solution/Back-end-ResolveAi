package br.com.fiap.resolveai.dao;

import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.model.PlacarItem;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consulta agregada do placar: abertas, resolvidas e tempo medio de resolucao por bairro. */
@Repository
public class PlacarDAO {

    private final DataSource dataSource;

    public PlacarDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * SELECT com SUM(CASE), AVG(dt_resolucao - dt_registro) e GROUP BY bairro/municipio.
     * Ignora ocorrencias ocultas e REJEITADA; categoria e periodo (por dt_registro) sao filtros opcionais.
     */
    public List<PlacarItem> listar(Long cdCategoria, LocalDate inicio, LocalDate fim) {
        StringBuilder sql = new StringBuilder(
                "SELECT b.nm_bairro, m.nm_municipio, " +
                "SUM(CASE WHEN o.st_ocorrencia <> 'RESOLVIDA' THEN 1 ELSE 0 END) AS qt_abertas, " +
                "SUM(CASE WHEN o.st_ocorrencia = 'RESOLVIDA' THEN 1 ELSE 0 END) AS qt_resolvidas, " +
                "ROUND(AVG(CASE WHEN o.st_ocorrencia = 'RESOLVIDA' THEN o.dt_resolucao - o.dt_registro END), 1) AS media_dias " +
                "FROM tb_ocorrencia o " +
                "JOIN tb_bairro b ON b.cd_bairro = o.cd_bairro " +
                "JOIN tb_municipio m ON m.cd_municipio = b.cd_municipio " +
                "WHERE o.ic_oculta = 'N' AND o.st_ocorrencia <> 'REJEITADA'");
        List<Object> parametros = new ArrayList<>();
        if (cdCategoria != null) {
            sql.append(" AND o.cd_categoria = ?");
            parametros.add(cdCategoria);
        }
        if (inicio != null) {
            sql.append(" AND o.dt_registro >= ?");
            parametros.add(Timestamp.valueOf(inicio.atStartOfDay()));
        }
        if (fim != null) {
            sql.append(" AND o.dt_registro < ?");
            parametros.add(Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
        }
        sql.append(" GROUP BY b.nm_bairro, m.nm_municipio" +
                   " ORDER BY SUM(CASE WHEN o.st_ocorrencia = 'RESOLVIDA' THEN 1 ELSE 0 END) / COUNT(*) DESC, b.nm_bairro");

        List<PlacarItem> itens = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal media = rs.getBigDecimal("media_dias");
                    itens.add(new PlacarItem(
                            rs.getString("nm_bairro"),
                            rs.getString("nm_municipio"),
                            rs.getInt("qt_abertas"),
                            rs.getInt("qt_resolvidas"),
                            media == null ? null : media.doubleValue()));
                }
            }
            return itens;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao montar o placar.", e);
        }
    }
}
