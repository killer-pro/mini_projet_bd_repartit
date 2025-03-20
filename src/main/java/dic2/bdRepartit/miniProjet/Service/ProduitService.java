package dic2.bdRepartit.miniProjet.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import dic2.bdRepartit.miniProjet.Model.Produit;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProduitService {

    @Autowired
    private DataSourceRouter dataSourceRouter;
    private static final Logger logger = LoggerFactory.getLogger(ProduitService.class);

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

        // Requête corrigée pour joindre produit_info et produit_stock
        String query = "SELECT pi.id, pi.nom, pi.description, pi.prix, ps.stock " +
                "FROM produit_info pi " +
                "JOIN produit_stock ps ON pi.id = ps.id_produit";

        allProduits.addAll(dataSourceRouter.getDataSourceByCity("dakar").query(query, produitRowMapper));
        allProduits.addAll(dataSourceRouter.getDataSourceByCity("thies").query(query, produitRowMapper));
        allProduits.addAll(dataSourceRouter.getDataSourceByCity("saint-louis").query(query, produitRowMapper));

        return allProduits;
    }

    public Produit getProduitById(Long id) {
        String query = "SELECT pi.id, pi.nom, pi.description, pi.prix, ps.stock " +
                "FROM produit_info pi " +
                "JOIN produit_stock ps ON pi.id = ps.id_produit " +
                "WHERE pi.id = ?";
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
        String query = "SELECT pi.id, pi.nom, pi.description, pi.prix, ps.stock " +
                "FROM produit_info pi " +
                "JOIN produit_stock ps ON pi.id = ps.id_produit " +
                "WHERE ps.stock > 0";

        if (ville != null && !ville.isEmpty()) {
            // Si une ville est spécifiée, récupérer les produits disponibles uniquement dans cette ville
            return dataSourceRouter.getDataSourceByCity(ville).query(query, produitRowMapper);
        } else {
            // Sinon, récupérer les produits disponibles dans toutes les villes
            return getAllProduits();
        }
    }

    public List<Produit> getProduitsRecommandesForClient(Long clientId) {
        logger.info("Fetching recommended products for client with ID: {}", clientId);

        // Retrieve the client's city
        JdbcTemplate clientDb = dataSourceRouter.getDataSourceForClient(clientId);
        String queryClient = "SELECT ville FROM client WHERE id = ?";
        String clientVille;
        try {
            clientVille = clientDb.queryForObject(queryClient, String.class, clientId);
            logger.info("Client's city: {}", clientVille);
        } catch (Exception e) {
            logger.error("Error retrieving client's city for client ID: {}", clientId, e);
            throw e;
        }

        // Retrieve the recommended products in the client's city
        String queryProduits = "SELECT pi.id, pi.nom, pi.description, pi.prix, ps.stock " +
                "FROM produit_info pi " +
                "JOIN produit_stock ps ON pi.id = ps.id_produit " +
                "WHERE ps.stock > 5 ORDER BY RANDOM() LIMIT 5";
        try {
            List<Produit> produits = dataSourceRouter.getDataSourceByCity(clientVille).query(queryProduits, produitRowMapper);
            logger.info("Recommended products retrieved successfully for client ID: {}", clientId);
            return produits;
        } catch (Exception e) {
            logger.error("Error retrieving recommended products for client ID: {}", clientId, e);
            throw e;
        }
    }

    @Transactional
    public Produit createProduit(Produit produit) {
        // Création d'un nouveau produit dans toutes les bases

        String insertProduitInfoQuery =
                "INSERT INTO produit_info (nom, description, prix) VALUES (?, ?, ?) RETURNING id";

        // Récupérer l'ID généré
        Long productId = dataSourceRouter.getDataSourceByCity("dakar")
                .queryForObject(insertProduitInfoQuery, Long.class,
                        produit.getNom(), produit.getDescription(), produit.getPrix());

        // Mettre à jour l'ID dans l'objet produit
        produit.setId(productId);

        // Pour les autres bases, vous devez aussi spécifier l'ID
        String insertWithIdQuery =
                "INSERT INTO produit_info (id, nom, description, prix) VALUES (?, ?, ?, ?)";


        dataSourceRouter.getDataSourceByCity("thies")
                .update(insertWithIdQuery, productId, produit.getNom(),
                        produit.getDescription(), produit.getPrix());

        dataSourceRouter.getDataSourceByCity("saint-louis")
                .update(insertWithIdQuery, productId, produit.getNom(),
                        produit.getDescription(), produit.getPrix());
        // Initialiser le stock à zéro dans chaque entrepôt
        String insertStockQuery =
                "INSERT INTO produit_stock (id_produit, stock, entrepot) VALUES (?, ?, ?)";

        dataSourceRouter.getDataSourceByCity("dakar")
                .update(insertStockQuery, productId, 0, "Dakar-Centre");

        dataSourceRouter.getDataSourceByCity("thies")
                .update(insertStockQuery, productId, 0, "thies-Centre");

        dataSourceRouter.getDataSourceByCity("saint-louis")
                .update(insertStockQuery, productId, 0, "Saint-Louis-Centre");

        // Définir le stock à 0 dans l'objet retourné
        produit.setStock(0);

        return produit;
    }

    @Transactional
    public Produit updateStock(Long productId, Integer quantity, String ville) {
        JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville);

        // Générer un UUID pour la transaction distribuée
        UUID transactionId = UUID.randomUUID();

        // Enregistrer le début de la transaction
        String insertTransactionQuery =
                "INSERT INTO transaction_distribuee (id, statut, commentaire) VALUES (?, ?, ?)";
        db.update(insertTransactionQuery, transactionId, "EN_COURS",
                "Mise à jour du stock pour le produit " + productId);

        try {
            // Vérifier que le produit existe
            String checkProductQuery =
                    "SELECT COUNT(*) FROM produit_info WHERE id = ?";

            Integer productCount = db.queryForObject(checkProductQuery, Integer.class, productId);

            if (productCount == 0) {
                throw new RuntimeException("Produit non trouvé avec l'ID: " + productId);
            }

            // Mettre à jour le stock
            String updateStockQuery =
                    "UPDATE produit_stock SET stock = stock + ? WHERE id_produit = ?";

            db.update(updateStockQuery, quantity, productId);

            // Marquer la transaction comme terminée
            String updateTransactionQuery =
                    "UPDATE transaction_distribuee SET statut = ?, timestamp_fin = CURRENT_TIMESTAMP WHERE id = ?";

            db.update(updateTransactionQuery, "TERMINÉE", transactionId);

            // Récupérer le produit mis à jour
            return getProduitById(productId);

        } catch (Exception e) {
            // En cas d'erreur, marquer la transaction comme échouée
            String updateTransactionQuery =
                    "UPDATE transaction_distribuee SET statut = ?, timestamp_fin = CURRENT_TIMESTAMP, commentaire = ? WHERE id = ?";

            db.update(updateTransactionQuery, "ÉCHOUÉE",
                    "Erreur: " + e.getMessage(), transactionId);

            throw e;
        }
    }

    @Transactional
    public void transferStock(Long productId, Integer quantity, String fromVille, String toVille) {
        JdbcTemplate sourceDb = dataSourceRouter.getDataSourceByCity(fromVille);
        JdbcTemplate targetDb = dataSourceRouter.getDataSourceByCity(toVille);

        // Générer un UUID pour la transaction distribuée
        UUID transactionId = UUID.randomUUID();

        // Commencer la transaction dans les deux bases
        String insertTransactionQuery =
                "INSERT INTO transaction_distribuee (id, statut, commentaire) VALUES (?, ?, ?)";

        String transactionComment = "Transfert de stock du produit " + productId +
                " de " + fromVille + " vers " + toVille;

        sourceDb.update(insertTransactionQuery, transactionId, "EN_COURS", transactionComment);
        targetDb.update(insertTransactionQuery, transactionId, "EN_COURS", transactionComment);

        try {
            // Vérifier le stock disponible
            String checkStockQuery =
                    "SELECT stock FROM produit_stock WHERE id_produit = ?";

            Integer availableStock = sourceDb.queryForObject(checkStockQuery, Integer.class, productId);

            if (availableStock < quantity) {
                throw new RuntimeException("Stock insuffisant pour le transfert");
            }

            // Réduire le stock dans la ville source
            String decreaseStockQuery =
                    "UPDATE produit_stock SET stock = stock - ? WHERE id_produit = ?";

            sourceDb.update(decreaseStockQuery, quantity, productId);

            // Augmenter le stock dans la ville cible
            String increaseStockQuery =
                    "UPDATE produit_stock SET stock = stock + ? WHERE id_produit = ?";

            targetDb.update(increaseStockQuery, quantity, productId);

            // Marquer les transactions comme terminées
            String updateTransactionQuery =
                    "UPDATE transaction_distribuee SET statut = ?, timestamp_fin = CURRENT_TIMESTAMP WHERE id = ?";

            sourceDb.update(updateTransactionQuery, "TERMINÉE", transactionId);
            targetDb.update(updateTransactionQuery, "TERMINÉE", transactionId);

        } catch (Exception e) {
            // En cas d'erreur, marquer les transactions comme échouées
            String updateTransactionQuery =
                    "UPDATE transaction_distribuee SET statut = ?, timestamp_fin = CURRENT_TIMESTAMP, commentaire = ? WHERE id = ?";

            sourceDb.update(updateTransactionQuery, "ÉCHOUÉE",
                    "Erreur: " + e.getMessage(), transactionId);
            targetDb.update(updateTransactionQuery, "ÉCHOUÉE",
                    "Erreur: " + e.getMessage(), transactionId);

            throw e;
        }
    }
}