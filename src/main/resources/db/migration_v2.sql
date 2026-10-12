-- Migración referencial para bases existentes.
-- Ejecutar solo si las columnas no existen.
ALTER TABLE productos ADD COLUMN costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0;
ALTER TABLE pedidos ADD COLUMN telefono_contacto VARCHAR(20) NULL;
ALTER TABLE pedidos ADD COLUMN estado_pago VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE pedidos ADD COLUMN metodo_pago VARCHAR(30) NULL;
ALTER TABLE pedidos ADD COLUMN fecha_pago DATETIME NULL;
ALTER TABLE pedidos ADD COLUMN fecha_entrega_confirmada DATETIME NULL;
UPDATE productos SET costo_unitario = 0 WHERE costo_unitario IS NULL;
UPDATE pedidos SET estado_pago = 'PENDIENTE' WHERE estado_pago IS NULL;
