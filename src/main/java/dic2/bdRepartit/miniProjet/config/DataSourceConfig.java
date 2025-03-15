package dic2.bdRepartit.miniProjet.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfig {

    @Bean(name = "dakarDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dakar")
    @Primary
    public DataSource dakarDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "thiesDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.thies")
    public DataSource thiesDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "saintlouisDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.saintlouis")
    public DataSource saintlouisDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dakarJdbcTemplate")
    public JdbcTemplate dakarJdbcTemplate(@Qualifier("dakarDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "thiesJdbcTemplate")
    public JdbcTemplate thiesJdbcTemplate(@Qualifier("thiesDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "saintlouisJdbcTemplate")
    public JdbcTemplate saintlouisJdbcTemplate(@Qualifier("saintlouisDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}