package br.com.fiap.desconfie.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cria um único ModelMapper para o sistema todo. Os services o recebem pelo construtor
 * e usam modelMapper.map(origem, Destino.class) para converter DTO <-> model.
 *
 * STRICT: só copia campos com o nome exatamente igual nos dois lados,
 * evitando ligações erradas entre campos parecidos (ex.: usuario e usuarioId).
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
