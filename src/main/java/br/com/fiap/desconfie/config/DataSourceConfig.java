package br.com.fiap.desconfie.config;

import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Cria a fonte de conexões com o Oracle (DataSource) usada por todos os DAOs.
 *
 * Substitui o ConnectionFactory estático: o Spring guarda o DataSource criado aqui
 * e o entrega pelo construtor de cada DAO (injeção de dependência).
 * Os dados de acesso vêm do application.properties, que por sua vez lê as
 * variáveis de ambiente DB_URL, DB_USER e DB_PASSWORD.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(@Value("${desconfie.db.url}") String url,
                                 @Value("${desconfie.db.usuario}") String usuario,
                                 @Value("${desconfie.db.senha}") String senha) throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(url);
        dataSource.setUser(usuario);
        dataSource.setPassword(senha);
        return dataSource;
    }
}
