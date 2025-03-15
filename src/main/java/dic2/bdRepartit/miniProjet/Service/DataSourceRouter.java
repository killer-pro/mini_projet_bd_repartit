package dic2.bdRepartit.miniProjet.Service;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataSourceRouter {

    private final JdbcTemplate dakarJdbcTemplate;
    private final JdbcTemplate thiesJdbcTemplate;
    private final JdbcTemplate saintlouisJdbcTemplate;

    private final Map<String, JdbcTemplate> cityDataSources = new HashMap<>();

    @Autowired
    public DataSourceRouter(
            @Qualifier("dakarJdbcTemplate") JdbcTemplate dakarJdbcTemplate,
            @Qualifier("thiesJdbcTemplate") JdbcTemplate thiesJdbcTemplate,
            @Qualifier("saintlouisJdbcTemplate") JdbcTemplate saintlouisJdbcTemplate) {

        this.dakarJdbcTemplate = dakarJdbcTemplate;
        this.thiesJdbcTemplate = thiesJdbcTemplate;
        this.saintlouisJdbcTemplate = saintlouisJdbcTemplate;

        cityDataSources.put("dakar", dakarJdbcTemplate);
        cityDataSources.put("thiès", thiesJdbcTemplate);
        cityDataSources.put("thies", thiesJdbcTemplate);
        cityDataSources.put("saint-louis", saintlouisJdbcTemplate);
    }

    public JdbcTemplate getDataSourceByCity(String city) {
        String normalizedCity = city.toLowerCase();
        return cityDataSources.getOrDefault(normalizedCity, dakarJdbcTemplate); // Dakar par défaut
    }

    public JdbcTemplate getDataSourceForClient(Long clientId) {
        // Déterminer la ville du client à partir de son ID
        String query = "SELECT ville FROM client WHERE id = ?";

        // Essayer d'abord la base de données de Dakar
        try {
            String city = dakarJdbcTemplate.queryForObject(query, String.class, clientId);
            return getDataSourceByCity(city);
        } catch (Exception e) {
            // Si le client n'est pas trouvé à Dakar, essayer Thiès
            try {
                String city = thiesJdbcTemplate.queryForObject(query, String.class, clientId);
                return getDataSourceByCity(city);
            } catch (Exception e2) {
                // Si le client n'est pas trouvé à Thiès, essayer Saint-Louis
                try {
                    String city = saintlouisJdbcTemplate.queryForObject(query, String.class, clientId);
                    return getDataSourceByCity(city);
                } catch (Exception e3) {
                    // Client non trouvé, retourner la base de données par défaut
                    return dakarJdbcTemplate;
                }
            }
        }
    }
}