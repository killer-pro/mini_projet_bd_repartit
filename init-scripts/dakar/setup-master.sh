#!/bin/bash
# Créer un utilisateur de réplication
psql -U ecommerce -d ecommerce_dakar -c "CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replication_password';"

# Modifier pg_hba.conf pour permettre la connexion du réplica
echo "host replication replicator 0.0.0.0/0 scram-sha-256" >> /var/lib/postgresql/data/pg_hba.conf

# Créer un slot de réplication
psql -U ecommerce -d ecommerce_dakar -c "SELECT pg_create_physical_replication_slot('dakar_replica_slot');"

# Redémarrer PostgreSQL pour appliquer les modifications
pg_ctl -D /var/lib/postgresql/data restart