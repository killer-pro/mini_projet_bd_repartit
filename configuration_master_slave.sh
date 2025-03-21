#!/bin/bash

echo "===== Configuration de la réplication pour Dakar ====="

# Démarrer les conteneurs
docker-compose up -d db-dakar

# Attendre que le maître soit prêt
echo "Attente du démarrage du serveur maître..."
sleep 20

# Configurer le maître (créer l'utilisateur de réplication si nécessaire)
echo "Configuration du serveur maître (db-dakar)..."
docker exec -it db-dakar bash -c "
# Vérifier si l'utilisateur replicator existe déjà
psql -U ecommerce -d ecommerce_dakar -tAc \"SELECT 1 FROM pg_roles WHERE rolname='replicator'\" | grep -q 1
if [ \$? -ne 0 ]; then
    echo 'Création du rôle replicator...'
    psql -U ecommerce -d ecommerce_dakar -c \"CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replication_password';\"
else
    echo 'Le rôle replicator existe déjà'
fi

# Vérifier si le slot de réplication existe déjà
psql -U ecommerce -d ecommerce_dakar -tAc \"SELECT 1 FROM pg_replication_slots WHERE slot_name='dakar_replica_slot'\" | grep -q 1
if [ \$? -ne 0 ]; then
    echo 'Création du slot de réplication...'
    psql -U ecommerce -d ecommerce_dakar -c \"SELECT pg_create_physical_replication_slot('dakar_replica_slot');\"
else
    echo 'Le slot de réplication existe déjà'
fi

# Modifier pg_hba.conf pour permettre la connexion du réplica (s'il n'est pas déjà configuré)
if ! grep -q 'replicator' /var/lib/postgresql/data/pg_hba.conf; then
    echo 'Ajout de la règle d\'authentification pour replicator...'
    echo \"host replication replicator 0.0.0.0/0 md5\" >> /var/lib/postgresql/data/pg_hba.conf
    echo \"host all replicator 0.0.0.0/0 md5\" >> /var/lib/postgresql/data/pg_hba.conf

    # Redémarrer PostgreSQL pour appliquer les modifications en tant qu'utilisateur postgres
    echo 'Redémarrage de PostgreSQL...'
    su - postgres -c \"pg_ctl -D /var/lib/postgresql/data reload\"
else
    echo 'La règle d\'authentification pour replicator existe déjà'
fi
"

# Attendre que le maître soit configuré
echo "Attente de la configuration du maître..."
sleep 10

# Démarrer le réplica
echo "Démarrage du serveur réplica..."
docker-compose up -d db-dakar-replica

# Attendre que le réplica soit prêt
echo "Attente du démarrage du serveur réplica..."
sleep 20

# Initialiser la réplication
echo "Initialisation de la réplication..."
docker exec -it db-dakar-replica bash -c "
# Arrêter PostgreSQL en tant qu'utilisateur postgres
su - postgres -c \"pg_ctl -D /var/lib/postgresql/data stop -m fast\"

# Supprimer les données existantes
rm -rf /var/lib/postgresql/data/*

# Initialiser le réplica avec les données du maître
su - postgres -c \"pg_basebackup -h db-dakar -p 5432 -U replicator -W -D /var/lib/postgresql/data -Fp -Xs -P -v\"
# Note: La commande demandera un mot de passe - entrez 'replication_password'

# Créer le fichier de signal standby
touch /var/lib/postgresql/data/standby.signal

# Ajouter la configuration de réplication
cat > /var/lib/postgresql/data/postgresql.auto.conf << EOF
primary_conninfo = 'host=db-dakar port=5432 user=replicator password=replication_password application_name=dakar_replica'
primary_slot_name = 'dakar_replica_slot'
EOF

# S'assurer que les fichiers appartiennent à postgres
chown -R postgres:postgres /var/lib/postgresql/data

# Démarrer PostgreSQL en tant qu'utilisateur postgres
su - postgres -c \"pg_ctl -D /var/lib/postgresql/data start\"
"

echo "===== Configuration de la réplication terminée ====="