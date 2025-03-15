package dic2.bdRepartit.miniProjet.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn({"dataSourceConfig"})
public class FlywayConfig {

    @Bean(name = "flywayInitializer")
    public Flyway flywayInitializer(@Qualifier("dakarDataSource") DataSource dakarDataSource,
                                    @Qualifier("thiesDataSource") DataSource thiesDataSource,
                                    @Qualifier("saintlouisDataSource") DataSource saintlouisDataSource) {

        // Configurer et exécuter Flyway pour Dakar
        Flyway flywayDakar = Flyway.configure()
                .dataSource(dakarDataSource)
                .locations("classpath:db/migration/dakar")
                .baselineOnMigrate(true)
                .validateMigrationNaming(true)
                .load();
        flywayDakar.migrate();

        // Configurer et exécuter Flyway pour Thiès
        Flyway flywayThies = Flyway.configure()
                .dataSource(thiesDataSource)
                .locations("classpath:db/migration/thies")
                .baselineOnMigrate(true)
                .validateMigrationNaming(true)
                .load();
        flywayThies.migrate();

        // Configurer et exécuter Flyway pour Saint-Louis
        Flyway flywaySaintLouis = Flyway.configure()
                .dataSource(saintlouisDataSource)
                .locations("classpath:db/migration/saintlouis")
                .baselineOnMigrate(true)
                .validateMigrationNaming(true)
                .load();
        flywaySaintLouis.migrate();

        // On retourne la dernière instance pour satisfaire le besoin du bean
        return flywaySaintLouis;
    }
}