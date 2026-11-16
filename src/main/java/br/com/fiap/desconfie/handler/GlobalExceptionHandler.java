package br.com.fiap.desconfie.handler;

import br.com.fiap.desconfie.dto.response.CampoErro;
import br.com.fiap.desconfie.dto.response.ErroResponse;
import br.com.fiap.desconfie.exception.BancoDeDadosException;
import br.com.fiap.desconfie.exception.CredenciaisInvalidasException;
import br.com.fiap.desconfie.exception.IaIndisponivelException;
import br.com.fiap.desconfie.exception.RecursoNaoEncontradoException;
import br.com.fiap.desconfie.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Captura as exceções de todos os controllers e devolve sempre o mesmo JSON de erro.
 * Assim os controllers não precisam de try/catch, e nenhum stack trace ou SQL
 * chega ao cliente: o detalhe técnico fica só no log do servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 400: dados inválidos ----------

    /** Falha do Bean Validation (@Valid): lista cada campo inválido. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<CampoErro> campos = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErro(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos. Confira os campos destacados.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "O corpo da requisição está ausente ou não é um JSON válido.", req, null);
    }

    /** Ex.: /usuarios/abc quando o id deveria ser número. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'.", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: '" + e.getParameterName() + "'.", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoConteudo(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Envie os dados em JSON (Content-Type: application/json).", req, null);
    }

    /** Validações feitas no service com IllegalArgumentException. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> argumentoInvalido(IllegalArgumentException e, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    // ---------- 401: login ----------

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> credenciais(CredenciaisInvalidasException e, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, e.getMessage(), req, null);
    }

    // ---------- 404: não encontrado ----------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    /** Rota que não existe na API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Rota não encontrada.", req, null);
    }

    // ---------- 405: método errado ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodo(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + e.getMethod() + " não é permitido nesta rota.", req, null);
    }

    // ---------- 409: regra de negócio ----------

    /** Também trata as filhas: LimiteDependentesException, EmailJaCadastradoException... */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException e, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    // ---------- 503: IA fora do ar (só para imagem) ----------

    @ExceptionHandler(IaIndisponivelException.class)
    public ResponseEntity<ErroResponse> iaIndisponivel(IaIndisponivelException e, HttpServletRequest req) {
        return montar(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), req, null);
    }

    // ---------- banco de dados ----------

    /**
     * Erros do Oracle. Violação de valor único (ORA-00001) e remoção de registro com filhos
     * (ORA-02292) viram 409; qualquer outro vira 500. O SQL nunca vai para a resposta.
     */
    @ExceptionHandler(BancoDeDadosException.class)
    public ResponseEntity<ErroResponse> banco(BancoDeDadosException e, HttpServletRequest req) {
        log.error("Erro no banco em {}: {}", req.getRequestURI(), e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        return switch (e.getCodigoOracle()) {
            case 1 -> montar(HttpStatus.CONFLICT, "Já existe um registro com esses dados.", req, null);
            case 2292 -> montar(HttpStatus.CONFLICT, "Este registro tem outros dados ligados a ele e não pode ser removido.", req, null);
            default -> montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", req, null);
        };
    }

    // ---------- 500: qualquer outro erro ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> generico(Exception e, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), e);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", req, null);
    }

    /** Monta o JSON padrão de erro. */
    private ResponseEntity<ErroResponse> montar(HttpStatus status, String mensagem, HttpServletRequest req, List<CampoErro> campos) {
        ErroResponse corpo = new ErroResponse(
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                req.getRequestURI(),
                LocalDateTime.now().withNano(0),
                campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
