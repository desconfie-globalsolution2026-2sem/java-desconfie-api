package br.com.fiap.desconfie.exception;

/**
 * Lançada quando o e-mail ou a senha do login não conferem (vira 401).
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
