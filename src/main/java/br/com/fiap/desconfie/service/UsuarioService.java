package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.dao.UsuarioDAO;
import br.com.fiap.desconfie.dto.request.LoginRequest;
import br.com.fiap.desconfie.dto.request.UsuarioRequest;
import br.com.fiap.desconfie.dto.request.UsuarioUpdateRequest;
import br.com.fiap.desconfie.dto.response.UsuarioResponse;
import br.com.fiap.desconfie.exception.CredenciaisInvalidasException;
import br.com.fiap.desconfie.exception.EmailJaCadastradoException;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.exception.RegraNegocioException;
import br.com.fiap.desconfie.model.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras de negócio do usuário. Fica entre o controller (HTTP) e o DAO (banco).
 *
 * Fluxo: recebe o DTO -> confere as regras -> converte para o model com o ModelMapper
 * -> chama o DAO -> converte o model de volta para o DTO de resposta.
 */
@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final ModelMapper modelMapper;

    public UsuarioService(UsuarioDAO usuarioDAO, ModelMapper modelMapper) {
        this.usuarioDAO = usuarioDAO;
        this.modelMapper = modelMapper;
    }

    public UsuarioResponse cadastrar(UsuarioRequest request) {
        String email = normalizarEmail(request.getEmail());

        // RN03: e-mail único por conta
        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            throw new EmailJaCadastradoException("Já existe uma conta com este e-mail.");
        }

        Usuario usuario = modelMapper.map(request, Usuario.class);
        usuario.setEmail(email);
        usuario.setTelefone(vazioParaNulo(request.getTelefone()));
        usuario.setStatus("A");

        Usuario salvo = usuarioDAO.inserir(usuario);
        return modelMapper.map(salvo, UsuarioResponse.class);
    }

    public List<UsuarioResponse> listar() {
        return usuarioDAO.listar().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioResponse.class))
                .toList();
    }

    public UsuarioResponse buscarPorId(Long id) {
        return modelMapper.map(buscarModel(id), UsuarioResponse.class);
    }

    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarModel(id);
        String email = normalizarEmail(request.getEmail());

        // RN03: o novo e-mail não pode pertencer a OUTRA conta
        usuarioDAO.buscarPorEmail(email)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new EmailJaCadastradoException("Já existe outra conta com este e-mail.");
                });

        usuario.setNome(request.getNome());
        usuario.setEmail(email);
        usuario.setTelefone(vazioParaNulo(request.getTelefone()));
        usuarioDAO.atualizar(usuario);
        return modelMapper.map(usuario, UsuarioResponse.class);
    }

    public void excluir(Long id) {
        buscarModel(id);

        // RN06: não excluir usuário com histórico de análises
        if (usuarioDAO.possuiAnalises(id)) {
            throw new RegraNegocioException("Este usuário tem análises registradas e não pode ser excluído.");
        }
        usuarioDAO.excluir(id);
    }

    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = usuarioDAO.buscarPorEmail(normalizarEmail(request.getEmail()))
                .filter(encontrado -> encontrado.getSenha().equals(request.getSenha()))
                .filter(encontrado -> "A".equals(encontrado.getStatus()))
                // mesma mensagem para e-mail ou senha errados: não revela qual dos dois falhou
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha incorretos."));
        return modelMapper.map(usuario, UsuarioResponse.class);
    }

    /** Busca o model ou lança 404. Usado também pelos outros services (vínculo, análise). */
    public Usuario buscarModel(Long id) {
        return usuarioDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado."));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String vazioParaNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }
}
