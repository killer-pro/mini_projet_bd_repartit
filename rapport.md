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

