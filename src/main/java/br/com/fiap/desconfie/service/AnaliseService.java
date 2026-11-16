package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.dao.AlertaDAO;
import br.com.fiap.desconfie.dao.AnaliseDAO;
import br.com.fiap.desconfie.dao.AnalisePadraoDAO;
import br.com.fiap.desconfie.dao.PadraoGolpeDAO;
import br.com.fiap.desconfie.dao.VinculoGuardiaoDAO;
import br.com.fiap.desconfie.dto.request.AnaliseRequest;
import br.com.fiap.desconfie.dto.request.FeedbackRequest;
import br.com.fiap.desconfie.dto.response.AnaliseResponse;
import br.com.fiap.desconfie.dto.response.AnaliseResumoResponse;
import br.com.fiap.desconfie.dto.response.AutorResponse;
import br.com.fiap.desconfie.dto.response.CirculoAnaliseResponse;
import br.com.fiap.desconfie.dto.response.PadraoEncontradoResponse;
import br.com.fiap.desconfie.dto.response.SinalLinkResponse;
import br.com.fiap.desconfie.enums.Feedback;
import br.com.fiap.desconfie.enums.NivelRisco;
import br.com.fiap.desconfie.enums.TipoEntrada;
import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.exception.IaIndisponivelException;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.model.Analise;
import br.com.fiap.desconfie.model.CategoriaGolpe;
import br.com.fiap.desconfie.model.PadraoEncontrado;
import br.com.fiap.desconfie.model.PadraoGolpe;
import br.com.fiap.desconfie.model.ResultadoRegras;
import br.com.fiap.desconfie.model.SinalLink;
import br.com.fiap.desconfie.model.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * O coração do Desconfie: recebe o conteúdo, calcula as duas metades da nota
 * (regras no Java + IA no Python), decide o semáforo e grava tudo numa transação só.
 */
@Service
public class AnaliseService {

    /** Domínio oficial conhecido: a nota final nunca passa de 29 (fica sempre verde). */
    static final double LIMITE_DOMINIO_OFICIAL = 29.0;
    static final int TAMANHO_MAXIMO_CONTEUDO = 2000;

    private static final String ORIENTACAO_VERDE =
            "Não encontramos sinais fortes de golpe. Na dúvida, confirme por outro canal antes de agir.";
    private static final String ORIENTACAO_AMARELO =
            "Atenção: há sinais suspeitos. Não clique, não pague e confirme por outro canal.";
    private static final String ORIENTACAO_VERMELHO =
            "Alto risco de golpe. Não clique, não pague e não passe dados. Confirme com a pessoa ou empresa por outro canal.";

    private final DataSource dataSource;
    private final AnaliseDAO analiseDAO;
    private final AnalisePadraoDAO analisePadraoDAO;
    private final AlertaDAO alertaDAO;
    private final PadraoGolpeDAO padraoDAO;
    private final VinculoGuardiaoDAO vinculoDAO;
    private final UsuarioService usuarioService;
    private final MotorRegrasService motorRegras;
    private final AlertaService alertaService;
    private final IaClient iaClient;
    private final ModelMapper modelMapper;

    public AnaliseService(DataSource dataSource, AnaliseDAO analiseDAO, AnalisePadraoDAO analisePadraoDAO,
                          AlertaDAO alertaDAO, PadraoGolpeDAO padraoDAO, VinculoGuardiaoDAO vinculoDAO,
                          UsuarioService usuarioService, MotorRegrasService motorRegras,
                          AlertaService alertaService, IaClient iaClient, ModelMapper modelMapper) {
        this.dataSource = dataSource;
        this.analiseDAO = analiseDAO;
        this.analisePadraoDAO = analisePadraoDAO;
        this.alertaDAO = alertaDAO;
        this.padraoDAO = padraoDAO;
        this.vinculoDAO = vinculoDAO;
        this.usuarioService = usuarioService;
        this.motorRegras = motorRegras;
        this.alertaService = alertaService;
        this.iaClient = iaClient;
        this.modelMapper = modelMapper;
    }

    public AnaliseResponse analisar(Long usuarioId, AnaliseRequest request) {
        Usuario usuario = usuarioService.buscarModel(usuarioId);
        validarEntrada(request);
        List<PadraoGolpe> padroesAtivos = padraoDAO.listarAtivos();

        // 1. Conteúdo, nota de regras e nota da IA, conforme o tipo
        String conteudo;
        ResultadoRegras regras;
        Optional<Double> notaIa;

        switch (request.getTipo()) {
            case IMAGEM -> {
                // RN07: imagem depende da IA para ler o texto (OCR); sem IA não há o que analisar
                IaClient.RespostaImagem resposta = iaClient.analisarImagem(request.getImagemBase64())
                        .orElseThrow(() -> new IaIndisponivelException(
                                "A análise de imagem está indisponível no momento. Cole o texto da mensagem."));
                conteudo = resposta.textoExtraido();
                regras = motorRegras.analisarTexto(conteudo, padroesAtivos);
                notaIa = Optional.ofNullable(resposta.probabilidadeGolpe()).map(p -> p * 100);
            }
            case LINK -> {
                conteudo = request.getConteudo().trim();
                regras = motorRegras.analisarLink(conteudo, padroesAtivos);
                notaIa = iaClient.notaLink(conteudo);
            }
            default -> {
                conteudo = request.getConteudo().trim();
                regras = motorRegras.analisarTexto(conteudo, padroesAtivos);
                notaIa = iaClient.notaTexto(conteudo);
            }
        }

        // 2. RN04: nota final = média de regras e IA; sem IA, só regras
        double notaRegras = regras.getNota();
        Double ia = notaIa.map(this::arredondar).orElse(null);
        double notaFinal = ia == null ? notaRegras : (notaRegras + ia) / 2;
        if (regras.isDominioOficial()) {
            notaFinal = Math.min(notaFinal, LIMITE_DOMINIO_OFICIAL);
        }
        notaFinal = arredondar(notaFinal);
        NivelRisco risco = NivelRisco.daNota(notaFinal);

        Analise analise = new Analise();
        analise.setUsuario(usuario);
        analise.setTipo(request.getTipo());
        analise.setConteudo(limitar(conteudo));
        analise.setNotaRegras(notaRegras);
        analise.setNotaIa(ia);
        analise.setNotaFinal(notaFinal);
        analise.setRisco(risco);
        analise.setFeedback(Feedback.SEM_FEEDBACK);
        analise.setDataAnalise(LocalDateTime.now().withNano(0));
        analise.setPadroesEncontrados(regras.getPadroes());

        // 3. RN05: quem recebe alerta (só quando VERMELHO)
        List<Long> destinatarios = alertaService.destinatariosDoAlerta(usuarioId, risco);

        // 4. Grava análise, padrões e alertas numa transação: ou grava tudo, ou nada
        salvarEmTransacao(analise, destinatarios);

        return montarResposta(analise, regras.getSinais(), destinatarios.size());
    }

    public List<AnaliseResumoResponse> listarPorUsuario(Long usuarioId) {
        usuarioService.buscarModel(usuarioId);
        return analiseDAO.listarPorUsuario(usuarioId).stream()
                .map(analise -> modelMapper.map(analise, AnaliseResumoResponse.class))
                .toList();
    }

    public List<AnaliseResumoResponse> listar() {
        return analiseDAO.listar().stream()
                .map(analise -> modelMapper.map(analise, AnaliseResumoResponse.class))
                .toList();
    }

    /** RN09: histórico compartilhado do círculo (amarelo e vermelho de todos, inclusive do próprio usuário). */
    public List<CirculoAnaliseResponse> listarDoCirculo(Long usuarioId) {
        usuarioService.buscarModel(usuarioId);
        List<Long> membros = new ArrayList<>(vinculoDAO.listarMembrosDoCirculo(usuarioId));
        membros.add(usuarioId);
        return analiseDAO.listarCompartilhadas(membros).stream()
                .map(analise -> {
                    CirculoAnaliseResponse item = modelMapper.map(analise, CirculoAnaliseResponse.class);
                    item.setAutor(modelMapper.map(analise.getUsuario(), AutorResponse.class));
                    return item;
                })
                .toList();
    }

    public AnaliseResponse buscarPorId(Long id) {
        Analise analise = buscarModel(id);
        analise.setPadroesEncontrados(analisePadraoDAO.listarPorAnalise(id));
        List<SinalLink> sinais = analise.getTipo() == TipoEntrada.LINK
                ? motorRegras.sinaisDoLink(analise.getConteudo())
                : List.of();
        return montarResposta(analise, sinais, alertaDAO.contarPorAnalise(id));
    }

    public AnaliseResponse atualizarFeedback(Long id, FeedbackRequest request) {
        buscarModel(id);
        analiseDAO.atualizarFeedback(id, request.getFeedback());
        return buscarPorId(id);
    }

    public void excluir(Long id) {
        buscarModel(id);
        analiseDAO.excluir(id);
    }

    private Analise buscarModel(Long id) {
        return analiseDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Análise " + id + " não encontrada."));
    }

    /** O campo obrigatório depende do tipo, por isso a conferência fica aqui (400). */
    private void validarEntrada(AnaliseRequest request) {
        if (request.getTipo() == TipoEntrada.IMAGEM) {
            if (request.getImagemBase64() == null || request.getImagemBase64().isBlank()) {
                throw new IllegalArgumentException("Envie a imagem em imagemBase64 para análises do tipo IMAGEM.");
            }
        } else if (request.getConteudo() == null || request.getConteudo().isBlank()) {
            throw new IllegalArgumentException("Envie o texto ou o link em conteudo.");
        }
    }

    /**
     * Transação: setAutoCommit(false) faz o Oracle esperar o commit() no fim.
     * Se qualquer INSERT falhar, o rollback() desfaz os anteriores, e não sobra análise pela metade.
     */
    private void salvarEmTransacao(Analise analise, List<Long> destinatarios) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                Long id = analiseDAO.inserir(con, analise);
                analise.setId(id);
                if (!analise.getPadroesEncontrados().isEmpty()) {
                    analisePadraoDAO.inserir(con, id, analise.getPadroesEncontrados());
                }
                if (!destinatarios.isEmpty()) {
                    alertaDAO.inserir(con, id, destinatarios);
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BancoDeDadosException("Erro ao salvar análise", e);
        }
    }

    /** Converte o model na resposta completa, com categoria principal e orientação. */
    private AnaliseResponse montarResposta(Analise analise, List<SinalLink> sinais, int alertasGerados) {
        AnaliseResponse resposta = modelMapper.map(analise, AnaliseResponse.class);
        resposta.setUsuarioId(analise.getUsuario().getId());
        resposta.setIaDisponivel(analise.getNotaIa() != null);
        resposta.setAlertasGerados(alertasGerados);

        List<PadraoEncontrado> encontrados = new ArrayList<>(analise.getPadroesEncontrados());
        encontrados.sort((a, b) -> b.getPadrao().getPeso() - a.getPadrao().getPeso());
        resposta.setPadroesEncontrados(encontrados.stream().map(this::paraPadraoResponse).toList());
        resposta.setSinaisLink(sinais.stream()
                .map(sinal -> modelMapper.map(sinal, SinalLinkResponse.class))
                .toList());

        // Categoria principal = categoria do padrão de maior peso; a orientação vem dela
        CategoriaGolpe principal = encontrados.stream()
                .map(e -> e.getPadrao().getCategoria())
                .filter(c -> c != null)
                .findFirst()
                .orElse(null);
        resposta.setCategoriaPrincipal(principal == null ? null : principal.getNome());
        resposta.setOrientacao(orientacao(analise.getRisco(), principal));
        return resposta;
    }

    private String orientacao(NivelRisco risco, CategoriaGolpe principal) {
        if (risco == NivelRisco.VERDE) {
            return ORIENTACAO_VERDE;
        }
        if (principal != null && principal.getOrientacao() != null && !principal.getOrientacao().isBlank()) {
            return principal.getOrientacao();
        }
        return risco == NivelRisco.VERMELHO ? ORIENTACAO_VERMELHO : ORIENTACAO_AMARELO;
    }

    private PadraoEncontradoResponse paraPadraoResponse(PadraoEncontrado encontrado) {
        PadraoGolpe padrao = encontrado.getPadrao();
        return new PadraoEncontradoResponse(padrao.getId(), padrao.getTermo(), padrao.getTipo(),
                padrao.getCategoria() == null ? null : padrao.getCategoria().getNome(),
                padrao.getPeso(), encontrado.getOcorrencias());
    }

    private double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    private String limitar(String texto) {
        return texto.length() > TAMANHO_MAXIMO_CONTEUDO ? texto.substring(0, TAMANHO_MAXIMO_CONTEUDO) : texto;
    }
}
