package br.com.fiap.desconfie.exception;

import java.sql.SQLException;

/**
 * Envolve qualquer SQLException lançada pelos DAOs.
 *
 * O detalhe técnico (mensagem do Oracle) fica guardado só para o log do servidor;
 * o cliente recebe uma mensagem genérica, sem SQL, como a rubrica exige.
 */
public class BancoDeDadosException extends RuntimeException {

    public BancoDeDadosException(String mensagem, SQLException causa) {
        super(mensagem, causa);
    }

    /** Código de erro do Oracle (ex.: 1 = valor único repetido, 2292 = registro com filhos). */
    public int getCodigoOracle() {
        return getCause() instanceof SQLException sql ? sql.getErrorCode() : 0;
    }
}
