package br.com.fiap.desconfie.exception;

/**
 * RN01: um guardião pode ter no máximo 3 dependentes (vira 409).
 */
public class LimiteDependentesException extends RegraNegocioException {

    public LimiteDependentesException(String mensagem) {
        super(mensagem);
    }
}
