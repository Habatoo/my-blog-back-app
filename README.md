# my-blog-back-app

`mvn clean compile`
`mvn package`

-- 1. Выгнать все активные сессии из blog_db
DO
$$
DECLARE
r RECORD;
BEGIN
FOR r IN (SELECT pid FROM pg_stat_activity WHERE datname = 'blog_db' AND pid <> pg_backend_pid())
LOOP
EXECUTE 'SELECT pg_terminate_backend(' || r.pid || ');';
END LOOP;
END
$$;

-- 2. Удалить базу (если существует)
DROP DATABASE IF EXISTS blog_db;

-- 3. Удалить пользователя (роль), если нет других зависимостей
DROP ROLE IF EXISTS blog_admin;

-- 4. (дополнительно) Если существует flyway-таблица в других базах - удалить вручную
-- (например, если flyway_schema_history есть в другой схеме или базе)
-- Подключиться к нужной базе и выполнить:
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- 5. (опционально) Полная очистка публичной схемы — если надо не удалять базу:
-- Подключиться к базе: \c blog_db
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- 6. Если нужно пересоздать пользователя и базу:
CREATE ROLE blog_admin WITH LOGIN PASSWORD 'blog_password';
CREATE DATABASE blog_db OWNER blog_admin;
GRANT ALL PRIVILEGES ON DATABASE blog_db TO blog_admin;
