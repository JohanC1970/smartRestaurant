-- Agrega el precio unitario congelado al momento del pedido.
-- Se inicializa en 0.0 para filas existentes (retrocompatibilidad).
ALTER TABLE order_item ADD COLUMN IF NOT EXISTS unit_price DOUBLE PRECISION NOT NULL DEFAULT 0.0;
