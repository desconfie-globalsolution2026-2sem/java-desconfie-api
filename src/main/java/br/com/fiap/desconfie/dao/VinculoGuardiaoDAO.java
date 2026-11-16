package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.model.Usuario;
import br.com.fiap.desconfie.model.VinculoGuardiao;
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
 * Acesso à tabela tb_dsf_vinculo_guardiao (autorrelacionamento usuário x usuário).
 * As consultas fazem JOIN com tb_dsf_usuario duas vezes: uma para o guardião (g)
 * e outra para o dependente (d).
 */
@Repository
public class VinculoGuardiaoDAO {

    private static final String SELECT_BASE =
            "SELECT v.dt_vinculo, "
          + "       g.cd_usuario g_id, g.nm_usuario g_nome, g.ds_email g_email, g.nr_telefone g_tel, "
          + "       d.cd_usuario d_id, d.nm_usuario d_nome, d.ds_email d_email, d.nr_telefone d_tel "
          + "FROM tb_dsf_vinculo_guardiao v "
          + "INNER JOIN tb_dsf_usuario g ON g.cd_usuario = v.cd_guardiao "
          + "INNER JOIN tb_dsf_usuario d ON d.cd_usuario = v.cd_dependente ";

    private final DataSource dataSource;

    public VinculoGuardiaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void inserir(Long guardiaoId, Long dependenteId) {
        String sql = "INSERT INTO tb_dsf_vinculo_guardiao (cd_guardiao, cd_dependente) VALUES (?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, guardiaoId);
            ps.setLong(2, dependenteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao criar vínculo", e);
        }
    }

    /** Usado pela RN01 (limite de 3 dependentes). */
    public int contarDependentes(Long guardiaoId) {
        String sql = "SELECT COUNT(*) FROM tb_dsf_vinculo_guardiao WHERE cd_guardiao = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, guardiaoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao contar dependentes", e);
        }
    }

    public List<VinculoGuardiao> listarDependentes(Long guardiaoId) {
        return consultar(SELECT_BASE + "WHERE v.cd_guardiao = ? ORDER BY d.nm_usuario", guardiaoId);
    }

    /** O guardião de um usuário (no máximo um, pela RN02). */
    public Optional<VinculoGuardiao> buscarPorDependente(Long dependenteId) {
        List<VinculoGuardiao> lista = consultar(SELECT_BASE + "WHERE v.cd_dependente = ?", dependenteId);
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    /** Remove o vínculo; devolve false se ele não existia. */
    public boolean excluir(Long guardiaoId, Long dependenteId) {
        String sql = "DELETE FROM tb_dsf_vinculo_guardiao WHERE cd_guardiao = ? AND cd_dependente = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, guardiaoId);
            ps.setLong(2, dependenteId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao remover vínculo", e);
        }
    }

    /**
     * Todos os outros membros do círculo de um usuário: o guardião dele, os outros dependentes
     * desse guardião e os dependentes do próprio usuário. Usado para gerar alertas (RN05)
     * e para o histórico compartilhado do círculo.
     */
    public List<Long> listarMembrosDoCirculo(Long usuarioId) {
        String sql = "SELECT cd_guardiao FROM tb_dsf_vinculo_guardiao WHERE cd_dependente = ? "
                   + "UNION "
                   + "SELECT v2.cd_dependente FROM tb_dsf_vinculo_guardiao v1 "
                   + "  INNER JOIN tb_dsf_vinculo_guardiao v2 ON v2.cd_guardiao = v1.cd_guardiao "
                   + "  WHERE v1.cd_dependente = ? "
                   + "UNION "
                   + "SELECT cd_dependente FROM tb_dsf_vinculo_guardiao WHERE cd_guardiao = ?";
        List<Long> membros = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.setLong(2, usuarioId);
            ps.setLong(3, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong(1);
                    if (id != usuarioId) {
                        membros.add(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar membros do círculo", e);
        }
        return membros;
    }

    private List<VinculoGuardiao> consultar(String sql, Long parametro) {
        List<VinculoGuardiao> vinculos = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vinculos.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao consultar vínculos", e);
        }
        return vinculos;
    }

    private VinculoGuardiao montar(ResultSet rs) throws SQLException {
        Usuario guardiao = new Usuario();
        guardiao.setId(rs.getLong("g_id"));
        guardiao.setNome(rs.getString("g_nome"));
        guardiao.setEmail(rs.getString("g_email"));
        guardiao.setTelefone(rs.getString("g_tel"));

        Usuario dependente = new Usuario();
        dependente.setId(rs.getLong("d_id"));
        dependente.setNome(rs.getString("d_nome"));
        dependente.setEmail(rs.getString("d_email"));
        dependente.setTelefone(rs.getString("d_tel"));

        return new VinculoGuardiao(guardiao, dependente, rs.getDate("dt_vinculo").toLocalDate());
    }
}
