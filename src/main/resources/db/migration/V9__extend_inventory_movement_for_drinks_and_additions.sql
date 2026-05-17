-- Permite que inventory_movement registre bebidas y adiciones además de productos.
-- product_id pasa a nullable; se agregan item_category, item_id, item_name.

ALTER TABLE inventory_movement
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE inventory_movement
    ADD COLUMN item_category VARCHAR(20) NOT NULL DEFAULT 'PRODUCT',
    ADD COLUMN item_id       VARCHAR(255),
    ADD COLUMN item_name     VARCHAR(255);

-- Rellena item_name para los registros existentes (productos)
UPDATE inventory_movement im
SET item_name = (SELECT p.name FROM product p WHERE p.id = im.product_id)
WHERE im.product_id IS NOT NULL;
