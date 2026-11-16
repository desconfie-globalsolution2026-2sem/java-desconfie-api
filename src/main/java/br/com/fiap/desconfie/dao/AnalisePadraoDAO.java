package br.com.fiap.desconfie.dao;

import br.com.fiap.desconfie.enums.TipoPadrao;
import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.model.CategoriaGolpe;
import br.com.fiap.desconfie.model.PadraoEncontrado;
import br.com.fiap.desconfie.model.PadraoGolpe;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela associativa tb_dsf_analise_padrao (N:N análise x padrão).
 */
@Repository
public class AnalisePadraoDAO {

    private final DataSource dataSource;

    public AnalisePadraoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Grava os padrões encontrados dentro da transação da análise. */
    public void inserir(Connection con, Long analiseId, List<PadraoEncontrado> encontrados) throws SQLException {
        String sql = "INSERT INTO tb_dsf_analise_padrao (cd_analise, cd_padrao, nr_ocorrencias) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (PadraoEncontrado encontrado : encontrados) {
                ps.setLong(1, analiseId);
                ps.setLong(2, encontrado.getPadrao().getId());
                ps.setInt(3, encontrado.getOcorrencias());
                ps.addBatch();   // junta os INSERTs e envia de uma vez
            }
            ps.executeBatch();
        }
    }

    /** Padrões encontrados em uma análise, do maior peso para o menor. */
    public List<PadraoEncontrado> listarPorAnalise(Long analiseId) {
        String sql = "SELECT ap.nr_ocorrencias, p.cd_padrao, p.tp_padrao, p.ds_termo, p.nr_peso, p.st_ativo, "
                   + "       c.cd_categoria, c.nm_categoria, c.ds_categoria, c.ds_orientacao "
                   + "FROM tb_dsf_analise_padrao ap "
                   + "INNER JOIN tb_dsf_padrao_golpe p ON p.cd_padrao = ap.cd_padrao "
                   + "LEFT JOIN tb_dsf_categoria_golpe c ON c.cd_categoria = p.cd_categoria "
                   + "WHERE ap.cd_analise = ? ORDER BY p.nr_peso DESC";
        List<PadraoEncontrado> lista = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, analiseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoriaGolpe categoria = null;
                    long cdCategoria = rs.getLong("cd_categoria");
                    if (!rs.wasNull()) {
                        categoria = new CategoriaGolpe(cdCategoria, rs.getString("nm_categoria"),
                                rs.getString("ds_categoria"), rs.getString("ds_orientacao"));
                    }
                    PadraoGolpe padrao = new PadraoGolpe(rs.getLong("cd_padrao"), categoria,
                            TipoPadrao.valueOf(rs.getString("tp_padrao")), rs.getString("ds_termo"),
                            rs.getInt("nr_peso"), "S".equals(rs.getString("st_ativo")));
                    lista.add(new PadraoEncontrado(padrao, rs.getInt("nr_ocorrencias")));
                }
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao listar padrões da análise", e);
        }
        return lista;
    }
}
