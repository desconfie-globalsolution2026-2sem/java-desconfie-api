package br.com.fiap.desconfie.enums;

/**
 * O semáforo: VERDE (nota abaixo de 30), AMARELO (30 a 59) e VERMELHO (60 ou mais).
 */
public enum NivelRisco {
    VERDE, AMARELO, VERMELHO;

    public static NivelRisco daNota(double notaFinal) {
        if (notaFinal >= 60) {
            return VERMELHO;
        }
        return notaFinal >= 30 ? AMARELO : VERDE;
    }
}
