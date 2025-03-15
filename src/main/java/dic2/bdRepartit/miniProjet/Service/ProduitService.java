package dic2.bdRepartit.miniProjet.Service;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import dic2.bdRepartit.miniProjet.Model.Produit;

@Service
public class ProduitService {

    @Autowired
    private DataSourceRouter dataSourceRouter;

    // Mapper pour convertir les résultats SQL en objets Produit
    private final RowMapper<Produit> produitRowMapper = (rs, rowNum) -> {
        return Produit.builder()
                .id(rs.getLong("id"))
                .nom(rs.getString("nom"))
                .description(rs.getString("description"))
                .prix(rs.getDouble("prix"))
                .stock(rs.getInt("stock"))
                .build();
    };


    public List<Produit> getAllProduits() {
        List<Produit> allProduits = new ArrayList<>();

        // Récupérer les produits de tous les serveurs
        String query = "SELECT * FROM produit";

        allProduits.addAll(dataSourceRouter.getDataSourceByCity("dakar").query(query, produitRowMapper));
        allProduits.addAll(dataSourceRouter.getDataSourceByCity("thies").query(query, produitRowMapper));
        allProduits.addAll(dataSourceRouter.getDataSourceByCity("saint-louis").query(query, produitRowMapper));

        return allProduits;
    }

    public Produit getProduitById(Long id) {
        String query = "SELECT * FROM produit WHERE id = ?";

        // Rechercher le produit dans toutes les bases de données
        try {
            return dataSourceRouter.getDataSourceByCity("dakar").queryForObject(query, produitRowMapper, id);
        } catch (Exception e) {
            try {
                return dataSourceRouter.getDataSourceByCity("thies").queryForObject(query, produitRowMapper, id);
            } catch (Exception e2) {
                try {
                    return dataSourceRouter.getDataSourceByCity("saint-louis").queryForObject(query, produitRowMapper, id);
                } catch (Exception e3) {
                    throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
                }
            }
        }
    }

    public List<Produit> getProduitsDisponibles(String ville) {
        String query = "SELECT * FROM produit WHERE stock > 0";

        if (ville != null && !ville.isEmpty()) {
            // Si une ville est spécifiée, récupérer les produits disponibles uniquement dans cette ville
            return dataSourceRouter.getDataSourceByCity(ville).query(query, produitRowMapper);
        } else {
            // Sinon, récupérer les produits disponibles dans toutes les villes
            return getAllProduits();
        }
    }

    public List<Produit> getProduitsRecommandesForClient(Long clientId) {
        // Récupérer la ville du client
        JdbcTemplate clientDb = dataSourceRouter.getDataSourceForClient(clientId);
        String queryClient = "SELECT ville FROM client WHERE id = ?";
        String clientVille = clientDb.queryForObject(queryClient, String.class, clientId);

        // Récupérer les produits disponibles dans la ville du client
        String queryProduits = "SELECT * FROM produit WHERE stock > 5 ORDER BY RANDOM() LIMIT 5";
        return dataSourceRouter.getDataSourceByCity(clientVille).query(queryProduits, produitRowMapper);
    }
}