package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.exception.BancoDeDadosException;
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
 * Acesso à tabela tb_dsf_usuario com JDBC puro (DataSource + PreparedStatement).
 *
 * O DataSource chega pelo construtor (injeção de dependência feita pelo Spring).
 * Cada método abre a conexão com try-with-resources, que fecha conexão, comando e
 * resultado sozinho no fim, mesmo se der erro.
 */
@Repository
public class UsuarioDAO {

    private static final String COLUNAS =
            "cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario, dt_cadastro";

    private final DataSource dataSource;

    public UsuarioDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** INSERT com o id gerado pela sequence; devolve o usuário já com id e data de cadastro. */
    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO tb_dsf_usuario (cd_usuario, nm_usuario, ds_email, ds_senha, nr_telefone, st_usuario) "
                   + "VALUES (sq_tb_dsf_usuario.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             // o segundo parâmetro pede ao Oracle para devolver o cd_usuario gerado pela sequence
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "CD_USUARIO" })) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenha());
            ps.setString(4, usuario.getTelefone());
            ps.setString(5, usuario.getStatus());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao cadastrar usuário", e);
        }
        // relê do banco para trazer a data de cadastro preenchida pelo DEFAULT SYSDATE
        return buscarPorId(usuario.getId()).orElse(usuario);
    }

    public List<Usuario> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_dsf_usuario ORDER BY cd_usuario";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar usuários", e);
        }
        return usuarios;
    }

    /** Optional = "pode não existir": vazio quando o id não está no banco. */
    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_dsf_usuario WHERE cd_usuario = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar usuário", e);
        }
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT " + COLUNAS + " FROM tb_dsf_usuario WHERE ds_email = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao buscar usuário por e-mail", e);
        }
    }

    /** Atualiza nome, e-mail e telefone (a senha não muda por aqui). */
    public void atualizar(Usuario usuario) {
        String sql = "UPDATE tb_dsf_usuario SET nm_usuario = ?, ds_email = ?, nr_telefone = ? WHERE cd_usuario = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getTelefone());
            ps.setLong(4, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao atualizar usuário", e);
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM tb_dsf_usuario WHERE cd_usuario = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao excluir usuário", e);
        }
    }

    /** Usado pela RN06: usuário com análises não pode ser excluído. */
    public boolean possuiAnalises(Long id) {
        String sql = "SELECT COUNT(*) FROM tb_dsf_analise WHERE cd_usuario = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao verificar análises do usuário", e);
        }
    }

    /** Converte a linha atual do ResultSet em um objeto Usuario. */
    private Usuario montar(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getLong("cd_usuario"),
                rs.getString("nm_usuario"),
                rs.getString("ds_email"),
                rs.getString("ds_senha"),
                rs.getString("nr_telefone"),
                rs.getString("st_usuario"),
                rs.getDate("dt_cadastro").toLocalDate());
    }
}
