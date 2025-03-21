#!/bin/bash
# Arrêter le serveur PostgreSQL
pg_ctl -D /var/lib/postgresql/data stop -m fast

# Supprimer les données existantes
rm -rf /var/lib/postgresql/data/*

# Initialiser le réplica avec les données du maître
pg_basebackup -h db-dakar -p 5432 -U replicator -D /var/lib/postgresql/data -Fp -Xs -R -P -v

# Créer le fichier de signal standby
touch /var/lib/postgresql/data/standby.signal

# Ajouter la configuration de réplication
cat > /var/lib/postgresql/data/postgresql.auto.conf << EOF
primary_conninfo = 'host=db-dakar port=5432 user=replicator password=replication_password application_name=dakar_replica'
primary_slot_name = 'dakar_replica_slot'
EOF

# Démarrer le serveur PostgreSQL en mode standby
pg_ctl -D /var/lib/postgresql/data start