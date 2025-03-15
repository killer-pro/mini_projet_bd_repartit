
-- Insertion de clients à saintlouis
INSERT INTO client (nom, ville, email) VALUES
                                           ('Babacar Ndiaye', 'saintlouis', 'babacar.ndiaye@example.com'),
                                           ('Dieynaba Tall', 'saintlouis', 'dieynaba.tall@example.com'),
                                           ('Abdou Kane', 'saintlouis', 'abdou.kane@example.com'),
                                           ('Coumba Diouf', 'saintlouis', 'coumba.diouf@example.com'),
                                           ('Malick Samb', 'saintlouis', 'malick.samb@example.com'),
                                           ('Adja Mbaye', 'saintlouis', 'adja.mbaye@example.com'),
                                           ('Seydou Diarra', 'saintlouis', 'seydou.diarra@example.com'),
                                           ('Arame Gueye', 'saintlouis', 'arame.gueye@example.com');

-- Insertion de produits (les mêmes que Dakar et Thiès pour correspondance)
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

-- Insertion de stock à saintlouis
INSERT INTO produit_stock (id_produit, stock, entrepot) VALUES
                                                            (1, 50, 'saintlouis-Centre'),
                                                            (2, 30, 'saintlouis-Centre'),
                                                            (3, 20, 'saintlouis-Nord'),
                                                            (4, 25, 'saintlouis-Centre'),
                                                            (5, 15, 'saintlouis-Nord'),
                                                            (6, 10, 'saintlouis-Centre'),
                                                            (7, 35, 'saintlouis-Nord'),
                                                            (8, 45, 'saintlouis-Centre'),
                                                            (9, 20, 'saintlouis-Nord'),
                                                            (10, 40, 'saintlouis-Centre');

-- Insertion de commandes
INSERT INTO commande (id_client, date_commande, statut, montant_total) VALUES
                                                                           (1, '2025-02-14 12:40:00', 'Livrée', 430000.00),
                                                                           (2, '2025-02-19 14:25:00', 'Livrée', 125000.00),
                                                                           (3, '2025-02-23 09:15:00', 'Expédiée', 65000.00),
                                                                           (4, '2025-03-02 11:30:00', 'En préparation', 320000.00),
                                                                           (5, '2025-03-06 16:20:00', 'Nouvelle', 45000.00),
                                                                           (6, '2025-03-09 13:45:00', 'Nouvelle', 155000.00);

-- Insertion de détails de commande
INSERT INTO commande_detail (id_commande, id_produit, quantite, prix_unitaire) VALUES
                                                                                   (1, 6, 1, 430000.00),
                                                                                   (2, 3, 1, 125000.00),
                                                                                   (3, 8, 1, 65000.00),
                                                                                   (4, 5, 1, 320000.00),
                                                                                   (5, 10, 1, 45000.00),
                                                                                   (6, 9, 1, 155000.00);

-- Insertion de transactions distribuées
INSERT INTO transaction_distribuee (id, statut, timestamp_debut, timestamp_fin, commentaire) VALUES
                                                                                                 ('a23e4567-e89b-12d3-a456-426614174009', 'TERMINÉE', '2025-02-14 12:40:00', '2025-02-14 12:40:07', 'Commande client Babacar Ndiaye'),
                                                                                                 ('b23e4567-e89b-12d3-a456-426614174010', 'TERMINÉE', '2025-02-19 14:25:00', '2025-02-19 14:25:10', 'Commande client Dieynaba Tall'),
                                                                                                 ('c23e4567-e89b-12d3-a456-426614174011', 'TERMINÉE', '2025-02-23 09:15:00', '2025-02-23 09:15:08', 'Commande client Abdou Kane'),
                                                                                                 ('d23e4567-e89b-12d3-a456-426614174012', 'TERMINÉE', '2025-03-02 11:30:00', '2025-03-02 11:30:12', 'Commande client Coumba Diouf');