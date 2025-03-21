##  Partie 1: Modélisation et Fragmentation des Données
### a. Fragmentation horizontale de la table Client
Pour une fragmentation horizontale de la table Client, nous allons diviser les données selon les villes. Chaque site (Dakar, Thiès, Saint-Louis) aura sa propre table contenant les clients de sa région:
```sql
-- Pour le site de Dakar
CREATE TABLE Client_Dakar (
    ID INT PRIMARY KEY,
    Nom VARCHAR(100),
    Ville VARCHAR(50) CHECK (Ville = 'Dakar'),
    Email VARCHAR(100)
);

-- Pour le site de Thiès
CREATE TABLE Client_Thies (
    ID INT PRIMARY KEY,
    Nom VARCHAR(100),
    Ville VARCHAR(50) CHECK (Ville = 'Thiès'),
    Email VARCHAR(100)
);

-- Pour le site de Saint-Louis
CREATE TABLE Client_SaintLouis (
    ID INT PRIMARY KEY,
    Nom VARCHAR(100),
    Ville VARCHAR(50) CHECK (Ville = 'Saint-Louis'),
    Email VARCHAR(100)
);

-- Pour les autres villes
CREATE TABLE Client_Autres (
    ID INT PRIMARY KEY,
    Nom VARCHAR(100),
    Ville VARCHAR(50) CHECK (Ville NOT IN ('Dakar', 'Thiès', 'Saint-Louis')),
    Email VARCHAR(100)
);
```
Cette fragmentation horizontale permettra d'optimiser les accès en stockant les données clients près de leur localisation physique.
### b. Fragmentation verticale de la table Produit
Pour la fragmentation verticale de la table Produit, nous allons diviser les informations générales des produits et celles spécifiques au stock:
```sql
-- Table des informations générales des produits (accessible depuis tous les sites)
CREATE TABLE Produit_Info (
    ID INT PRIMARY KEY,
    Nom VARCHAR(100),
    Description TEXT,
    Prix DECIMAL(10, 2)
);

-- Table des stocks de produits (peut être fragmentée par site également)
CREATE TABLE Produit_Stock (
    ID INT PRIMARY KEY,
    Stock INT,
    FOREIGN KEY (ID) REFERENCES Produit_Info(ID)
);
```
Cette fragmentation verticale permet de séparer les informations qui changent rarement (nom, description, prix) de celles qui sont fréquemment mises à jour (stock).
### c. Implémentation des tables SQL avec insertion de données
Les schemas des tables sont montrés plus haut ,
Voici le script SQL pour insérer les données d'exemple:
```sql
-- Insertion des données clients dans les tables fragmentées
INSERT INTO Client_Dakar (ID, Nom, Ville, Email) VALUES
(1, 'Mamadou Ndiaye', 'Dakar', 'mndiaye@ept.sn'),
(4, 'Fatou Sow', 'Dakar', 'fsow@ept.sn'),
(8, 'Aissatou Diallo', 'Dakar', 'adiallo@ept.sn');

INSERT INTO Client_Thies (ID, Nom, Ville, Email) VALUES
(2, 'Awa Diop', 'Thiès', 'adiop@ept.sn'),
(6, 'Khadija Seck', 'Thiès', 'kseck@ept.sn');

INSERT INTO Client_SaintLouis (ID, Nom, Ville, Email) VALUES
(3, 'Ibrahima Ba', 'Saint-Louis', 'iba@ept.sn'),
(9, 'Cheikh Diagne', 'Saint-Louis', 'cdiagne@ept.sn');

INSERT INTO Client_Autres (ID, Nom, Ville, Email) VALUES
(5, 'Oumar Fall', 'Ziguinchor', 'ofall@ept.sn'),
(7, 'Modou Gaye', 'Kaolack', 'mgaye@ept.sn'),
(10, 'Seynabou Sy', 'Ziguinchor', 'ssy@ept.sn');

-- Insertion des données produits dans les tables fragmentées
INSERT INTO Produit_Info (ID, Nom, Description, Prix) VALUES
(101, 'Ordinateur HP', 'PC portable 15 pouces', 450000),
(102, 'Smartphone Samsung', 'Galaxy S22, 128 Go', 350000),
(103, 'Casque Bluetooth', 'Casque sans fil Sony', 75000),
(104, 'Tablette iPad', 'iPad Air 10.9 pouces', 600000),
(105, 'Disque Dur Externe', '1 To, USB 3.0', 55000),
(106, 'Clavier Mécanique', 'Clavier gamer rétroéclairé', 65000),
(107, 'Imprimante HP Laser', 'Imprimante laser monochrome', 175000),
(108, 'Chargeur Universel', 'Chargeur rapide 65W', 30000),
(109, 'Écouteurs sans fil', 'Écouteurs Bluetooth', 45000),
(110, 'Souris Gamer', 'Souris optique 8000 DPI', 40000);

INSERT INTO Produit_Stock (ID, Stock) VALUES
(101, 20),
(102, 50),
(103, 30),
(104, 15),
(105, 40),
(106, 25),
(107, 10),
(108, 60),
(109, 35),
(110, 45);
```
#### Cette structure permet une gestion optimisée des données client par région et sépare les informations produits selon leur nature (stable vs variable).

## Partie 2: Répartition des Données et Accès Optimisé
## Partie 3: Transactions Réparties et Gestion des Commandes
## Partie 4: Contrôle de Concurrence et Sérialisabilité
### a. Problèmes potentiels de concurrence lors de la mise à jour du stock
Dans le système actuel, plusieurs problèmes de concurrence peuvent survenir lors de la mise à jour du stock:

1. Race conditions sur le stock:
Dans la méthode reserverStock(), la vérification et la mise à jour du stock ne sont pas atomiques. Entre la lecture du stock disponible et sa mise à jour, une autre transaction pourrait modifier la valeur du stock.
2. Verrous non explicites:
Le code actuel repose uniquement sur les mécanismes d'isolation de transaction par défaut sans verrouillage explicite, ce qui peut conduire à des incohérences en cas d'accès concurrents.
3. Réservations partielles incohérentes:
Si la transaction échoue après avoir réservé du stock dans certaines villes mais pas dans d'autres, la méthode annulerReservationStock() doit être appelée correctement pour restaurer la cohérence.
4. Absence de gestion de version des données:
Le système ne vérifie pas si les données lues sont toujours valides au moment de l'écriture (problème de concurrence optimiste).
5. Pas de détection des interblocages:
Le code ne gère pas explicitement les situations d'interblocage (deadlocks) potentielles lorsque plusieurs transactions attendent mutuellement des ressources.

### b. Solution proposée pour éviter les conflits
Je propose une combinaison d'approches:

```java

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
* Mise à jour du schéma pour supporter la gestion de concurrence
  */
  @Transactional
  public void updateDatabaseSchema() {
  for (String ville : VILLES) {
  JdbcTemplate db = dataSourceRouter.getDataSourceByCity(ville);

       // Ajouter une colonne de version pour la gestion optimiste
       db.update("ALTER TABLE produit_stock ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0");
       
       // Créer une table de journalisation pour les réservations
       db.update("CREATE TABLE IF NOT EXISTS log_reservation_stock (" +
                "id SERIAL PRIMARY KEY, " +
                "id_produit INTEGER NOT NULL, " +
                "quantite INTEGER NOT NULL, " +
                "ville VARCHAR(50) NOT NULL, " +
                "operation VARCHAR(20) NOT NULL, " + 
                "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
  }
  }
```

#### Explication de la solution:

1. Verrouillage pessimiste avec FOR UPDATE:

Utilisation de la clause FOR UPDATE pour verrouiller les lignes de stock lors de la lecture
Empêche d'autres transactions de modifier ces lignes jusqu'à la fin de la transaction actuelle


2. Gestion optimiste avec version:

Ajout d'une colonne version incrémentée à chaque mise à jour
Vérification que la version n'a pas changé depuis la lecture avant l'écriture
Permet de détecter les modifications concurrentes et de réessayer l'opération


3. Journalisation des opérations:

Création d'une table log_reservation_stock pour suivre toutes les opérations
Utile pour l'audit, le débogage et la récupération en cas de problème


4. Transactions atomiques: 

Utilisation des transactions pour garantir que les opérations sont soit toutes exécutées, soit toutes annulées

### C. Simulation de la concurrence et vérification de la sérialisabilité
#### Explication de la simulation:

1. Initialisation du test:

Mise en place d'un stock initial connu pour chaque ville
Création d'un pool de threads pour simuler des clients concurrents


2. Transactions concurrentes:

Plusieurs clients tentent d'acheter simultanément le même produit
Chaque client crée une commande pour une quantité fixe du produit


3. Vérification de la sérialisabilité:

Test de cohérence: stock initial = stock restant + quantités commandées
Analyse des journaux d'opérations pour voir l'ordre des transactions
Vérification des annulations pour détecter les conflits résolus


4. Conditions de réussite:

Les données sont cohérentes après toutes les transactions
Les transactions s'exécutent comme si elles étaient séquentielles
Les conflits sont correctement détectés et résolus
## Partie 5: Tolérance aux Pannes et Réplication

