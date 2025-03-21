package dic2.bdRepartit.miniProjet.Service;

import dic2.bdRepartit.miniProjet.Model.Client;
import dic2.bdRepartit.miniProjet.Model.Commande;
import dic2.bdRepartit.miniProjet.Model.CommandeDetail;
import dic2.bdRepartit.miniProjet.Model.Produit;
import dic2.bdRepartit.miniProjet.Model.StockRepartition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommandeService {

    @Autowired
    private DataSourceRouter dataSourceRouter;

    @Autowired
    private ProduitService produitService;

    private static final Logger logger = LoggerFactory.getLogger(CommandeService.class);

    private static final List<String> VILLES = Arrays.asList("dakar", "thies", "saint-louis");

    private final RowMapper<Commande> commandeRowMapper = (rs, rowNum) -> {
        return Commande.builder()
                .id(rs.getLong("id"))
                .idClient(rs.getLong("id_client"))
                .dateCommande(rs.getTimestamp("date_commande").toLocalDateTime())
                .statut(rs.getString("statut"))
                .montantTotal(rs.getDouble("montant_total"))
                .ville(rs.getString("ville"))
                .build();
    };

    private final RowMapper<CommandeDetail> commandeDetailRowMapper = (rs, rowNum) -> {
        return CommandeDetail.builder()
                .id(rs.getLong("id"))
                .idCommande(rs.getLong("id_commande"))
                .idProduit(rs.getLong("id_produit"))
                .quantite(rs.getInt("quantite"))
                .prixUnitaire(rs.getDouble("prix_unitaire"))
                .ville(rs.getString("ville"))
                .build();
    };

    private final RowMapper<StockRepartition> stockRepartitionRowMapper = (rs, rowNum) -> {
        return StockRepartition.builder()
                .id(rs.getLong("id"))
                .idCommande(rs.getLong("id_commande"))
                .idProduit(rs.getLong("id_produit"))
                .quantite(rs.getInt("quantite"))
                .villeSource(rs.getString("ville_source"))
                .villeDestination(rs.getString("ville_destination"))
                .build();
    };

    /**
     * Crée une nouvelle commande pour un client avec plusieurs produits
     * en s'assurant que le stock est disponible à travers les différentes villes.
     *
     * @param idClient L'ID du client qui passe la commande
     * @param ville La ville où la commande est passée
     * @param produitsCommandes Map des produits commandés (ID produit -> quantité)
     * @return La commande créée
     */
    @Transactional
    public Commande creerCommande(Long idClient, String ville, Map<Long, Integer> produitsCommandes) {
        logger.info("Création d'une commande pour le client ID: {} dans la ville: {}", idClient, ville);

        // Normaliser la ville (convertir en minuscules pour correspondre à la convention existante)
        String clientVille = ville.toLowerCase();

        // Vérifier que la ville est valide
        if (!VILLES.contains(clientVille)) {
            throw new IllegalArgumentException("Ville non valide: " + clientVille);
        }

        // Vérifier que le client existe
        verifierClientExiste(idClient);

        // 1. Générer un UUID pour la transaction distribuée
        UUID transactionId = UUID.randomUUID();

        // 2. Enregistrer le début de la transaction dans toutes les bases
        for (String v : VILLES) {
            JdbcTemplate db = dataSourceRouter.getDataSourceByCity(v);
            String insertTransactionQuery =
                    "INSERT INTO transaction_distribuee (id, statut, timestamp_debut, commentaire) " +
                            "VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
            db.update(insertTransactionQuery, transactionId, "EN_COURS",
                    "Création de commande pour client " + idClient + " dans la ville " + clientVille);
        }

        try {
            // 3. Vérifier la disponibilité des produits et calculer le montant total
            double montantTotal = 0.0;
            Map<Long, Double> prixProduits = new HashMap<>();
            Map<Long, Map<String, Integer>> repartitionStock = new HashMap<>();

            // Pour chaque produit commandé
            for (Map.Entry<Long, Integer> entry : produitsCommandes.entrySet()) {
                Long idProduit = entry.getKey();
                Integer quantiteRequise = entry.getValue();

                if (quantiteRequise <= 0) {
                    throw new IllegalArgumentException("La quantité doit être positive pour le produit " + idProduit);
                }

                // Obtenir les informations sur le produit
                Produit produit = produitService.getProduitById(idProduit);
                prixProduits.put(idProduit, produit.getPrix());

                // Initialiser la répartition pour ce produit
                Map<String, Integer> repartitionProduit = new HashMap<>();
                repartitionStock.put(idProduit, repartitionProduit);

                // Vérifier d'abord dans la ville où la commande est passée
                Integer quantiteRestante = reserverStock(idProduit, quantiteRequise, clientVille, repartitionProduit);

                // Si le stock est insuffisant, chercher dans les autres villes
                if (quantiteRestante > 0) {
                    for (String v : VILLES) {
                        if (!v.equals(clientVille)) {
                            quantiteRestante = reserverStock(idProduit, quantiteRestante, v, repartitionProduit);
                            if (quantiteRestante == 0) break;
                        }
                    }
                }

                // S'il reste des produits non disponibles
                if (quantiteRestante > 0) {
                    // Annuler toutes les réservations
                    for (Map.Entry<Long, Map<String, Integer>> produitEntry : repartitionStock.entrySet()) {
                        for (Map.Entry<String, Integer> villeEntry : produitEntry.getValue().entrySet()) {
                            annulerReservationStock(produitEntry.getKey(), villeEntry.getValue(), villeEntry.getKey());
                        }
                    }

                    throw new RuntimeException("Stock insuffisant pour le produit " + idProduit
                            + ". Manque " + quantiteRestante + " unités.");
                }

                // Calculer le montant total
                montantTotal += produit.getPrix() * quantiteRequise;
            }

            // 4. Créer la commande dans la base de données de la ville spécifiée
            JdbcTemplate clientDb = dataSourceRouter.getDataSourceByCity(clientVille);
            KeyHolder keyHolder = new GeneratedKeyHolder();

            double finalMontantTotal = montantTotal;
            clientDb.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO commande (id_client, date_commande, statut, montant_total, ville) " +
                                "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, idClient);
                ps.setString(2, "Nouvelle");
                ps.setDouble(3, finalMontantTotal);
                ps.setString(4, clientVille);
                return ps;
            }, keyHolder);

            Long idCommande = ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("id")).longValue();
            logger.info("Commande créée avec ID: {}", idCommande);

            // 5. Créer les détails de commande
            List<CommandeDetail> details = new ArrayList<>();
            List<StockRepartition> repartitions = new ArrayList<>();

            for (Map.Entry<Long, Integer> entry : produitsCommandes.entrySet()) {
                Long idProduit = entry.getKey();
                Integer quantiteTotal = entry.getValue();
                Double prixUnitaire = prixProduits.get(idProduit);

                // Créer un seul enregistrement détail par produit (indépendamment de la répartition)
                CommandeDetail detail = CommandeDetail.builder()
                        .idCommande(idCommande)
                        .idProduit(idProduit)
                        .quantite(quantiteTotal)
                        .prixUnitaire(prixUnitaire)
                        .ville(clientVille)
                        .build();

                clientDb.update(
                        "INSERT INTO commande_detail (id_commande, id_produit, quantite, prix_unitaire, ville) " +
                                "VALUES (?, ?, ?, ?, ?)",
                        idCommande, idProduit, quantiteTotal, prixUnitaire, clientVille
                );

                details.add(detail);

                // Enregistrer la répartition de stock par ville
                Map<String, Integer> villesQuantites = repartitionStock.get(idProduit);
                for (Map.Entry<String, Integer> villeEntry : villesQuantites.entrySet()) {
                    String villeSource = villeEntry.getKey();
                    Integer quantite = villeEntry.getValue();

                    if (quantite > 0) {
                        // Insérer dans la table de répartition de stock
                        KeyHolder repartitionKeyHolder = new GeneratedKeyHolder();
                        clientDb.update(connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                    "INSERT INTO stock_repartition (id_commande, id_produit, quantite, ville_source, ville_destination) " +
                                            "VALUES (?, ?, ?, ?, ?)",
                                    Statement.RETURN_GENERATED_KEYS);
                            ps.setLong(1, idCommande);
                            ps.setLong(2, idProduit);
                            ps.setInt(3, quantite);
                            ps.setString(4, villeSource);
                            ps.setString(5, clientVille);
                            return ps;
                        }, repartitionKeyHolder);

                        Long idRepartition = ((Number) repartitionKeyHolder.getKeys().get("id")).longValue();
                        StockRepartition repartition = StockRepartition.builder()
                                .id(idRepartition)
                                .idCommande(idCommande)
                                .idProduit(idProduit)
                                .quantite(quantite)
                                .villeSource(villeSource)
                                .villeDestination(clientVille)
                                .build();

                        repartitions.add(repartition);
                    }
                }
            }

            // 6. Garder trace de la répartition dans un journal
            for (StockRepartition repartition : repartitions) {
                String commentaire = String.format(
                        "Commande %d: %d unités du produit %d prélevées de %s vers %s",
                        repartition.getIdCommande(), repartition.getQuantite(),
                        repartition.getIdProduit(), repartition.getVilleSource(),
                        repartition.getVilleDestination()
                );

                clientDb.update(
                        "UPDATE transaction_distribuee SET commentaire = CONCAT(commentaire, E'\\n', ?) WHERE id = ?",
                        commentaire, transactionId
                );
            }

            // 7. Marquer les transactions comme terminées
            for (String v : VILLES) {
                JdbcTemplate db = dataSourceRouter.getDataSourceByCity(v);
                String updateTransactionQuery =
                        "UPDATE transaction_distribuee SET statut = ?, timestamp_fin = CURRENT_TIMESTAMP WHERE id = ?";
                db.update(updateTransactionQuery, "TERMINÉE", transactionId);
            }

            // 8. Retourner la commande créée
            Commande commande = Commande.builder()
                    .id(idCommande)
                    .idClient(idClient)
                    .dateCommande(LocalDateTime.now())
                    .statut("Nouvelle")
                    .montantTotal(montantTotal)
                    .details(details)
                    .stockRepartitions(repartitions)
                    .ville(clientVille)
                    .build();

            logger.info("Commande {} créée avec succès pour le client {} dans la ville {}", idCommande, idClient, clientVille);
            return commande;

        } catch (Exception e) {
            // En cas d'erreur, marquer les transactions comme échouées
            for (String v : VILLES) {
                JdbcTemplate db = dataSourceRouter.getDataSourceByCity(v);
                String updateTransactionQuery =
                        "UPDATE transaction_distribuee SET statut = ?, timestamp_fin = CURRENT_TIMESTAMP, commentaire = ? WHERE id = ?";
                db.update(updateTransactionQuery, "ÉCHOUÉE", "Erreur: " + e.getMessage(), transactionId);
            }

            logger.error("Erreur lors de la création de la commande pour le client {} dans la ville {}: {}",
                    idClient, clientVille, e.getMessage());
            throw e;
        }
    }

    /**
     * Vérifie que le client existe dans n'importe quelle ville
     *
     * @param idClient L'ID du client à vérifier
     * @throws RuntimeException si le client n'existe pas
     */
    private void verifierClientExiste(Long idClient) {
        for (String v : VILLES) {
            try {
                JdbcTemplate db = dataSourceRouter.getDataSourceByCity(v);
                String query = "SELECT COUNT(*) FROM client WHERE id = ?";
                Integer count = db.queryForObject(query, Integer.class, idClient);
                if (count != null && count > 0) {
                    return; // Le client existe
                }
            } catch (Exception e) {
                // Ignorer l'erreur et continuer avec la ville suivante
                continue;
            }
        }
        throw new RuntimeException("Client non trouvé avec l'ID: " + idClient);
    }
    /**
     * Récupère la ville d'un client en cherchant dans toutes les bases de données
     *
     * @param idClient L'ID du client
     * @return La ville du client
     */
    private String getClientCity(Long idClient) {
        for (String ville : VILLES) {
            try {
                JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville);
                String query = "SELECT ville FROM client WHERE id = ?";
                return db.queryForObject(query, String.class, idClient);
            } catch (Exception e) {
                // Si le client n'est pas trouvé dans cette ville, essayer la suivante
                continue;
            }
        }
        throw new RuntimeException("Client non trouvé avec l'ID: " + idClient);
    }

    /**
     * Tente de réserver une quantité de stock d'un produit dans une ville spécifique
     * en utilisant un verrouillage explicite pour éviter les problèmes de concurrence
     *
     * @param idProduit L'ID du produit
     * @param quantiteRequise La quantité nécessaire
     * @param ville La ville où chercher le stock
     * @param repartition La map de répartition à mettre à jour
     * @return La quantité restante à réserver (0 si tout est réservé)
     */
    private Integer reserverStock(Long idProduit, Integer quantiteRequise, String ville, Map<String, Integer> repartition) {
        if (quantiteRequise <= 0) return 0;

        JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville);

        try {
            // Verrouiller la ligne de stock du produit avec FOR UPDATE (verrouillage pessimiste)
            String checkStockQuery = "SELECT stock, version FROM produit_stock WHERE id_produit = ? FOR UPDATE";
            Map<String, Object> stockInfo = db.queryForMap(checkStockQuery, idProduit);

            if (stockInfo == null) {
                logger.warn("Produit {} non trouvé en stock dans {}", idProduit, ville);
                return quantiteRequise;
            }

            Integer stockDisponible = (Integer) stockInfo.get("stock");
            Long version = (Long) stockInfo.get("version");

            // Calculer combien peut être réservé
            Integer quantiteAReserver = Math.min(stockDisponible, quantiteRequise);

            if (quantiteAReserver > 0) {
                // Mettre à jour le stock avec vérification de version optimiste
                String updateStockQuery = "UPDATE produit_stock SET stock = stock - ?, version = version + 1 " +
                        "WHERE id_produit = ? AND version = ?";
                int rowsUpdated = db.update(updateStockQuery, quantiteAReserver, idProduit, version);

                if (rowsUpdated == 0) {
                    // La version a changé entre-temps, retenter l'opération
                    logger.warn("Conflit de version détecté pour le produit {} dans {}. Réessai...", idProduit, ville);
                    return reserverStock(idProduit, quantiteRequise, ville, repartition);
                }

                // Enregistrer la réservation dans la table des transactions
                String insertLogQuery = "INSERT INTO log_reservation_stock (id_produit, quantite, ville, operation, timestamp) " +
                        "VALUES (?, ?, ?, 'RESERVE', CURRENT_TIMESTAMP)";
                db.update(insertLogQuery, idProduit, quantiteAReserver, ville);

                // Mettre à jour la répartition
                repartition.put(ville, quantiteAReserver);

                logger.info("Réservé {} unités du produit {} dans {}", quantiteAReserver, idProduit, ville);
            }

            // Retourner la quantité encore nécessaire
            return quantiteRequise - quantiteAReserver;
        } catch (Exception e) {
            logger.error("Erreur lors de la réservation du stock pour le produit {} dans {}: {}",
                    idProduit, ville, e.getMessage());
            return quantiteRequise;
        }
    }
    /**
     * Annule une réservation de stock en cas d'échec de la transaction
     * avec gestion de la concurrence
     *
     * @param idProduit L'ID du produit
     * @param quantite La quantité à remettre en stock
     * @param ville La ville concernée
     */
    private void annulerReservationStock(Long idProduit, Integer quantite, String ville) {
        if (quantite <= 0) return;

        JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville);

        try {
            // Verrouillage de la ligne pour mise à jour atomique
            String lockQuery = "SELECT version FROM produit_stock WHERE id_produit = ? FOR UPDATE";
            Long version = db.queryForObject(lockQuery, Long.class, idProduit);

            if (version == null) {
                logger.warn("Impossible d'annuler la réservation: produit {} non trouvé dans {}", idProduit, ville);
                return;
            }

            // Mise à jour atomique avec incrémentation de version
            String updateStockQuery = "UPDATE produit_stock SET stock = stock + ?, version = version + 1 WHERE id_produit = ?";
            db.update(updateStockQuery, quantite, idProduit);

            // Journalisation de l'annulation
            String insertLogQuery = "INSERT INTO log_reservation_stock (id_produit, quantite, ville, operation, timestamp) " +
                    "VALUES (?, ?, ?, 'ANNULATION', CURRENT_TIMESTAMP)";
            db.update(insertLogQuery, idProduit, quantite, ville);

            logger.info("Annulation de la réservation de {} unités du produit {} dans {}",
                    quantite, idProduit, ville);
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation de la réservation pour le produit {} dans {}: {}",
                    idProduit, ville, e.getMessage());
        }
    }

    /**
     * Récupère une commande par son ID et sa ville
     *
     * @param idCommande L'ID de la commande
     * @param ville La ville où chercher la commande (optionnel)
     * @return La commande avec ses détails
     */
    public Commande getCommandeById(Long idCommande, String ville) {
        // Si la ville est spécifiée, chercher uniquement dans cette ville
        if (ville != null && !ville.isEmpty()) {
            JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville.toLowerCase());
            try {
                String query = "SELECT * FROM commande WHERE id = ?";
                Commande commande = db.queryForObject(query, commandeRowMapper, idCommande);

                if (commande != null) {
                    // Récupérer les détails de la commande
                    String detailsQuery = "SELECT * FROM commande_detail WHERE id_commande = ?";
                    List<CommandeDetail> details = db.query(detailsQuery, commandeDetailRowMapper, idCommande);
                    commande.setDetails(details);

                    // Récupérer les informations de répartition de stock
                    String repartitionQuery = "SELECT * FROM stock_repartition WHERE id_commande = ?";
                    List<StockRepartition> repartitions = db.query(repartitionQuery, stockRepartitionRowMapper, idCommande);
                    commande.setStockRepartitions(repartitions);

                    return commande;
                }
            } catch (Exception e) {
                throw new RuntimeException("Commande non trouvée avec l'ID: " + idCommande + " dans la ville: " + ville);
            }
        }

        // Si la ville n'est pas spécifiée, chercher dans toutes les villes
        for (String villeActuelle : VILLES) {
            try {
                JdbcTemplate db = dataSourceRouter.getDataSourceByCity(villeActuelle);
                String query = "SELECT * FROM commande WHERE id = ?";
                Commande commande = db.queryForObject(query, commandeRowMapper, idCommande);

                // Si trouvé, récupérer les détails
                if (commande != null) {
                    String detailsQuery = "SELECT * FROM commande_detail WHERE id_commande = ?";
                    List<CommandeDetail> details = db.query(detailsQuery, commandeDetailRowMapper, idCommande);
                    commande.setDetails(details);

                    // Récupérer les informations de répartition de stock
                    String repartitionQuery = "SELECT * FROM stock_repartition WHERE id_commande = ?";
                    List<StockRepartition> repartitions = db.query(repartitionQuery, stockRepartitionRowMapper, idCommande);
                    commande.setStockRepartitions(repartitions);

                    return commande;
                }
            } catch (Exception e) {
                // Si la commande n'est pas trouvée dans cette ville, essayer la suivante
                continue;
            }
        }
        throw new RuntimeException("Commande non trouvée avec l'ID: " + idCommande);
    }

    /**
     * Récupère toutes les commandes d'un client
     *
     * @param idClient L'ID du client
     * @return La liste des commandes du client avec leurs détails
     */
    public List<Commande> getCommandesByClientId(Long idClient) {
        String clientVille = getClientCity(idClient);
        JdbcTemplate db = dataSourceRouter.getDataSourceByCity(clientVille);

        String query = "SELECT * FROM commande WHERE id_client = ?";
        List<Commande> commandes = db.query(query, commandeRowMapper, idClient);

        // Pour chaque commande, récupérer les détails
        for (Commande commande : commandes) {
            String detailsQuery = "SELECT * FROM commande_detail WHERE id_commande = ?";
            List<CommandeDetail> details = db.query(detailsQuery, commandeDetailRowMapper, commande.getId());
            commande.setDetails(details);

            // Récupérer les informations de répartition de stock
            String repartitionQuery = "SELECT * FROM stock_repartition WHERE id_commande = ?";
            List<StockRepartition> repartitions = db.query(repartitionQuery, stockRepartitionRowMapper, commande.getId());
            commande.setStockRepartitions(repartitions);
        }

        return commandes;
    }


}