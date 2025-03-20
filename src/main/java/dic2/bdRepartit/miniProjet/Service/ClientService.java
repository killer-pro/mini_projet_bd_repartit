package dic2.bdRepartit.miniProjet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dic2.bdRepartit.miniProjet.Model.Client;

@Service
public class ClientService {

    @Autowired
    private DataSourceRouter dataSourceRouter;

    private final RowMapper<Client> clientRowMapper = (rs, rowNum) -> {
        return Client.builder()
                .id(rs.getLong("id"))
                .nom(rs.getString("nom"))
                .ville(rs.getString("ville"))
                .email(rs.getString("email"))
                .build();
    };

    @Transactional
    public Client createClient(Client client) {
        // Normaliser le nom de la ville (en minuscules)
        String ville = client.getVille().toLowerCase();

        // Vérifier si la ville est prise en charge
        if (!isValidCity(ville)) {
            throw new IllegalArgumentException("Ville non prise en charge: " + ville);
        }

        // Choisir la bonne base de données selon la ville
        JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville);

        // Vérifier que la contrainte de ville est respectée
        if (!checkCityConstraint(db, ville)) {
            throw new IllegalArgumentException("La base de données " + ville +
                    " n'accepte que les clients de " + ville);
        }

        // Insertion du client
        String insertClientQuery =
                "INSERT INTO client (nom, ville, email) VALUES (?, ?, ?) RETURNING id";

        // Récupérer l'ID généré
        Long clientId = db.queryForObject(insertClientQuery, Long.class,
                client.getNom(), ville, client.getEmail());

        // Mettre à jour l'ID dans l'objet client
        client.setId(clientId);

        return client;
    }

    private boolean isValidCity(String ville) {
        return ville.equals("dakar") || ville.equals("thies") || ville.equals("saint-louis");
    }

    private boolean checkCityConstraint(JdbcTemplate db, String ville) {
        // Vérifier si la contrainte CHECK existe dans la table client
        String checkConstraintQuery =
                "SELECT COUNT(*) FROM information_schema.table_constraints " +
                        "WHERE table_name = 'client' AND constraint_name = 'check_ville'";

        Integer constraintCount = db.queryForObject(checkConstraintQuery, Integer.class);

        // Si la contrainte existe, vérifier sa valeur
        if (constraintCount > 0) {
            String getConstraintDefinitionQuery =
                    "SELECT pg_get_constraintdef(oid) FROM pg_constraint " +
                            "WHERE conname = 'check_ville'";

            String constraintDef = db.queryForObject(getConstraintDefinitionQuery, String.class);

            // La définition de la contrainte contient la ville attendue
            // Format typique: CHECK ((ville = 'dakar'::text))
            return constraintDef.contains("'" + ville + "'");
        }

        // Si pas de contrainte, on accepte toutes les villes
        return true;
    }
}