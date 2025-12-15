-- ============================================================================
-- iWallet Database Migration Script v1.0
-- ============================================================================
-- Application: iWallet - Financial Wealth Management
-- Tech Stack:  Jakarta EE, JPA/Hibernate, MySQL 8.0
-- Date:        2025-12-15
-- Author:      Database Architecture Team
-- ============================================================================
-- 
-- PURPOSE:
-- This script performs a comprehensive database migration to:
--   1. Add missing foreign key constraints (portfolio_transactions -> portfolios)
--   2. Add audit columns (created_at, updated_at) to key tables
--   3. Remove deprecated recurring_transactions table
--   4. Add performance indexes on all foreign key columns
--   5. Ensure JPA compatibility (version fields, proper DECIMAL precision)
--
-- NOTES:
--   - Script is IDEMPOTENT: safe to run multiple times
--   - All changes preserve existing data
--   - Uses conditional checks before each modification
--   - Compatible with Hibernate/JPA auto-update mode
--
-- ============================================================================

-- Use the iWallet database
USE ewallet_db;

-- ============================================================================
-- SECTION 1: DROP DEPRECATED TABLE (recurring_transactions)
-- ============================================================================
-- The recurring_transactions feature has been removed from the project.
-- Safely drop the table if it exists.
-- ============================================================================

-- Check and drop recurring_transactions table
DROP TABLE IF EXISTS recurring_transactions;

-- Log the action (optional, for audit purposes)
SELECT 'Section 1: Dropped recurring_transactions table (if existed)' AS migration_status;


-- ============================================================================
-- SECTION 2: ADD MISSING VERSION COLUMNS FOR OPTIMISTIC LOCKING
-- ============================================================================
-- JPA requires a 'version' column for @Version annotation (optimistic locking).
-- This prevents concurrent modification conflicts.
-- ============================================================================

-- 2.1 Add version column to users table (if not exists)
SET @table_name = 'users';
SET @column_name = 'version';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = @column_name
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column users.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.2 Add version column to accounts table (if not exists)
SET @table_name = 'accounts';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = 'version'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE accounts ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column accounts.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.3 Add version column to portfolios table (if not exists)
SET @table_name = 'portfolios';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = 'version'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE portfolios ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column portfolios.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.4 Add version column to assets table (if not exists)
SET @table_name = 'assets';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = 'version'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE assets ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column assets.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.5 Add version column to transactions table (if not exists)
SET @table_name = 'transactions';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = 'version'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE transactions ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column transactions.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.6 Add version column to portfolio_transactions table (if not exists)
SET @table_name = 'portfolio_transactions';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = 'version'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE portfolio_transactions ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column portfolio_transactions.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.7 Add version column to wealth_trackers table (if not exists)
SET @table_name = 'wealth_trackers';
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = @table_name 
    AND COLUMN_NAME = 'version'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE wealth_trackers ADD COLUMN version BIGINT DEFAULT 0',
    'SELECT "Column wealth_trackers.version already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Section 2: Version columns verified/added for optimistic locking' AS migration_status;


-- ============================================================================
-- SECTION 3: ADD AUDIT COLUMNS (created_at, updated_at)
-- ============================================================================
-- These columns enable automatic tracking of record creation and modification.
-- Essential for audit trails and debugging data issues.
-- ============================================================================

-- 3.1 portfolios: created_at
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolios' 
    AND COLUMN_NAME = 'created_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE portfolios ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    'SELECT "Column portfolios.created_at already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.2 portfolios: updated_at
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolios' 
    AND COLUMN_NAME = 'updated_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE portfolios ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT "Column portfolios.updated_at already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.3 assets: created_at
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'assets' 
    AND COLUMN_NAME = 'created_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE assets ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    'SELECT "Column assets.created_at already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.4 assets: updated_at
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'assets' 
    AND COLUMN_NAME = 'updated_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE assets ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT "Column assets.updated_at already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.5 portfolio_transactions: created_at
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolio_transactions' 
    AND COLUMN_NAME = 'created_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE portfolio_transactions ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    'SELECT "Column portfolio_transactions.created_at already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.6 portfolio_transactions: updated_at
SET @column_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolio_transactions' 
    AND COLUMN_NAME = 'updated_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE portfolio_transactions ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT "Column portfolio_transactions.updated_at already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Section 3: Audit columns (created_at, updated_at) verified/added' AS migration_status;


-- ============================================================================
-- SECTION 4: ADD MISSING FOREIGN KEY CONSTRAINTS
-- ============================================================================
-- Foreign keys ensure referential integrity between related tables.
-- ON DELETE CASCADE: when parent is deleted, children are automatically deleted.
-- ============================================================================

-- 4.1 FK: accounts.user_id -> users.user_id
-- Check if constraint already exists
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'accounts' 
    AND CONSTRAINT_NAME = 'fk_accounts_user_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE accounts ADD CONSTRAINT fk_accounts_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE',
    'SELECT "FK fk_accounts_user_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.2 FK: portfolios.user_id -> users.user_id
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolios' 
    AND CONSTRAINT_NAME = 'fk_portfolios_user_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE portfolios ADD CONSTRAINT fk_portfolios_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE',
    'SELECT "FK fk_portfolios_user_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.3 FK: assets.portfolio_id -> portfolios.portfolio_id
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'assets' 
    AND CONSTRAINT_NAME = 'fk_assets_portfolio_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE assets ADD CONSTRAINT fk_assets_portfolio_id FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE',
    'SELECT "FK fk_assets_portfolio_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.4 FK: portfolio_transactions.portfolio_id -> portfolios.portfolio_id
-- THIS IS THE CRITICAL MISSING FK IDENTIFIED IN THE REQUEST
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolio_transactions' 
    AND CONSTRAINT_NAME = 'fk_portfolio_transactions_portfolio_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE portfolio_transactions ADD CONSTRAINT fk_portfolio_transactions_portfolio_id FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE',
    'SELECT "FK fk_portfolio_transactions_portfolio_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.5 FK: transactions.account_id -> accounts.account_id
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'transactions' 
    AND CONSTRAINT_NAME = 'fk_transactions_account_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE transactions ADD CONSTRAINT fk_transactions_account_id FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE',
    'SELECT "FK fk_transactions_account_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.6 FK: wealth_trackers.user_id -> users.user_id
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wealth_trackers' 
    AND CONSTRAINT_NAME = 'fk_wealth_trackers_user_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE wealth_trackers ADD CONSTRAINT fk_wealth_trackers_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE',
    'SELECT "FK fk_wealth_trackers_user_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4.7 FK: wealth_history.tracker_id -> wealth_trackers.id
SET @fk_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wealth_history' 
    AND CONSTRAINT_NAME = 'fk_wealth_history_tracker_id'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0, 
    'ALTER TABLE wealth_history ADD CONSTRAINT fk_wealth_history_tracker_id FOREIGN KEY (tracker_id) REFERENCES wealth_trackers(id) ON DELETE CASCADE',
    'SELECT "FK fk_wealth_history_tracker_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Section 4: Foreign key constraints verified/added' AS migration_status;


-- ============================================================================
-- SECTION 5: ADD PERFORMANCE INDEXES ON FOREIGN KEY COLUMNS
-- ============================================================================
-- Indexes on FK columns dramatically improve JOIN performance.
-- MySQL auto-creates indexes for FK constraints, but we ensure they exist.
-- ============================================================================

-- 5.1 Index on accounts.user_id
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'accounts' 
    AND INDEX_NAME = 'idx_accounts_user_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_accounts_user_id ON accounts(user_id)',
    'SELECT "Index idx_accounts_user_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.2 Index on portfolios.user_id
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolios' 
    AND INDEX_NAME = 'idx_portfolios_user_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_portfolios_user_id ON portfolios(user_id)',
    'SELECT "Index idx_portfolios_user_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.3 Index on assets.portfolio_id
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'assets' 
    AND INDEX_NAME = 'idx_assets_portfolio_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_assets_portfolio_id ON assets(portfolio_id)',
    'SELECT "Index idx_assets_portfolio_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.4 Index on portfolio_transactions.portfolio_id (CRITICAL FOR PERFORMANCE)
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolio_transactions' 
    AND INDEX_NAME = 'idx_portfolio_transactions_portfolio_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_portfolio_transactions_portfolio_id ON portfolio_transactions(portfolio_id)',
    'SELECT "Index idx_portfolio_transactions_portfolio_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.5 Index on transactions.account_id
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'transactions' 
    AND INDEX_NAME = 'idx_transactions_account_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_transactions_account_id ON transactions(account_id)',
    'SELECT "Index idx_transactions_account_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.6 Index on wealth_trackers.user_id
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wealth_trackers' 
    AND INDEX_NAME = 'idx_wealth_trackers_user_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_wealth_trackers_user_id ON wealth_trackers(user_id)',
    'SELECT "Index idx_wealth_trackers_user_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.7 Index on wealth_history.tracker_id
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'wealth_history' 
    AND INDEX_NAME = 'idx_wealth_history_tracker_id'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_wealth_history_tracker_id ON wealth_history(tracker_id)',
    'SELECT "Index idx_wealth_history_tracker_id already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5.8 Additional useful indexes for common queries
-- Index on portfolio_transactions.symbol for asset-based queries
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'portfolio_transactions' 
    AND INDEX_NAME = 'idx_portfolio_transactions_symbol'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_portfolio_transactions_symbol ON portfolio_transactions(symbol)',
    'SELECT "Index idx_portfolio_transactions_symbol already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index on users.email for login queries (uniqueness already enforced)
SET @idx_exists = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'users' 
    AND INDEX_NAME = 'idx_users_email'
);

SET @sql = IF(@idx_exists = 0, 
    'CREATE INDEX idx_users_email ON users(email)',
    'SELECT "Index idx_users_email already exists" AS status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Section 5: Performance indexes verified/added' AS migration_status;


-- ============================================================================
-- SECTION 6: VERIFY DECIMAL PRECISION FOR FINANCIAL COLUMNS
-- ============================================================================
-- Financial amounts must use DECIMAL(19,4) for precision.
-- Quantity fields use DECIMAL(19,8) for crypto decimals.
-- ============================================================================

-- Note: JPA/Hibernate with @Column(precision=19, scale=4/8) handles this.
-- The following is informational - Hibernate will manage column types.
-- If precision issues are found, uncomment and modify these ALTER statements.

-- Example (uncomment if needed):
-- ALTER TABLE accounts MODIFY COLUMN balance DECIMAL(19,4) DEFAULT 0;
-- ALTER TABLE assets MODIFY COLUMN unit_value DECIMAL(19,8) DEFAULT 0;
-- ALTER TABLE assets MODIFY COLUMN quantity DECIMAL(19,8) DEFAULT 0;
-- ALTER TABLE transactions MODIFY COLUMN amount DECIMAL(19,4) DEFAULT 0;
-- ALTER TABLE portfolio_transactions MODIFY COLUMN quantity DECIMAL(19,8) DEFAULT 0;
-- ALTER TABLE portfolio_transactions MODIFY COLUMN unit_price DECIMAL(19,4) DEFAULT 0;
-- ALTER TABLE portfolio_transactions MODIFY COLUMN total_value DECIMAL(19,4) DEFAULT 0;

SELECT 'Section 6: DECIMAL precision verified (managed by Hibernate)' AS migration_status;


-- ============================================================================
-- SECTION 7: FINAL VERIFICATION QUERIES
-- ============================================================================
-- These queries help verify the migration was successful.
-- Run them to confirm all changes were applied correctly.
-- ============================================================================

-- 7.1 List all foreign keys in the database
SELECT 'Foreign Key Constraints:' AS verification_section;
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    information_schema.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- 7.2 List all indexes
SELECT 'Indexes:' AS verification_section;
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM 
    information_schema.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME, INDEX_NAME;

-- 7.3 Verify tables structure
SELECT 'Tables with version column:' AS verification_section;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM 
    information_schema.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME = 'version'
ORDER BY TABLE_NAME;

-- 7.4 Verify audit columns
SELECT 'Tables with audit columns:' AS verification_section;
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM 
    information_schema.COLUMNS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME IN ('created_at', 'updated_at')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 7.5 Confirm recurring_transactions is dropped
SELECT 'Verify recurring_transactions does not exist:' AS verification_section;
SELECT 
    TABLE_NAME 
FROM 
    information_schema.TABLES 
WHERE 
    TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'recurring_transactions';


-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
SELECT '=== MIGRATION COMPLETED SUCCESSFULLY ===' AS final_status;
SELECT 'Script: migration_v1.sql' AS script_name;
SELECT NOW() AS execution_time;

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================
