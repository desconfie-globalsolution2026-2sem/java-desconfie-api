package br.com.fiap.desconfie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Prepara o cliente HTTP que a API Java usa para chamar a API Python (Machine Learning).
 *
 * Os tempos-limite garantem que a análise não fique travada se a IA estiver lenta
 * ou fora do ar: depois do limite a chamada desiste e a análise segue só com as regras.
 */
@Configuration
public class IaClientConfig {

    @Bean
    public RestClient iaRestClient(@Value("${desconfie.ia.url}") String iaUrl) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        // No plano gratuito do Render a API Python "dorme" e leva cerca de 50 s para acordar
        fabrica.setConnectTimeout(Duration.ofSeconds(10)); // tempo para conseguir conectar
        fabrica.setReadTimeout(Duration.ofSeconds(60));    // tempo para a IA responder (OCR é o mais lento)
        return RestClient.builder()
                .baseUrl(iaUrl)
                .requestFactory(fabrica)
                .build();
    }
}
