package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.dao.AlertaDAO;
import br.com.fiap.desconfie.dao.VinculoGuardiaoDAO;
import br.com.fiap.desconfie.dto.response.AlertaResponse;
import br.com.fiap.desconfie.dto.response.AutorResponse;
import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.model.Alerta;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras dos alertas do círculo de proteção.
 */
@Service
public class AlertaService {

    private final AlertaDAO alertaDAO;
    private final VinculoGuardiaoDAO vinculoDAO;
    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public AlertaService(AlertaDAO alertaDAO, VinculoGuardiaoDAO vinculoDAO,
                         UsuarioService usuarioService, ModelMapper modelMapper) {
        this.alertaDAO = alertaDAO;
        this.vinculoDAO = vinculoDAO;
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    /**
     * RN05: um resultado VERMELHO avisa todos os outros membros do círculo de quem analisou
     * (o guardião, os outros dependentes dele e os dependentes do próprio usuário).
     * Amarelo e verde não geram alerta.
     */
    public List<Long> destinatariosDoAlerta(Long autorId, NivelRisco risco) {
        if (risco != NivelRisco.VERMELHO) {
            return List.of();
        }
        return vinculoDAO.listarMembrosDoCirculo(autorId);
    }

    public List<AlertaResponse> listar(Long usuarioId, boolean apenasNaoLidos) {
        usuarioService.buscarModel(usuarioId);
        return alertaDAO.listarPorDestinatario(usuarioId, apenasNaoLidos).stream()
                .map(this::paraResponse)
                .toList();
    }

    public AlertaResponse marcarComoLido(Long alertaId) {
        Alerta alerta = alertaDAO.buscarPorId(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta " + alertaId + " não encontrado."));
        alertaDAO.marcarComoLido(alertaId);
        alerta.setLido(true);
        return paraResponse(alerta);
    }

    /** ModelMapper copia id, dataAlerta e lido; os dados da análise e do autor vêm de dentro dela. */
    private AlertaResponse paraResponse(Alerta alerta) {
        AlertaResponse resposta = modelMapper.map(alerta, AlertaResponse.class);
        resposta.setAnaliseId(alerta.getAnalise().getId());
        resposta.setAutor(modelMapper.map(alerta.getAnalise().getUsuario(), AutorResponse.class));
        resposta.setRisco(alerta.getAnalise().getRisco());
        resposta.setNotaFinal(alerta.getAnalise().getNotaFinal());
        resposta.setCategoriaPrincipal(alerta.getAnalise().getCategoriaPrincipal());
        return resposta;
    }
}
