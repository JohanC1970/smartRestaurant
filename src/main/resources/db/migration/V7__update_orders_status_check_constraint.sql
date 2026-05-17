-- Actualiza el CHECK constraint de orders.status para incluir el nuevo estado SENT.
-- Flyway ejecuta esto en una transacción, por lo que es seguro en producción.

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

ALTER TABLE orders
    ADD CONSTRAINT orders_status_check
    CHECK (status IN ('PENDING', 'SENT', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED', 'CANCELLED'));
