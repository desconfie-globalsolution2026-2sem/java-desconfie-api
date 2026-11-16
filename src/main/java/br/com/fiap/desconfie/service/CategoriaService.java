package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.dao.CategoriaGolpeDAO;
import br.com.fiap.desconfie.dto.request.CategoriaRequest;
import br.com.fiap.desconfie.dto.response.CategoriaResponse;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.exception.RegraNegocioException;
import br.com.fiap.desconfie.model.CategoriaGolpe;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras das categorias de golpe.
 */
@Service
public class CategoriaService {

    private final CategoriaGolpeDAO categoriaDAO;
    private final ModelMapper modelMapper;

    public CategoriaService(CategoriaGolpeDAO categoriaDAO, ModelMapper modelMapper) {
        this.categoriaDAO = categoriaDAO;
        this.modelMapper = modelMapper;
    }

    public CategoriaResponse cadastrar(CategoriaRequest request) {
        validarNomeUnico(request.getNome().trim(), null);
        CategoriaGolpe categoria = modelMapper.map(request, CategoriaGolpe.class);
        categoria.setNome(request.getNome().trim());
        return modelMapper.map(categoriaDAO.inserir(categoria), CategoriaResponse.class);
    }

    public List<CategoriaResponse> listar() {
        return categoriaDAO.listar().stream()
                .map(categoria -> modelMapper.map(categoria, CategoriaResponse.class))
                .toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return modelMapper.map(buscarModel(id), CategoriaResponse.class);
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        CategoriaGolpe categoria = buscarModel(id);
        validarNomeUnico(request.getNome().trim(), id);
        categoria.setNome(request.getNome().trim());
        categoria.setDescricao(request.getDescricao());
        categoria.setOrientacao(request.getOrientacao());
        categoriaDAO.atualizar(categoria);
        return modelMapper.map(categoria, CategoriaResponse.class);
    }

    public void excluir(Long id) {
        buscarModel(id);
        // RN06: categoria com padrões não pode ser excluída
        if (categoriaDAO.possuiPadroes(id)) {
            throw new RegraNegocioException("Esta categoria tem padrões cadastrados. Remova ou mova os padrões antes.");
        }
        categoriaDAO.excluir(id);
    }

    /** Busca o model ou lança 404. Também usado pelo PadraoService. */
    public CategoriaGolpe buscarModel(Long id) {
        return categoriaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada."));
    }

    private void validarNomeUnico(String nome, Long idAtual) {
        categoriaDAO.buscarPorNome(nome)
                .filter(outra -> !outra.getId().equals(idAtual))
                .ifPresent(outra -> {
                    throw new RegraNegocioException("Já existe uma categoria com o nome '" + nome + "'.");
                });
    }
}
