package br.com.fiap.desconfie.exception;

/**
 * RN03: cada e-mail só pode ter uma conta (vira 409).
 */
public class EmailJaCadastradoException extends RegraNegocioException {

    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
