package br.com.fiap.resolveai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Conversoes e consultas pequenas usadas por todos os DAOs. */
final class JdbcUtil {

    private JdbcUtil() {
    }

    static LocalDateTime paraData(Timestamp valor) {
        return valor == null ? null : valor.toLocalDateTime();
    }

    /** SELECT sq_tb_xxx.NEXTVAL FROM dual: reserva o proximo id da sequence antes do INSERT. */
    static long proximoId(Connection conn, String sequence) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT " + sequence + ".NEXTVAL FROM dual");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
