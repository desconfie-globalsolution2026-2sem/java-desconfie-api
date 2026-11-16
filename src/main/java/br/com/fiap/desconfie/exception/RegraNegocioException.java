package br.com.fiap.desconfie.exception;

/**
 * Lançada quando uma regra do domínio é violada (vira 409).
 * As exceções de regras específicas herdam desta.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
