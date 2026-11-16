package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.enums.TipoPadrao;
import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.model.CategoriaGolpe;
import br.com.fiap.desconfie.model.PadraoGolpe;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_dsf_padrao_golpe com JDBC puro.
 * As consultas fazem LEFT JOIN com a categoria para já montar o objeto CategoriaGolpe
 * (LEFT porque DOMINIO_OFICIAL não tem categoria).
 */
@Repository
public class PadraoGolpeDAO {

    private static final String SELECT_BASE =
            "SELECT p.cd_padrao, p.tp_padrao, p.ds_termo, p.nr_peso, p.st_ativo, "
          + "       c.cd_categoria, c.nm_categoria, c.ds_categoria, c.ds_orientacao "
          + "FROM tb_dsf_padrao_golpe p "
          + "LEFT JOIN tb_dsf_categoria_golpe c ON c.cd_categoria = p.cd_categoria ";

    private final DataSource dataSource;

    public PadraoGolpeDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PadraoGolpe inserir(PadraoGolpe padrao) {
        String sql = "INSERT INTO tb_dsf_padrao_golpe (cd_padrao, cd_categoria, tp_padrao, ds_termo, nr_peso, st_ativo) "
                   + "VALUES (sq_tb_dsf_padrao_golpe.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "CD_PADRAO" })) {
            preencher(ps, padrao);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    padrao.setId(chaves.getLong(1));
                }
            }
            return padrao;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar padrão", e);
        }
    }

    public List<PadraoGolpe> listar() {
        return consultar(SELECT_BASE + "ORDER BY p.tp_padrao, p.nr_peso DESC", null, "Erro ao listar padrões");
    }

    /** Rota aninhada do 1:N: padrões de uma categoria. */
    public List<PadraoGolpe> listarPorCategoria(Long categoriaId) {
        return consultar(SELECT_BASE + "WHERE p.cd_categoria = ? ORDER BY p.nr_peso DESC", categoriaId,
                "Erro ao listar padrões da categoria");
    }

    /** Usado pelo motor de regras: só os padrões ativos. */
    public List<PadraoGolpe> listarAtivos() {
        return consultar(SELECT_BASE + "WHERE p.st_ativo = 'S' ORDER BY p.nr_peso DESC", null,
                "Erro ao listar padrões ativos");
    }

    public Optional<PadraoGolpe> buscarPorId(Long id) {
        List<PadraoGolpe> lista = consultar(SELECT_BASE + "WHERE p.cd_padrao = ?", id, "Erro ao buscar padrão");
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** Verifica a regra "o mesmo termo não se repete no mesmo tipo", ignorando o próprio padrão no PUT. */
    public boolean existeTermo(TipoPadrao tipo, String termo, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_dsf_padrao_golpe WHERE tp_padrao = ? AND ds_termo = ? AND cd_padrao <> ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo.name());
            ps.setString(2, termo);
            ps.setLong(3, ignorarId == null ? -1 : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao verificar termo repetido", e);
        }
    }

    public void atualizar(PadraoGolpe padrao) {
        String sql = "UPDATE tb_dsf_padrao_golpe SET cd_categoria = ?, tp_padrao = ?, ds_termo = ?, nr_peso = ?, st_ativo = ? "
                   + "WHERE cd_padrao = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, padrao);
            ps.setLong(6, padrao.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar padrão", e);
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM tb_dsf_padrao_golpe WHERE cd_padrao = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao excluir padrão", e);
        }
    }

    /** Usado pela RN06: padrão já encontrado em análises não pode ser excluído (deve ser desativado). */
    public boolean foiEncontradoEmAnalises(Long id) {
        String sql = "SELECT COUNT(*) FROM tb_dsf_analise_padrao WHERE cd_padrao = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao verificar uso do padrão", e);
        }
    }

    /** Preenche os 5 primeiros parâmetros, comuns ao INSERT e ao UPDATE. */
    private void preencher(PreparedStatement ps, PadraoGolpe padrao) throws SQLException {
        if (padrao.getCategoria() == null) {
            ps.setNull(1, Types.NUMERIC);
        } else {
            ps.setLong(1, padrao.getCategoria().getId());
        }
        ps.setString(2, padrao.getTipo().name());
        ps.setString(3, padrao.getTermo());
        ps.setInt(4, padrao.getPeso());
        ps.setString(5, Boolean.TRUE.equals(padrao.getAtivo()) ? "S" : "N");
    }

    private List<PadraoGolpe> consultar(String sql, Long parametro, String mensagemErro) {
        List<PadraoGolpe> padroes = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    padroes.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException(mensagemErro, e);
        }
        return padroes;
    }

    private PadraoGolpe montar(ResultSet rs) throws SQLException {
        CategoriaGolpe categoria = null;
        long cdCategoria = rs.getLong("cd_categoria");
        if (!rs.wasNull()) {
            categoria = new CategoriaGolpe(cdCategoria, rs.getString("nm_categoria"),
                    rs.getString("ds_categoria"), rs.getString("ds_orientacao"));
        }
        return new PadraoGolpe(
                rs.getLong("cd_padrao"),
                categoria,
                TipoPadrao.valueOf(rs.getString("tp_padrao")),
                rs.getString("ds_termo"),
                rs.getInt("nr_peso"),
                "S".equals(rs.getString("st_ativo")));
    }
}
