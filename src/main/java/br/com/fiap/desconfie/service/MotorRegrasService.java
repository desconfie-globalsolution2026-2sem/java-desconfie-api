package br.com.fiap.desconfie.service;

import br.com.fiap.desconfie.enums.TipoPadrao;
import br.com.fiap.desconfie.model.PadraoEncontrado;
import br.com.fiap.desconfie.model.PadraoGolpe;
import br.com.fiap.desconfie.model.ResultadoRegras;
import br.com.fiap.desconfie.model.SinalLink;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de regras: a metade da nota calculada pela API Java.
 *
 * Recebe o texto (ou link) e a lista de padrões ativos do banco e soma os pesos encontrados.
 * Cada padrão soma o peso UMA vez só, mesmo que apareça várias vezes (RN08), e a nota
 * fica limitada a 100. Não acessa o banco: assim dá para testar com testes unitários.
 */
@Service
public class MotorRegrasService {

    static final int NOTA_MAXIMA = 100;

    // Pesos dos sinais de link que são regras fixas do código (ver docs/02-arquitetura.md)
    static final int PESO_IP = 30;
    static final int PESO_ARROBA = 25;
    static final int PESO_SEM_HTTPS = 10;
    static final int PESO_SUBDOMINIOS = 15;
    static final int PESO_HIFENS = 10;
    static final int PESO_PUNYCODE = 30;

    private static final Pattern IP = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    /** Analisa uma mensagem (TEXTO ou o texto lido de uma IMAGEM). */
    public ResultadoRegras analisarTexto(String texto, List<PadraoGolpe> padroesAtivos) {
        String normalizado = TextoUtil.normalizar(texto);
        ResultadoRegras resultado = new ResultadoRegras();

        for (PadraoGolpe padrao : padroesAtivos) {
            if (padrao.getTipo() != TipoPadrao.TEXTO) {
                continue;
            }
            int ocorrencias = contarOcorrencias(normalizado, padrao.getTermo());
            if (ocorrencias > 0) {
                resultado.getPadroes().add(new PadraoEncontrado(padrao, ocorrencias));
                resultado.setNota(resultado.getNota() + padrao.getPeso());   // soma uma vez só
            }
        }
        resultado.setNota(Math.min(resultado.getNota(), NOTA_MAXIMA));
        return resultado;
    }

    /** Analisa um LINK: padrões do banco (TLD, encurtador, marca, domínio oficial) + sinais do código. */
    public ResultadoRegras analisarLink(String url, List<PadraoGolpe> padroesAtivos) {
        String link = url.trim();
        ResultadoRegras resultado = new ResultadoRegras();

        // '@' na URL esconde o destino real: tudo antes do @ é ignorado pelo navegador
        if (link.contains("@")) {
            adicionarSinal(resultado, "O link tem '@', que pode esconder o site de destino real", PESO_ARROBA);
        }
        if (link.toLowerCase().startsWith("http://")) {
            adicionarSinal(resultado, "O link não usa conexão segura (HTTPS)", PESO_SEM_HTTPS);
        }

        String host = extrairHost(link);

        if (IP.matcher(host).matches()) {
            adicionarSinal(resultado, "O link usa um número de IP no lugar do nome do site", PESO_IP);
        }
        if (host.contains("xn--")) {
            adicionarSinal(resultado, "O endereço usa letras que imitam outras (punycode)", PESO_PUNYCODE);
        }
        if (contar(host, '.') > 3) {
            adicionarSinal(resultado, "O endereço tem muitos subdomínios", PESO_SUBDOMINIOS);
        }
        if (contar(host, '-') >= 3) {
            adicionarSinal(resultado, "O endereço tem muitos hífens", PESO_HIFENS);
        }

        // Domínio oficial primeiro: uma marca dentro do site oficial dela não é imitação
        boolean oficial = padroesAtivos.stream()
                .filter(p -> p.getTipo() == TipoPadrao.DOMINIO_OFICIAL)
                .anyMatch(p -> pertenceAoDominio(host, p.getTermo()));
        resultado.setDominioOficial(oficial);

        for (PadraoGolpe padrao : padroesAtivos) {
            boolean encontrado = switch (padrao.getTipo()) {
                case TLD -> host.endsWith(padrao.getTermo().startsWith(".") ? padrao.getTermo() : "." + padrao.getTermo());
                case ENCURTADOR -> pertenceAoDominio(host, padrao.getTermo());
                case MARCA -> !oficial && host.contains(padrao.getTermo());
                default -> false;   // TEXTO não se aplica a link; DOMINIO_OFICIAL já foi tratado
            };
            if (encontrado) {
                resultado.getPadroes().add(new PadraoEncontrado(padrao, 1));
                resultado.setNota(resultado.getNota() + padrao.getPeso());
            }
        }
        resultado.setNota(Math.min(resultado.getNota(), NOTA_MAXIMA));
        return resultado;
    }

    /** Recalcula só os sinais fixos de um link (usado ao consultar uma análise já gravada). */
    public List<SinalLink> sinaisDoLink(String url) {
        return analisarLink(url, List.of()).getSinais();
    }

    /**
     * Pega o nome do site (host) da URL, em minúsculas e sem "www.".
     * Se o link vier sem "http://", o esquema é acrescentado só para conseguir ler o host.
     */
    String extrairHost(String link) {
        String comEsquema = link.matches("(?i)^[a-z][a-z0-9+.-]*://.*") ? link : "http://" + link;
        try {
            String host = new URI(comEsquema.replace(" ", "%20")).getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Link inválido. Envie um endereço como https://exemplo.com.br");
            }
            host = host.toLowerCase();
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Link inválido. Envie um endereço como https://exemplo.com.br");
        }
    }

    /** host é o próprio domínio ou um subdomínio dele (ex.: app.itau.com.br pertence a itau.com.br). */
    private boolean pertenceAoDominio(String host, String dominio) {
        return host.equals(dominio) || host.endsWith("." + dominio);
    }

    /** Conta o termo como palavra inteira: "pix" conta em "um pix", mas não em "pixel". */
    private int contarOcorrencias(String texto, String termo) {
        Matcher m = Pattern.compile("(?<![a-z0-9])" + Pattern.quote(termo) + "(?![a-z0-9])").matcher(texto);
        int total = 0;
        while (m.find()) {
            total++;
        }
        return total;
    }

    private void adicionarSinal(ResultadoRegras resultado, String descricao, int peso) {
        resultado.getSinais().add(new SinalLink(descricao, peso));
        resultado.setNota(resultado.getNota() + peso);
    }

    private int contar(String texto, char caractere) {
        return (int) texto.chars().filter(c -> c == caractere).count();
    }
}
