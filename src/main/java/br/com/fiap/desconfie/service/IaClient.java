package br.com.fiap.desconfie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

/**
 * Chama a API Python (Machine Learning), que dá a outra metade da nota.
 *
 * Rede de segurança: se a IA estiver fora do ar, lenta ou com erro, cada método devolve
 * Optional vazio e a análise segue só com as regras (iaDisponivel = false).
 * O contrato dos JSONs está em docs/05-contrato-integracao.md (Parte 2).
 */
@Service
public class IaClient {

    private static final Logger log = LoggerFactory.getLogger(IaClient.class);

    /** Respostas da API Python. */
    public record RespostaIa(Double probabilidadeGolpe, String modelo) { }
    public record RespostaImagem(String textoExtraido, Double probabilidadeGolpe, String modelo) { }

    private final RestClient iaRestClient;

    public IaClient(RestClient iaRestClient) {
        this.iaRestClient = iaRestClient;
    }

    /** POST /analisar/texto -> nota de 0 a 100, ou vazio se a IA não respondeu. */
    public Optional<Double> notaTexto(String texto) {
        return chamar("/analisar/texto", Map.of("texto", texto));
    }

    /** POST /analisar/link -> nota de 0 a 100, ou vazio se a IA não respondeu. */
    public Optional<Double> notaLink(String url) {
        return chamar("/analisar/link", Map.of("url", url));
    }

    /**
     * POST /analisar/imagem -> texto lido do print (OCR) e a probabilidade de golpe.
     * Se a IA responder 422 (nenhum texto na imagem), lança IllegalArgumentException (400 para o front).
     */
    public Optional<RespostaImagem> analisarImagem(String imagemBase64) {
        try {
            RespostaImagem resposta = iaRestClient.post().uri("/analisar/imagem")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("imagemBase64", imagemBase64))
                    .retrieve()
                    .body(RespostaImagem.class);
            return Optional.ofNullable(resposta);
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

    private Optional<Double> chamar(String rota, Map<String, String> corpo) {
        try {
            RespostaIa resposta = iaRestClient.post().uri(rota)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(RespostaIa.class);
            if (resposta == null || resposta.probabilidadeGolpe() == null) {
                return Optional.empty();
            }
            return Optional.of(resposta.probabilidadeGolpe() * 100);
        } catch (Exception e) {
            log.warn("IA indisponível em {}: {}", rota, e.getMessage());
            return Optional.empty();
        }
    }
}
