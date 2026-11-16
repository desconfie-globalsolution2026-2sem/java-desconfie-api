package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.enums.Feedback;
import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.enums.TipoEntrada;
import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.model.Analise;
import br.com.fiap.desconfie.model.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Acesso à tabela tb_dsf_analise com JDBC puro.
 * O INSERT recebe a Connection de fora porque faz parte de uma transação maior
 * (análise + padrões encontrados + alertas), controlada pelo AnaliseService.
 */
@Repository
public class AnaliseDAO {

    /**
     * Subconsulta que calcula a categoria principal: a categoria do padrão de maior peso
     * encontrado na análise. Usada também pelo AlertaDAO.
     */
    static final String SUBCONSULTA_CATEGORIA =
            "(SELECT c.nm_categoria FROM tb_dsf_analise_padrao ap "
          + "   INNER JOIN tb_dsf_padrao_golpe p ON p.cd_padrao = ap.cd_padrao "
          + "   INNER JOIN tb_dsf_categoria_golpe c ON c.cd_categoria = p.cd_categoria "
          + "  WHERE ap.cd_analise = a.cd_analise "
          + "  ORDER BY p.nr_peso DESC FETCH FIRST 1 ROWS ONLY)";

    private static final String SELECT_BASE =
            "SELECT a.cd_analise, a.tp_entrada, a.ds_conteudo, a.nr_nota_regras, a.nr_nota_ia, a.nr_nota_final, "
          + "       a.st_risco, a.st_feedback, a.dt_analise, "
          + "       u.cd_usuario, u.nm_usuario, u.nr_telefone, "
          + "       " + SUBCONSULTA_CATEGORIA + " categoria_principal "
          + "FROM tb_dsf_analise a "
          + "INNER JOIN tb_dsf_usuario u ON u.cd_usuario = a.cd_usuario ";

    private final DataSource dataSource;

    public AnaliseDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT dentro de uma transação aberta pelo service; devolve o id gerado pela sequence. */
    public Long inserir(Connection con, Analise analise) throws SQLException {
        String sql = "INSERT INTO tb_dsf_analise (cd_analise, cd_usuario, tp_entrada, ds_conteudo, nr_nota_regras, "
                   + "nr_nota_ia, nr_nota_final, st_risco, st_feedback, dt_analise) "
                   + "VALUES (sq_tb_dsf_analise.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[] { "CD_ANALISE" })) {
            ps.setLong(1, analise.getUsuario().getId());
            ps.setString(2, analise.getTipo().name());
            ps.setString(3, analise.getConteudo());
            ps.setDouble(4, analise.getNotaRegras());
            if (analise.getNotaIa() == null) {
                ps.setNull(5, Types.NUMERIC);   // IA indisponível
            } else {
                ps.setDouble(5, analise.getNotaIa());
            }
            ps.setDouble(6, analise.getNotaFinal());
            ps.setString(7, analise.getRisco().name());
            ps.setString(8, analise.getFeedback().name());
            ps.setTimestamp(9, Timestamp.valueOf(analise.getDataAnalise()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getLong(1);
            }
        }
    }

    public List<Analise> listar() {
        return consultar(SELECT_BASE + "ORDER BY a.dt_analise DESC", List.of(), "Erro ao listar análises");
    }

    /** Rota aninhada do 1:N usuário -> análises, da mais recente para a mais antiga. */
    public List<Analise> listarPorUsuario(Long usuarioId) {
        return consultar(SELECT_BASE + "WHERE a.cd_usuario = ? ORDER BY a.dt_analise DESC",
                List.of(usuarioId), "Erro ao listar análises do usuário");
    }

    /** Histórico do círculo: só AMARELO e VERMELHO dos usuários informados (RN09). */
    public List<Analise> listarCompartilhadas(List<Long> usuarioIds) {
        String marcadores = usuarioIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = SELECT_BASE + "WHERE a.cd_usuario IN (" + marcadores + ") "
                   + "AND a.st_risco IN ('AMARELO', 'VERMELHO') ORDER BY a.dt_analise DESC";
        return consultar(sql, usuarioIds, "Erro ao listar análises do círculo");
    }

    public Optional<Analise> buscarPorId(Long id) {
        List<Analise> lista = consultar(SELECT_BASE + "WHERE a.cd_analise = ?", List.of(id), "Erro ao buscar análise");
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    public void atualizarFeedback(Long id, Feedback feedback) {
        String sql = "UPDATE tb_dsf_analise SET st_feedback = ? WHERE cd_analise = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, feedback.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar feedback", e);
        }
    }

    /** O ON DELETE CASCADE do banco apaga junto os padrões encontrados e os alertas desta análise. */
    public void excluir(Long id) {
        String sql = "DELETE FROM tb_dsf_analise WHERE cd_analise = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao excluir análise", e);
        }
    }

    private List<Analise> consultar(String sql, List<Long> parametros, String mensagemErro) {
        List<Analise> analises = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setLong(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    analises.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException(mensagemErro, e);
        }
        return analises;
    }

    private Analise montar(ResultSet rs) throws SQLException {
        Usuario autor = new Usuario();
        autor.setId(rs.getLong("cd_usuario"));
        autor.setNome(rs.getString("nm_usuario"));
        autor.setTelefone(rs.getString("nr_telefone"));

        Analise analise = new Analise();
        analise.setId(rs.getLong("cd_analise"));
        analise.setUsuario(autor);
        analise.setTipo(TipoEntrada.valueOf(rs.getString("tp_entrada")));
        analise.setConteudo(rs.getString("ds_conteudo"));
        analise.setNotaRegras(rs.getDouble("nr_nota_regras"));
        double notaIa = rs.getDouble("nr_nota_ia");
        analise.setNotaIa(rs.wasNull() ? null : notaIa);
        analise.setNotaFinal(rs.getDouble("nr_nota_final"));
        analise.setRisco(NivelRisco.valueOf(rs.getString("st_risco")));
        analise.setFeedback(Feedback.valueOf(rs.getString("st_feedback")));
        analise.setDataAnalise(rs.getTimestamp("dt_analise").toLocalDateTime().withNano(0));
        analise.setCategoriaPrincipal(rs.getString("categoria_principal"));
        return analise;
    }
}
