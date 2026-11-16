package br.com.fiap.desconfie.service;

import java.text.Normalizer;

/**
 * Normaliza textos para comparação: minúsculas, sem acento e sem espaços repetidos.
 * Assim "Troquei de NÚMERO" e "troquei de numero" são considerados iguais.
 */
public final class TextoUtil {

    private TextoUtil() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
