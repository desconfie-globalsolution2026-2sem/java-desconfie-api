package br.com.fiap.desconfie.exception;

/**
 * Lançada quando um registro procurado não existe (vira 404).
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
