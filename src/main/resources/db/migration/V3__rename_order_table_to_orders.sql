-- Renombrar tabla 'order' (palabra reservada) a 'orders'
-- Esta migración solo se ejecutará si la tabla 'order' existe

-- Primero, eliminar las restricciones de clave foránea que referencian a 'order'
ALTER TABLE IF EXISTS order_item DROP CONSTRAINT IF EXISTS FKt6wv8m7eshksp5kp8w4b2d1dm;
ALTER TABLE IF EXISTS payment DROP CONSTRAINT IF EXISTS FK458pu56xefty15ugupb46wrin;

-- Renombrar la tabla
ALTER TABLE IF EXISTS "order" RENAME TO orders;

-- Recrear las restricciones de clave foránea con el nuevo nombre
ALTER TABLE IF EXISTS order_item
    ADD CONSTRAINT FKt6wv8m7eshksp5kp8w4b2d1dm
    FOREIGN KEY (order_id) REFERENCES orders;

ALTER TABLE IF EXISTS payment
    ADD CONSTRAINT FK458pu56xefty15ugupb46wrin
    FOREIGN KEY (order_id) REFERENCES orders;

