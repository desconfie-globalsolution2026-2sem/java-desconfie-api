package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.dao.UsuarioDAO;
import br.com.fiap.desconfie.dao.VinculoGuardiaoDAO;
import br.com.fiap.desconfie.dto.request.DependenteRequest;
import br.com.fiap.desconfie.dto.response.MembroCirculoResponse;
import br.com.fiap.desconfie.dto.response.UsuarioResumoResponse;
import br.com.fiap.desconfie.dto.response.VinculoResponse;
import br.com.fiap.desconfie.exception.LimiteDependentesException;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.exception.RegraNegocioException;
import br.com.fiap.desconfie.model.Usuario;
import br.com.fiap.desconfie.model.VinculoGuardiao;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras do círculo de proteção (guardião e dependentes).
 */
@Service
public class VinculoService {

    /** RN01: limite de dependentes por guardião (constante compartilhada no contrato). */
    public static final int MAXIMO_DEPENDENTES = 3;

    private final VinculoGuardiaoDAO vinculoDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public VinculoService(VinculoGuardiaoDAO vinculoDAO, UsuarioDAO usuarioDAO,
                          UsuarioService usuarioService, ModelMapper modelMapper) {
        this.vinculoDAO = vinculoDAO;
        this.usuarioDAO = usuarioDAO;
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    public VinculoResponse adicionarDependente(Long guardiaoId, DependenteRequest request) {
        Usuario guardiao = usuarioService.buscarModel(guardiaoId);
        Usuario dependente = usuarioDAO.buscarPorEmail(request.getEmailDependente().trim().toLowerCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma conta encontrada com o e-mail " + request.getEmailDependente() + "."));

        // RN02: ninguém é guardião de si mesmo
        if (guardiao.getId().equals(dependente.getId())) {
            throw new RegraNegocioException("Você não pode ser guardião de si mesmo.");
        }

        // RN02: cada dependente tem um único guardião (e o mesmo vínculo não se repete)
        vinculoDAO.buscarPorDependente(dependente.getId()).ifPresent(existente -> {
            if (existente.getGuardiao().getId().equals(guardiaoId)) {
                throw new RegraNegocioException(dependente.getNome() + " já é seu dependente.");
            }
            throw new RegraNegocioException(dependente.getNome() + " já tem um guardião.");
        });

        // RN01: no máximo 3 dependentes por guardião
        if (vinculoDAO.contarDependentes(guardiaoId) >= MAXIMO_DEPENDENTES) {
            throw new LimiteDependentesException("Este guardião já tem " + MAXIMO_DEPENDENTES
                    + " dependentes. Remova um para adicionar outro.");
        }

        vinculoDAO.inserir(guardiaoId, dependente.getId());
        return new VinculoResponse(guardiaoId,
                modelMapper.map(dependente, UsuarioResumoResponse.class),
                LocalDate.now());
    }

    public List<MembroCirculoResponse> listarDependentes(Long guardiaoId) {
        usuarioService.buscarModel(guardiaoId);
        return vinculoDAO.listarDependentes(guardiaoId).stream()
                .map(vinculo -> paraMembro(vinculo.getDependente(), vinculo))
                .toList();
    }

    public MembroCirculoResponse buscarGuardiao(Long usuarioId) {
        usuarioService.buscarModel(usuarioId);
        VinculoGuardiao vinculo = vinculoDAO.buscarPorDependente(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este usuário não tem guardião."));
        return paraMembro(vinculo.getGuardiao(), vinculo);
    }

    public void removerDependente(Long guardiaoId, Long dependenteId) {
        usuarioService.buscarModel(guardiaoId);
        if (!vinculoDAO.excluir(guardiaoId, dependenteId)) {
            throw new RecursoNaoEncontradoException("O usuário " + dependenteId + " não é dependente deste guardião.");
        }
    }

    /** ModelMapper copia id, nome e e-mail do usuário; a data vem do vínculo. */
    private MembroCirculoResponse paraMembro(Usuario usuario, VinculoGuardiao vinculo) {
        MembroCirculoResponse membro = modelMapper.map(usuario, MembroCirculoResponse.class);
        membro.setDataVinculo(vinculo.getDataVinculo());
        return membro;
    }
}
