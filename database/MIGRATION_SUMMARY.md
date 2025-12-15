# iWallet Database Migration - Summary Document

## Version 1.0 - 2025-12-15

---

## 1. DIAGRAMME ER CORRIGE (Description Textuelle)

```
+-------------------+       1:N        +-------------------+
|      users        |----------------->|     accounts      |
+-------------------+                  +-------------------+
| PK: user_id       |                  | PK: account_id    |
|    email (UNIQUE) |                  | FK: user_id       |
|    password       |                  |    type           |
|    first_name     |                  |    balance        |
|    last_name      |                  |    name           |
|    version        |                  |    version        |
+-------------------+                  +-------------------+
         |                                      |
         | 1:N                                  | 1:N
         v                                      v
+-------------------+                  +-------------------+
|    portfolios     |                  |   transactions    |
+-------------------+                  +-------------------+
| PK: portfolio_id  |                  | PK: transaction_id|
| FK: user_id       |                  | FK: account_id    |
|    name           |                  |    type           |
|    version        |                  |    amount         |
|    created_at     |                  |    timestamp      |
|    updated_at     |                  |    description    |
+-------------------+                  |    linked_txn_id  |
         |                             |    version        |
         | 1:N                         +-------------------+
         v
+-------------------+       1:N        +------------------------+
|      assets       |<-----------------|  portfolio_transactions |
+-------------------+                  +------------------------+
| PK: asset_id      |                  | PK: id                 |
| FK: portfolio_id  |                  | FK: portfolio_id       |
|    symbol         |                  |    symbol              |
|    type           |                  |    asset_name          |
|    asset_name     |                  |    type                |
|    unit_value     |                  |    quantity            |
|    quantity       |                  |    unit_price          |
|    version        |                  |    total_value         |
|    created_at     |                  |    timestamp           |
|    updated_at     |                  |    version             |
+-------------------+                  |    created_at          |
                                       |    updated_at          |
                                       +------------------------+

+-------------------+       1:1        +-------------------+
|      users        |<-----------------|  wealth_trackers  |
+-------------------+                  +-------------------+
                                       | PK: id            |
                                       | FK: user_id       |
                                       |    total_wealth   |
                                       |    total_cash     |
                                       |    total_crypto   |
                                       |    total_stocks   |
                                       |    growth_rate    |
                                       |    version        |
                                       +-------------------+
                                                |
                                                | 1:N
                                                v
                                       +-------------------+
                                       |  wealth_history   |
                                       +-------------------+
                                       | FK: tracker_id    |
                                       |    value          |
                                       +-------------------+
```

### Relations du Schema:

| Relation | Table Source | Table Cible | Cardinalite | ON DELETE |
|----------|-------------|-------------|-------------|-----------|
| fk_accounts_user_id | accounts | users | N:1 | CASCADE |
| fk_portfolios_user_id | portfolios | users | N:1 | CASCADE |
| fk_assets_portfolio_id | assets | portfolios | N:1 | CASCADE |
| fk_portfolio_transactions_portfolio_id | portfolio_transactions | portfolios | N:1 | CASCADE |
| fk_transactions_account_id | transactions | accounts | N:1 | CASCADE |
| fk_wealth_trackers_user_id | wealth_trackers | users | 1:1 | CASCADE |
| fk_wealth_history_tracker_id | wealth_history | wealth_trackers | N:1 | CASCADE |

---

## 2. CHANGEMENTS EFFECTUES

### 2.1 Table Supprimee

| Table | Raison |
|-------|--------|
| `recurring_transactions` | Fonctionnalite supprimee du projet |

### 2.2 Contraintes FK Ajoutees

| Contrainte | Table | Colonne | Reference | Raison |
|------------|-------|---------|-----------|--------|
| `fk_portfolio_transactions_portfolio_id` | portfolio_transactions | portfolio_id | portfolios(portfolio_id) | **FK CRITIQUE MANQUANTE** - La table avait la colonne mais pas la contrainte |
| `fk_accounts_user_id` | accounts | user_id | users(user_id) | Assurer l'integrite referentielle |
| `fk_portfolios_user_id` | portfolios | user_id | users(user_id) | Assurer l'integrite referentielle |
| `fk_assets_portfolio_id` | assets | portfolio_id | portfolios(portfolio_id) | Assurer l'integrite referentielle |
| `fk_transactions_account_id` | transactions | account_id | accounts(account_id) | Assurer l'integrite referentielle |
| `fk_wealth_trackers_user_id` | wealth_trackers | user_id | users(user_id) | Assurer l'integrite referentielle |
| `fk_wealth_history_tracker_id` | wealth_history | tracker_id | wealth_trackers(id) | Assurer l'integrite referentielle |

### 2.3 Colonnes d'Audit Ajoutees

| Table | Colonne | Type | Description |
|-------|---------|------|-------------|
| portfolios | created_at | TIMESTAMP | Date de creation, DEFAULT CURRENT_TIMESTAMP |
| portfolios | updated_at | TIMESTAMP | Date de modification, auto-update |
| assets | created_at | TIMESTAMP | Date de creation, DEFAULT CURRENT_TIMESTAMP |
| assets | updated_at | TIMESTAMP | Date de modification, auto-update |
| portfolio_transactions | created_at | TIMESTAMP | Date de creation, DEFAULT CURRENT_TIMESTAMP |
| portfolio_transactions | updated_at | TIMESTAMP | Date de modification, auto-update |

### 2.4 Colonnes Version (Optimistic Locking)

Toutes les tables principales ont maintenant une colonne `version BIGINT`:
- users
- accounts
- portfolios
- assets
- transactions
- portfolio_transactions
- wealth_trackers

### 2.5 Indexes de Performance

| Index | Table | Colonne(s) | Justification |
|-------|-------|------------|---------------|
| idx_accounts_user_id | accounts | user_id | Performance des JOINs user-accounts |
| idx_portfolios_user_id | portfolios | user_id | Performance des JOINs user-portfolios |
| idx_assets_portfolio_id | assets | portfolio_id | Performance des JOINs portfolio-assets |
| idx_portfolio_transactions_portfolio_id | portfolio_transactions | portfolio_id | **CRITIQUE** - Performance des queries historiques |
| idx_transactions_account_id | transactions | account_id | Performance des JOINs account-transactions |
| idx_wealth_trackers_user_id | wealth_trackers | user_id | Performance du suivi de richesse |
| idx_wealth_history_tracker_id | wealth_history | tracker_id | Performance de l'historique |
| idx_portfolio_transactions_symbol | portfolio_transactions | symbol | Queries par symbole d'actif |
| idx_users_email | users | email | Performance des requetes de login |

---

## 3. VALIDATION DE COHERENCE

### 3.1 Precision des Montants Financiers

| Table | Colonne | Precision | Conforme |
|-------|---------|-----------|----------|
| accounts | balance | DECIMAL(19,4) | OUI |
| assets | unit_value | DECIMAL(19,8) | OUI (precision crypto) |
| assets | quantity | DECIMAL(19,8) | OUI (precision crypto) |
| transactions | amount | DECIMAL(19,4) | OUI |
| portfolio_transactions | quantity | DECIMAL(19,8) | OUI |
| portfolio_transactions | unit_price | DECIMAL(19,4) | OUI |
| portfolio_transactions | total_value | DECIMAL(19,4) | OUI |
| wealth_trackers | total_wealth_usd | DECIMAL(19,4) | OUI |
| wealth_trackers | total_cash | DECIMAL(19,4) | OUI |
| wealth_trackers | total_crypto | DECIMAL(19,4) | OUI |
| wealth_trackers | total_stocks | DECIMAL(19,4) | OUI |
| wealth_trackers | growth_rate | DECIMAL(10,4) | OUI |

### 3.2 Compatibilite JPA/Hibernate

| Aspect | Status | Notes |
|--------|--------|-------|
| @Version columns | OK | Toutes les entites ont version BIGINT |
| @PrePersist/@PreUpdate | OK | Callbacks d'audit implementes |
| Cascade operations | OK | ON DELETE CASCADE sur toutes les FK |
| Orphan removal | OK | Configure dans les entites JPA |
| Fetch strategy | OK | LAZY fetching pour les collections |

---

## 4. FICHIERS MODIFIES

### 4.1 Script SQL
- `/database/migration_v1.sql` - Script de migration complet et idempotent

### 4.2 Entites JPA Modifiees
- `Portfolio.java` - Ajout created_at, updated_at, @PrePersist, @PreUpdate
- `Asset.java` - Ajout created_at, updated_at, @PrePersist, @PreUpdate
- `PortfolioTransaction.java` - Ajout created_at, updated_at, extension du @PrePersist, ajout @PreUpdate

---

## 5. INSTRUCTIONS D'EXECUTION

### 5.1 Prerequis
1. MySQL 8.0 en cours d'execution
2. Base de donnees `ewallet_db` existante
3. Acces root ou privileges ALTER, CREATE, DROP

### 5.2 Execution du Script

```bash
# Option 1: Via Docker
docker exec -i ewallet-db mysql -uroot -proot ewallet_db < database/migration_v1.sql

# Option 2: Via MySQL Client
mysql -h localhost -P 3306 -uroot -proot ewallet_db < database/migration_v1.sql

# Option 3: Via MySQL Workbench
# Ouvrir migration_v1.sql et executer
```

### 5.3 Verification Post-Migration

Le script inclut des requetes de verification a la fin. Verifiez:
1. Toutes les FK sont listees
2. Tous les indexes sont crees
3. Les colonnes version existent
4. Les colonnes audit existent
5. recurring_transactions n'existe plus

---

## 6. ROLLBACK (si necessaire)

```sql
-- Supprimer les FK ajoutees
ALTER TABLE accounts DROP FOREIGN KEY fk_accounts_user_id;
ALTER TABLE portfolios DROP FOREIGN KEY fk_portfolios_user_id;
ALTER TABLE assets DROP FOREIGN KEY fk_assets_portfolio_id;
ALTER TABLE portfolio_transactions DROP FOREIGN KEY fk_portfolio_transactions_portfolio_id;
ALTER TABLE transactions DROP FOREIGN KEY fk_transactions_account_id;
ALTER TABLE wealth_trackers DROP FOREIGN KEY fk_wealth_trackers_user_id;

-- Supprimer les colonnes audit
ALTER TABLE portfolios DROP COLUMN created_at, DROP COLUMN updated_at;
ALTER TABLE assets DROP COLUMN created_at, DROP COLUMN updated_at;
ALTER TABLE portfolio_transactions DROP COLUMN created_at, DROP COLUMN updated_at;

-- Note: Les colonnes version sont gerees par Hibernate, ne pas supprimer
```

---

## 7. NOTES IMPORTANTES

- **Script Idempotent**: Le script peut etre execute plusieurs fois sans erreur
- **Preservation des Donnees**: Aucune donnee existante n'est supprimee
- **Hibernate Sync**: Avec `hibernate.hbm2ddl.auto=update`, Hibernate synchronisera automatiquement
- **Performance**: Les indexes ameliorent significativement les JOINs sur grandes tables

---

*Document genere le 2025-12-15*
*Version: 1.0*
*Auteur: Database Architecture Team*
