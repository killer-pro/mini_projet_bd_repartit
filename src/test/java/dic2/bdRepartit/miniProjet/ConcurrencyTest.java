package dic2.bdRepartit.miniProjet;

import dic2.bdRepartit.miniProjet.Service.CommandeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private CommandeService commandeService;

    @Autowired
    @Qualifier("dakarJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private static final Long PRODUIT_ID = 1L;
    private static final Integer STOCK_INITIAL = 100;
    private static final Integer CLIENTS_CONCURRENTS = 10;
    private static final Integer QUANTITE_PAR_CLIENT = 8;

    /**
     * Initialise le stock du produit pour le test
     */
    private void initialiserStock() {
        for (String ville : new String[]{"dakar", "thies", "saint-louis"}) {
            // Rediriger vers la base de données de la ville
            String updateStockSql =
                    "UPDATE produit_stock SET stock = ? WHERE id_produit = ?";
            jdbcTemplate.update(updateStockSql, STOCK_INITIAL, PRODUIT_ID);
        }
    }

    /**
     * Vérifie que le stock restant est cohérent avec les commandes passées
     */
    private boolean verifierCoherence() {
        int totalStock = 0;
        int totalCommande = 0;

        // Récupérer le stock restant dans toutes les villes
        for (String ville : new String[]{"dakar", "thies", "saint-louis"}) {
            Integer stock = jdbcTemplate.queryForObject(
                    "SELECT stock FROM produit_stock WHERE id_produit = ?",
                    Integer.class,
                    PRODUIT_ID
            );
            totalStock += (stock != null ? stock : 0);
        }

        // Récupérer le total des quantités commandées
        for (String ville : new String[]{"dakar", "thies", "saint-louis"}) {
            Integer quantiteCommandee = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(quantite), 0) FROM commande_detail WHERE id_produit = ?",
                    Integer.class,
                    PRODUIT_ID
            );
            totalCommande += (quantiteCommandee != null ? quantiteCommandee : 0);
        }

        // Vérifier que stock initial = stock restant + quantités commandées
        return (STOCK_INITIAL * 3) == (totalStock + totalCommande);
    }

    /**
     * Test de sérialisabilité des transactions concurrentes
     */
    @Test
    public void testTransactionSerializability() throws Exception {
        // Initialiser le stock
        initialiserStock();

        // Créer un pool de threads pour les clients concurrents
        ExecutorService executor = Executors.newFixedThreadPool(CLIENTS_CONCURRENTS);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger commandesReussies = new AtomicInteger(0);

        // Lancer les commandes concurrentes
        for (int i = 0; i < CLIENTS_CONCURRENTS; i++) {
            final Long clientId = (long) (i + 1);
            final String ville = i % 3 == 0 ? "dakar" : (i % 3 == 1 ? "thies" : "saint-louis");

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Créer une commande pour un seul produit
                    Map<Long, Integer> produitsCommandes = new HashMap<>();
                    produitsCommandes.put(PRODUIT_ID, QUANTITE_PAR_CLIENT);

                    // Tenter de créer la commande
                    commandeService.creerCommande(clientId, ville, produitsCommandes);
                    commandesReussies.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Commande échouée pour client " + clientId + " : " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        // Attendre que toutes les commandes soient traitées
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Vérifier les résultats
        System.out.println("Commandes réussies: " + commandesReussies.get() + "/" + CLIENTS_CONCURRENTS);

        // Vérifier la cohérence des données
        boolean coherent = verifierCoherence();
        System.out.println("Cohérence des données: " + (coherent ? "OK" : "ERREUR"));

        // Analyser la sérialisabilité
        analyserSerialisabilite();
    }

    /**
     * Analyse la sérialisabilité des transactions basée sur les logs
     */
    private void analyserSerialisabilite() {
        for (String ville : new String[]{"dakar", "thies", "saint-louis"}) {
            List<Map<String, Object>> logs = jdbcTemplate.queryForList(
                    "SELECT * FROM log_reservation_stock WHERE id_produit = ? ORDER BY timestamp",
                    PRODUIT_ID
            );

            System.out.println("\nJournal des opérations pour " + ville + ":");
            for (Map<String, Object> log : logs) {
                System.out.println(
                        log.get("timestamp") + " - " +
                                log.get("operation") + " - " +
                                "Quantité: " + log.get("quantite")
                );
            }
        }

        // Vérifier s'il y a eu des annulations (indice de conflits)
        for (String ville : new String[]{"dakar", "thies", "saint-louis"}) {
            Integer annulations = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM log_reservation_stock WHERE id_produit = ? AND operation = 'ANNULATION'",
                    Integer.class,
                    PRODUIT_ID
            );

            System.out.println("Annulations dans " + ville + ": " + annulations);
        }
    }
}