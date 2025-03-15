-- Création des tables pour la base de données de Dakar

-- Fragmentation horizontale des clients (uniquement les clients de Dakar)
CREATE TABLE IF NOT EXISTS client (
                                      id SERIAL PRIMARY KEY,
                                      nom VARCHAR(100) NOT NULL,
                                      ville VARCHAR(50) NOT NULL,
                                      email VARCHAR(100) UNIQUE NOT NULL,
                                      CONSTRAINT check_ville CHECK (ville = 'Dakar')
);

-- Fragmentation verticale des produits (informations générales)
CREATE TABLE IF NOT EXISTS produit_info (
                                            id SERIAL PRIMARY KEY,
                                            nom VARCHAR(100) NOT NULL,
                                            description TEXT,
                                            prix DECIMAL(12, 2) NOT NULL
);

-- Informations de stock spécifiques à Dakar
CREATE TABLE IF NOT EXISTS produit_stock (
                                             id_produit INTEGER PRIMARY KEY REFERENCES produit_info(id),
                                             stock INTEGER NOT NULL DEFAULT 0,
                                             entrepot VARCHAR(50) NOT NULL DEFAULT 'Dakar'
);

-- Table des commandes
CREATE TABLE IF NOT EXISTS commande (
                                        id SERIAL PRIMARY KEY,
                                        id_client INTEGER NOT NULL REFERENCES client(id),
                                        date_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        statut VARCHAR(20) NOT NULL DEFAULT 'Nouvelle',
                                        montant_total DECIMAL(12, 2) NOT NULL
);

-- Détails des commandes
CREATE TABLE IF NOT EXISTS commande_detail (
                                               id SERIAL PRIMARY KEY,
                                               id_commande INTEGER NOT NULL REFERENCES commande(id),
                                               id_produit INTEGER NOT NULL REFERENCES produit_info(id),
                                               quantite INTEGER NOT NULL,
                                               prix_unitaire DECIMAL(12, 2) NOT NULL
);

-- Table de gestion des transactions distribuées
CREATE TABLE IF NOT EXISTS transaction_distribuee (
                                                      id UUID PRIMARY KEY,
                                                      statut VARCHAR(20) NOT NULL,
                                                      timestamp_debut TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                      timestamp_fin TIMESTAMP,
                                                      commentaire TEXT
);