
-- Insertion de clients à thies
INSERT INTO client (nom, ville, email) VALUES
                                           ('Moussa Diallo', 'thies', 'moussa.diallo@example.com'),
                                           ('Aminata Sall', 'thies', 'aminata.sall@example.com'),
                                           ('Cheikh Diagne', 'thies', 'cheikh.diagne@example.com'),
                                           ('Awa Cissé', 'thies', 'awa.cisse@example.com'),
                                           ('Modou Faye', 'thies', 'modou.faye@example.com'),
                                           ('Khady Ba', 'thies', 'khady.ba@example.com'),
                                           ('Omar Niang', 'thies', 'omar.niang@example.com'),
                                           ('Ndèye Diop', 'thies', 'ndeye.diop@example.com');

-- Insertion de produits (les mêmes que Dakar pour correspondance)
INSERT INTO produit_info (id, nom, description, prix) VALUES
                                                          (1, 'Smartphone Galaxy S23', 'Dernier modèle Samsung avec 256Go', 499000.00),
                                                          (2, 'Ordinateur Portable Lenovo', 'Idéal pour les professionnels', 650000.00),
                                                          (3, 'Imprimante HP LaserJet', 'Imprimante professionnelle noir et blanc', 125000.00),
                                                          (4, 'Télévision LG 55"', 'Smart TV 4K avec technologie OLED', 580000.00),
                                                          (5, 'Climatiseur Samsung', 'Climatiseur split 12000 BTU', 320000.00),
                                                          (6, 'Réfrigérateur Hisense', 'Double porte avec distributeur d''eau', 430000.00),
                                                          (7, 'Four micro-ondes LG', 'Four micro-ondes 30L avec grill', 95000.00),
                                                          (8, 'Enceinte Bluetooth JBL', 'Enceinte portable résistante à l''eau', 65000.00),
                                                          (9, 'Machine à café Nespresso', 'Machine à café automatique avec mousseur à lait', 155000.00),
                                                          (10, 'Ventilateur sur pied', 'Ventilateur télécommandé 5 vitesses', 45000.00);

-- Insertion de stock à thies
INSERT INTO produit_stock (id_produit, stock, entrepot) VALUES
                                                            (1, 75, 'thies-Centre'),
                                                            (2, 40, 'thies-Centre'),
                                                            (3, 25, 'thies-Est'),
                                                            (4, 30, 'thies-Centre'),
                                                            (5, 20, 'thies-Est'),
                                                            (6, 15, 'thies-Centre'),
                                                            (7, 45, 'thies-Est'),
                                                            (8, 60, 'thies-Centre'),
                                                            (9, 25, 'thies-Est'),
                                                            (10, 50, 'thies-Centre');

-- Insertion de commandes
INSERT INTO commande (id_client, date_commande, statut, montant_total) VALUES
                                                                           (1, '2025-02-12 11:20:00', 'Livrée', 650000.00),
                                                                           (2, '2025-02-18 13:45:00', 'Livrée', 95000.00),
                                                                           (3, '2025-02-22 15:30:00', 'Expédiée', 320000.00),
                                                                           (4, '2025-02-28 10:10:00', 'En préparation', 65000.00),
                                                                           (5, '2025-03-03 14:25:00', 'Nouvelle', 499000.00),
                                                                           (6, '2025-03-07 12:50:00', 'Nouvelle', 580000.00);

-- Insertion de détails de commande
INSERT INTO commande_detail (id_commande, id_produit, quantite, prix_unitaire) VALUES
                                                                                   (1, 2, 1, 650000.00),
                                                                                   (2, 7, 1, 95000.00),
                                                                                   (3, 5, 1, 320000.00),
                                                                                   (4, 8, 1, 65000.00),
                                                                                   (5, 1, 1, 499000.00),
                                                                                   (6, 4, 1, 580000.00);

-- Insertion de transactions distribuées
INSERT INTO transaction_distribuee (id, statut, timestamp_debut, timestamp_fin, commentaire) VALUES
                                                                                                 ('623e4567-e89b-12d3-a456-426614174005', 'TERMINÉE', '2025-02-12 11:20:00', '2025-02-12 11:20:08', 'Commande client Moussa Diallo'),
                                                                                                 ('723e4567-e89b-12d3-a456-426614174006', 'TERMINÉE', '2025-02-18 13:45:00', '2025-02-18 13:45:09', 'Commande client Aminata Sall'),
                                                                                                 ('823e4567-e89b-12d3-a456-426614174007', 'TERMINÉE', '2025-02-22 15:30:00', '2025-02-22 15:30:11', 'Commande client Cheikh Diagne'),
                                                                                                 ('923e4567-e89b-12d3-a456-426614174008', 'TERMINÉE', '2025-02-28 10:10:00', '2025-02-28 10:10:07', 'Commande client Awa Cissé');