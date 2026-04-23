-- Create databases
CREATE DATABASE IF NOT EXISTS user_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ✅ Create user FIRST
CREATE USER IF NOT EXISTS 'ecommerce'@'%' IDENTIFIED BY 'akhil';

-- ✅ Then grant privileges
GRANT ALL PRIVILEGES ON user_db.*    TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON order_db.*   TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'ecommerce'@'%';

FLUSH PRIVILEGES;