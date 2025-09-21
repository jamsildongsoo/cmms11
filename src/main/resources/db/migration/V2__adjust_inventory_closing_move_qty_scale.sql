-- Ensure move_qty values align to two decimal places before altering column scale
UPDATE inventory_closing
SET move_qty = ROUND(move_qty, 2)
WHERE move_qty IS NOT NULL AND move_qty <> ROUND(move_qty, 2);

ALTER TABLE inventory_closing
    MODIFY move_qty DECIMAL(18,2);
