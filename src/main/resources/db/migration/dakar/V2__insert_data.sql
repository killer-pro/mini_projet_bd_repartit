-- Insertion de clients à Dakar
INSERT INTO client (nom, ville, email) VALUES
                                           ('Amadou Diop', 'Dakar', 'amadou.diop@example.com'),
                                           ('Fatou Ndiaye', 'Dakar', 'fatou.ndiaye@example.com'),
                                           ('Ousmane Sow', 'Dakar', 'ousmane.sow@example.com'),
                                           ('Mariama Bâ', 'Dakar', 'mariama.ba@example.com'),
                                           ('Ibrahima Fall', 'Dakar', 'ibrahima.fall@example.com'),
                                           ('Aïssatou Diallo', 'Dakar', 'aissatou.diallo@example.com'),
                                           ('Mamadou Mbaye', 'Dakar', 'mamadou.mbaye@example.com'),
                                           ('Sokhna Gueye', 'Dakar', 'sokhna.gueye@example.com'),
                                           ('Abdoulaye Sarr', 'Dakar', 'abdoulaye.sarr@example.com'),
                                           ('Ramatoulaye Seck', 'Dakar', 'ramatoulaye.seck@example.com');

-- Insertion de produits (informations générales)
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

-- Insertion de stock à Dakar
INSERT INTO produit_stock (id_produit, stock, entrepot) VALUES
                                                            (1, 150, 'Dakar-Centre'),
                                                            (2, 75, 'Dakar-Centre'),
                                                            (3, 45, 'Dakar-Ouest'),
                                                            (4, 60, 'Dakar-Centre'),
                                                            (5, 30, 'Dakar-Ouest'),
                                                            (6, 25, 'Dakar-Centre'),
                                                            (7, 80, 'Dakar-Ouest'),
                                                            (8, 120, 'Dakar-Centre'),
                                                            (9, 40, 'Dakar-Ouest'),
                                                            (10, 95, 'Dakar-Centre');

-- Insertion de commandes
INSERT INTO commande (id_client, date_commande, statut, montant_total) VALUES
                                                                           (1, '2025-02-10 14:30:00', 'Livrée', 499000.00),
                                                                           (2, '2025-02-15 10:15:00', 'Livrée', 580000.00),
                                                                           (3, '2025-02-20 16:45:00', 'Expédiée', 125000.00),
                                                                           (4, '2025-02-25 09:20:00', 'En préparation', 430000.00),
                                                                           (5, '2025-03-01 13:10:00', 'En préparation', 65000.00),
                                                                           (6, '2025-03-05 11:30:00', 'Nouvelle', 155000.00),
                                                                           (7, '2025-03-08 15:45:00', 'Nouvelle', 499000.00),
                                                                           (1, '2025-03-10 10:00:00', 'Nouvelle', 140000.00);

-- Insertion de détails de commande
INSERT INTO commande_detail (id_commande, id_produit, quantite, prix_unitaire) VALUES
                                                                                   (1, 1, 1, 499000.00),
                                                                                   (2, 4, 1, 580000.00),
                                                                                   (3, 3, 1, 125000.00),
                                                                                   (4, 6, 1, 430000.00),
                                                                                   (5, 8, 1, 65000.00),
                                                                                   (6, 9, 1, 155000.00),
                                                                                   (7, 1, 1, 499000.00),
                                                                                   (8, 7, 1, 95000.00),
                                                                                   (8, 8, 1, 45000.00);

-- Insertion de transactions distribuées
INSERT INTO transaction_distribuee (id, statut, timestamp_debut, timestamp_fin, commentaire) VALUES
                                                                                                 ('123e4567-e89b-12d3-a456-426614174000', 'TERMINÉE', '2025-02-10 14:30:00', '2025-02-10 14:30:05', 'Commande client Amadou Diop'),
                                                                                                 ('223e4567-e89b-12d3-a456-426614174001', 'TERMINÉE', '2025-02-15 10:15:00', '2025-02-15 10:15:07', 'Commande client Fatou Ndiaye'),
                                                                                                 ('323e4567-e89b-12d3-a456-426614174002', 'TERMINÉE', '2025-02-20 16:45:00', '2025-02-20 16:45:12', 'Commande client Ousmane Sow'),
                                                                                                 ('423e4567-e89b-12d3-a456-426614174003', 'TERMINÉE', '2025-02-25 09:20:00', '2025-02-25 09:20:09', 'Commande client Mariama Bâ'),
                                                                                                 ('523e4567-e89b-12d3-a456-426614174004', 'TERMINÉE', '2025-03-01 13:10:00', '2025-03-01 13:10:06', 'Commande client Ibrahima Fall');