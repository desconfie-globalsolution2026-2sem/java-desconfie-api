package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.model.Alerta;
import br.com.fiap.desconfie.model.Analise;
import br.com.fiap.desconfie.model.Usuario;
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
 * Acesso à tabela tb_dsf_alerta com JDBC puro.
 */
@Repository
public class AlertaDAO {

    private static final String SELECT_BASE =
            "SELECT al.cd_alerta, al.cd_destinatario, al.dt_alerta, al.st_lido, "
          + "       a.cd_analise, a.nr_nota_final, a.st_risco, "
          + "       u.cd_usuario, u.nm_usuario, u.nr_telefone, "
          + "       " + AnaliseDAO.SUBCONSULTA_CATEGORIA + " categoria_principal "
          + "FROM tb_dsf_alerta al "
          + "INNER JOIN tb_dsf_analise a ON a.cd_analise = al.cd_analise "
          + "INNER JOIN tb_dsf_usuario u ON u.cd_usuario = a.cd_usuario ";

    private final DataSource dataSource;

    public AlertaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Grava um alerta para cada destinatário, dentro da transação da análise. */
    public void inserir(Connection con, Long analiseId, List<Long> destinatarios) throws SQLException {
        String sql = "INSERT INTO tb_dsf_alerta (cd_alerta, cd_analise, cd_destinatario) "
                   + "VALUES (sq_tb_dsf_alerta.NEXTVAL, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Long destinatario : destinatarios) {
                ps.setLong(1, analiseId);
                ps.setLong(2, destinatario);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int contarPorAnalise(Long analiseId) {
        String sql = "SELECT COUNT(*) FROM tb_dsf_alerta WHERE cd_analise = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, analiseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao contar alertas", e);
        }
    }

    /** Alertas recebidos por um usuário; com apenasNaoLidos = true, só os que ele ainda não viu. */
    public List<Alerta> listarPorDestinatario(Long usuarioId, boolean apenasNaoLidos) {
        String sql = SELECT_BASE + "WHERE al.cd_destinatario = ? "
                   + (apenasNaoLidos ? "AND al.st_lido = 'N' " : "")
                   + "ORDER BY al.dt_alerta DESC";
        return consultar(sql, usuarioId, "Erro ao listar alertas");
    }

    public Optional<Alerta> buscarPorId(Long id) {
        List<Alerta> lista = consultar(SELECT_BASE + "WHERE al.cd_alerta = ?", id, "Erro ao buscar alerta");
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public void marcarComoLido(Long id) {
        String sql = "UPDATE tb_dsf_alerta SET st_lido = 'S' WHERE cd_alerta = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao marcar alerta como lido", e);
        }
    }

    private List<Alerta> consultar(String sql, Long parametro, String mensagemErro) {
        List<Alerta> alertas = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alertas.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException(mensagemErro, e);
        }
        return alertas;
    }

    private Alerta montar(ResultSet rs) throws SQLException {
        Usuario autor = new Usuario();
        autor.setId(rs.getLong("cd_usuario"));
        autor.setNome(rs.getString("nm_usuario"));
        autor.setTelefone(rs.getString("nr_telefone"));

        Analise analise = new Analise();
        analise.setId(rs.getLong("cd_analise"));
        analise.setUsuario(autor);
        analise.setNotaFinal(rs.getDouble("nr_nota_final"));
        analise.setRisco(NivelRisco.valueOf(rs.getString("st_risco")));
        analise.setCategoriaPrincipal(rs.getString("categoria_principal"));

        Usuario destinatario = new Usuario();
        destinatario.setId(rs.getLong("cd_destinatario"));

        return new Alerta(rs.getLong("cd_alerta"), analise, destinatario,
                rs.getTimestamp("dt_alerta").toLocalDateTime().withNano(0),
                "S".equals(rs.getString("st_lido")));
    }
}
