package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.dao.PadraoGolpeDAO;
import br.com.fiap.desconfie.dto.request.PadraoRequest;
import br.com.fiap.desconfie.dto.response.PadraoResponse;
import br.com.fiap.desconfie.enums.TipoPadrao;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.exception.RegraNegocioException;
import br.com.fiap.desconfie.model.CategoriaGolpe;
import br.com.fiap.desconfie.model.PadraoGolpe;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras dos padrões de golpe (frases e sinais de link com peso).
 */
@Service
public class PadraoService {

    private final PadraoGolpeDAO padraoDAO;
    private final CategoriaService categoriaService;
    private final ModelMapper modelMapper;

    public PadraoService(PadraoGolpeDAO padraoDAO, CategoriaService categoriaService, ModelMapper modelMapper) {
        this.padraoDAO = padraoDAO;
        this.categoriaService = categoriaService;
        this.modelMapper = modelMapper;
    }

    /** POST /padroes (categoria vem no corpo) e POST /categorias/{id}/padroes (categoria vem da URL). */
    public PadraoResponse cadastrar(PadraoRequest request, Long categoriaIdDaUrl) {
        Long categoriaId = categoriaIdDaUrl != null ? categoriaIdDaUrl : request.getCategoriaId();
        PadraoGolpe padrao = new PadraoGolpe();
        aplicar(padrao, request, categoriaId);
        if (padrao.getAtivo() == null) {
            padrao.setAtivo(true);
        }
        return paraResponse(padraoDAO.inserir(padrao));
    }

    public List<PadraoResponse> listar() {
        return padraoDAO.listar().stream().map(this::paraResponse).toList();
    }

    public List<PadraoResponse> listarPorCategoria(Long categoriaId) {
        categoriaService.buscarModel(categoriaId);
        return padraoDAO.listarPorCategoria(categoriaId).stream().map(this::paraResponse).toList();
    }

    public PadraoResponse buscarPorId(Long id) {
        return paraResponse(buscarModel(id));
    }

    public PadraoResponse atualizar(Long id, PadraoRequest request) {
        PadraoGolpe padrao = buscarModel(id);
        aplicar(padrao, request, request.getCategoriaId());
        if (request.getAtivo() == null) {
            padrao.setAtivo(true);
        }
        padraoDAO.atualizar(padrao);
        return paraResponse(padrao);
    }

    public void excluir(Long id) {
        buscarModel(id);
        // RN06: padrão já encontrado em análises fica no histórico; deve ser desativado, não excluído
        if (padraoDAO.foiEncontradoEmAnalises(id)) {
            throw new RegraNegocioException(
                    "Este padrão já foi encontrado em análises. Desative-o (ativo = false) em vez de excluir.");
        }
        padraoDAO.excluir(id);
    }

    private PadraoGolpe buscarModel(Long id) {
        return padraoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Padrão " + id + " não encontrado."));
    }

    /** Copia os dados do request para o model, aplicando as regras de categoria e termo único. */
    private void aplicar(PadraoGolpe padrao, PadraoRequest request, Long categoriaId) {
        // mesma regra do CHECK ck_dsf_padrao_categoria do banco, com mensagem clara (400)
        if (request.getTipo() == TipoPadrao.DOMINIO_OFICIAL && categoriaId != null) {
            throw new IllegalArgumentException("Padrão do tipo DOMINIO_OFICIAL não pertence a uma categoria de golpe.");
        }
        if (request.getTipo() != TipoPadrao.DOMINIO_OFICIAL && categoriaId == null) {
            throw new IllegalArgumentException("Informe a categoria (categoriaId) do padrão.");
        }
        CategoriaGolpe categoria = categoriaId == null ? null : categoriaService.buscarModel(categoriaId);

        // termos guardados normalizados: o motor de regras compara texto sem acento e em minúsculas
        String termo = TextoUtil.normalizar(request.getTermo());
        if (padraoDAO.existeTermo(request.getTipo(), termo, padrao.getId())) {
            throw new RegraNegocioException("Já existe um padrão " + request.getTipo() + " com o termo '" + termo + "'.");
        }

        padrao.setCategoria(categoria);
        padrao.setTipo(request.getTipo());
        padrao.setTermo(termo);
        padrao.setPeso(request.getPeso());
        padrao.setAtivo(request.getAtivo());
    }

    /**
     * ModelMapper copia os campos de mesmo nome (id, tipo, termo, peso, ativo).
     * categoriaId e categoriaNome vêm de dentro do objeto categoria, que pode ser nulo,
     * por isso são preenchidos à mão.
     */
    private PadraoResponse paraResponse(PadraoGolpe padrao) {
        PadraoResponse resposta = modelMapper.map(padrao, PadraoResponse.class);
        CategoriaGolpe categoria = padrao.getCategoria();
        resposta.setCategoriaId(categoria == null ? null : categoria.getId());
        resposta.setCategoriaNome(categoria == null ? null : categoria.getNome());
        return resposta;
    }
}
