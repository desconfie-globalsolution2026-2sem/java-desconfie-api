package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.model.CategoriaGolpe;
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
 * Acesso à tabela tb_dsf_categoria_golpe com JDBC puro.
 */
@Repository
public class CategoriaGolpeDAO {

    private static final String COLUNAS = "cd_categoria, nm_categoria, ds_categoria, ds_orientacao";

    private final DataSource dataSource;

    public CategoriaGolpeDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CategoriaGolpe inserir(CategoriaGolpe categoria) {
        String sql = "INSERT INTO tb_dsf_categoria_golpe (cd_categoria, nm_categoria, ds_categoria, ds_orientacao) "
                   + "VALUES (sq_tb_dsf_categoria_golpe.NEXTVAL, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "CD_CATEGORIA" })) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setString(3, categoria.getOrientacao());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    categoria.setId(chaves.getLong(1));
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar categoria", e);
        }
    }

    public List<CategoriaGolpe> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_dsf_categoria_golpe ORDER BY nm_categoria";
        List<CategoriaGolpe> categorias = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar categorias", e);
        }
        return categorias;
    }

    public Optional<CategoriaGolpe> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_dsf_categoria_golpe WHERE cd_categoria = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar categoria", e);
        }
    }

    /** Busca pelo nome ignorando maiúsculas/minúsculas (para a regra de nome único). */
    public Optional<CategoriaGolpe> buscarPorNome(String nome) {
        String sql = "SELECT " + COLUNAS + " FROM tb_dsf_categoria_golpe WHERE UPPER(nm_categoria) = UPPER(?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar categoria por nome", e);
        }
    }

    public void atualizar(CategoriaGolpe categoria) {
        String sql = "UPDATE tb_dsf_categoria_golpe SET nm_categoria = ?, ds_categoria = ?, ds_orientacao = ? "
                   + "WHERE cd_categoria = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setString(3, categoria.getOrientacao());
            ps.setLong(4, categoria.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar categoria", e);
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM tb_dsf_categoria_golpe WHERE cd_categoria = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao excluir categoria", e);
        }
    }

    /** Usado pela RN06: categoria com padrões não pode ser excluída. */
    public boolean possuiPadroes(Long id) {
        String sql = "SELECT COUNT(*) FROM tb_dsf_padrao_golpe WHERE cd_categoria = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao verificar padrões da categoria", e);
        }
    }

    private CategoriaGolpe montar(ResultSet rs) throws SQLException {
        return new CategoriaGolpe(
                rs.getLong("cd_categoria"),
                rs.getString("nm_categoria"),
                rs.getString("ds_categoria"),
                rs.getString("ds_orientacao"));
    }
}
