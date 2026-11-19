package br.com.fiap.desconfie.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Chama a API Python, que lê o texto dos prints (OCR) e consulta o modelo de Machine Learning.
 *
 * Rede de segurança: se a IA estiver fora do ar, lenta ou com erro, cada método devolve
 * Optional vazio (ou nota vazia) e a análise segue só com as regras (iaDisponivel = false).
 */
@Service
public class IaClient {

    private static final Logger log = LoggerFactory.getLogger(IaClient.class);

    /** Resposta de POST /prever/texto. A API Python manda outros campos, que são ignorados. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespostaTexto(Double probabilidadeGolpe, String modelo) { }

    /** Resposta de POST /prever/url. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespostaUrl(Double probabilidadePhishing, String modelo) { }

    /** Resposta de POST /extrair: o texto lido e, dentro de analiseML.texto, a resposta do modelo. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RespostaExtracao(String textoExtraido, AnaliseMl analiseML) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AnaliseMl(ConsultaMl texto) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConsultaMl(Integer status, RespostaTexto resultado) { }

    /** O que a análise de imagem precisa: o texto do print e a nota (0 a 100), que pode faltar. */
    public record RespostaImagem(String textoExtraido, Double notaIa) { }

    private final RestClient iaRestClient;

    public IaClient(RestClient iaRestClient) {
        this.iaRestClient = iaRestClient;
    }

    /** POST /prever/texto -> nota de 0 a 100, ou vazio se a IA não respondeu. */
    public Optional<Double> notaTexto(String texto) {
        return chamar("/prever/texto", Map.of("texto", texto), RespostaTexto.class)
                .map(RespostaTexto::probabilidadeGolpe)
                .map(p -> p * 100);
    }

    /** POST /prever/url -> nota de 0 a 100, ou vazio se a IA não respondeu. */
    public Optional<Double> notaLink(String url) {
        return chamar("/prever/url", Map.of("url", url), RespostaUrl.class)
                .map(RespostaUrl::probabilidadePhishing)
                .map(p -> p * 100);
    }

    /**
     * POST /extrair (multipart, campo "arquivo") -> texto lido do print e a nota do modelo.
     *
     * - Se o OCR funcionar e só o modelo falhar, devolve o texto com notaIa vazia: a análise segue com as regras.
     * - Se a API Python responder 422 (nenhum texto na imagem), lança IllegalArgumentException (400 para o front).
     * - Se a API Python estiver fora do ar, devolve Optional vazio (503 para o front).
     */
    public Optional<RespostaImagem> analisarImagem(String imagemBase64) {
        byte[] imagem = decodificar(imagemBase64);
        String nomeArquivo = nomeDoArquivo(imagem);

        // ByteArrayResource precisa de um nome: a API Python confere se a extensão bate com o conteúdo
        ByteArrayResource arquivo = new ByteArrayResource(imagem) {
            @Override
            public String getFilename() {
                return nomeArquivo;
            }
        };
        MultiValueMap<String, Object> formulario = new LinkedMultiValueMap<>();
        formulario.add("arquivo", arquivo);

        try {
            RespostaExtracao resposta = iaRestClient.post().uri("/extrair")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(formulario)
                    .retrieve()
                    .body(RespostaExtracao.class);
            if (resposta == null || resposta.textoExtraido() == null || resposta.textoExtraido().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new RespostaImagem(resposta.textoExtraido(), notaDaExtracao(resposta)));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                throw new IllegalArgumentException(
                        "Não encontramos texto nesta imagem. Envie um print mais nítido ou cole o texto.");
            }
            log.warn("IA respondeu erro na análise de imagem: {}", e.getStatusCode());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("IA indisponível na análise de imagem: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** GET /health da API Python. */
    public boolean estaDisponivel() {
        try {
            iaRestClient.get().uri("/health").retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private <T> Optional<T> chamar(String rota, Map<String, String> corpo, Class<T> tipoResposta) {
        try {
            T resposta = iaRestClient.post().uri(rota)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(tipoResposta);
            return Optional.ofNullable(resposta);
        } catch (Exception e) {
            log.warn("IA indisponível em {}: {}", rota, e.getMessage());
            return Optional.empty();
        }
    }

    /** A nota só vale quando o modelo respondeu 200; senão fica vazia e a análise usa só as regras. */
    private Double notaDaExtracao(RespostaExtracao resposta) {
        if (resposta.analiseML() == null || resposta.analiseML().texto() == null) {
            return null;
        }
        ConsultaMl consulta = resposta.analiseML().texto();
        if (consulta.status() == null || consulta.status() != 200
                || consulta.resultado() == null || consulta.resultado().probabilidadeGolpe() == null) {
            log.warn("Modelo não deu nota para o texto da imagem (status {})", consulta.status());
            return null;
        }
        return consulta.resultado().probabilidadeGolpe() * 100;
    }

    /** Aceita o Base64 puro ou com o prefixo "data:image/png;base64,". */
    private byte[] decodificar(String imagemBase64) {
        String base64 = imagemBase64.trim();
        int virgula = base64.indexOf(',');
        if (base64.startsWith("data:") && virgula > 0) {
            base64 = base64.substring(virgula + 1);
        }
        try {
            return Base64.getMimeDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("A imagem enviada não é um Base64 válido.");
        }
    }

    /** Descobre se é PNG ou JPG pelos primeiros bytes do arquivo. */
    private String nomeDoArquivo(byte[] imagem) {
        if (imagem.length > 8 && (imagem[0] & 0xFF) == 0x89 && imagem[1] == 'P' && imagem[2] == 'N' && imagem[3] == 'G') {
            return "print.png";
        }
        if (imagem.length > 3 && (imagem[0] & 0xFF) == 0xFF && (imagem[1] & 0xFF) == 0xD8 && (imagem[2] & 0xFF) == 0xFF) {
            return "print.jpg";
        }
        throw new IllegalArgumentException("A imagem deve ser PNG ou JPG.");
    }
}
