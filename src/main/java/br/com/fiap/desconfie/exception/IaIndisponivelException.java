package br.com.fiap.desconfie.exception;

/**
 * RN07: a análise de imagem depende da IA; sem ela, a API devolve 503.
 */
public class IaIndisponivelException extends RuntimeException {

    public IaIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
