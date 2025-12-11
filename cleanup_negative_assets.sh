#!/bin/bash
# Script de nettoyage des quantités négatives dans la base de données

echo "=== Nettoyage des actifs avec quantités négatives ==="
echo ""
echo "ATTENTION: Ce script va supprimer les assets avec des quantités négatives."
echo "Appuyez sur Ctrl+C pour annuler, ou Entrée pour continuer..."
read

# Connexion à MySQL et exécution des requêtes
docker exec -i ewallet-db mysql -uroot -proot ewallet_db <<EOF

-- 1. Identifier les assets avec quantités négatives
SELECT 
    '=== Assets avec quantités négatives ===' as '';
    
SELECT 
    a.asset_id,
    a.portfolio_id,
    a.asset_name,
    a.symbol,
    a.quantity,
    a.unit_value,
    a.total_value
FROM assets a
WHERE a.quantity < 0
ORDER BY a.quantity ASC;

-- 2. Compter le nombre d'assets négatifs
SELECT 
    COUNT(*) as 'Nombre d''assets négatifs à supprimer' 
FROM assets 
WHERE quantity < 0;

-- 3. Supprimer les assets négatifs
DELETE FROM assets WHERE quantity < 0;

-- 4. Vérification post-nettoyage
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ Aucun asset négatif trouvé - Nettoyage réussi!'
        ELSE CONCAT('❌ ', COUNT(*), ' assets négatifs restants')
    END as 'Résultat'
FROM assets 
WHERE quantity < 0;

EOF

echo ""
echo "=== Nettoyage terminé ==="
