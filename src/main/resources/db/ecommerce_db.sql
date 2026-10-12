-- =============================================================================
-- BASE DE DATOS COMPLETA: TECHSTORE E-COMMERCE
-- Compatible con MySQL 8.0+ / MariaDB 10.4+
-- =============================================================================

-- ATENCION: descomenta la siguiente linea SOLO si quieres borrar toda la BD y empezar de cero
-- DROP DATABASE IF EXISTS ecommerce_db;
CREATE DATABASE IF NOT EXISTS ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_db;

-- -----------------------------------------------------------------------------
-- 1. TABLA: usuarios
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usuarios_email (email),
    INDEX idx_usuarios_rol (rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. TABLA: clientes (Dirección y teléfono OBLIGATORIOS)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    INDEX idx_clientes_telefono (telefono)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. TABLA: categorias
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. TABLA: productos
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    costo_unitario DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock INT NOT NULL DEFAULT 0,
    imagen_url VARCHAR(500),
    categoria_id BIGINT,
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id) ON DELETE SET NULL,
    INDEX idx_productos_categoria (categoria_id),
    INDEX idx_productos_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. TABLA: carritos
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carritos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carritos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. TABLA: detalles_carrito
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalles_carrito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalles_carrito_carrito FOREIGN KEY (carrito_id) REFERENCES carritos (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalles_carrito_producto FOREIGN KEY (producto_id) REFERENCES productos (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. TABLA: pedidos (Dirección y teléfono OBLIGATORIOS, estados de pago y entrega)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    direccion_envio VARCHAR(255) NOT NULL,
    telefono_contacto VARCHAR(20) NOT NULL,
    estado_pago VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    metodo_pago VARCHAR(30) NULL,
    referencia_pago VARCHAR(100) NULL,
    fecha_pago DATETIME NULL,
    fecha_entrega_confirmada DATETIME NULL,
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    INDEX idx_pedidos_usuario (usuario_id),
    INDEX idx_pedidos_estado (estado),
    INDEX idx_pedidos_estado_pago (estado_pago),
    INDEX idx_pedidos_fecha (fecha_pedido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. TABLA: detalles_pedido
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalles_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalles_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalles_pedido_producto FOREIGN KEY (producto_id) REFERENCES productos (id) ON DELETE RESTRICT,
    INDEX idx_detalles_pedido_pedido (pedido_id),
    INDEX idx_detalles_pedido_producto (producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- DATOS INICIALES (SEEDS)
-- =============================================================================

-- Contraseña encriptada con BCrypt:
-- 'admin123'  => $2a$10$abcdefghijklmnopqrstuuEZ4tpXm4W5SeJkDbuNdfJ.CF0.Wc2V2
-- '123456'    => $2a$10$abcdefghijklmnopqrstuuxD0vCwu/f2ahNVJLL35ZYA2xuiRyPH6

INSERT INTO usuarios (id, nombre, email, password, rol, fecha_registro) VALUES
(1, 'Administrador TechStore', 'admin@ecommerce.com', '$2a$10$abcdefghijklmnopqrstuuEZ4tpXm4W5SeJkDbuNdfJ.CF0.Wc2V2', 'ADMIN', NOW()),
(2, 'Melandy Garcia', 'melandy@gmail.com', '$2a$10$abcdefghijklmnopqrstuuxD0vCwu/f2ahNVJLL35ZYA2xuiRyPH6', 'CLIENTE', NOW())
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Clientes con dirección y teléfono
INSERT INTO clientes (id, usuario_id, direccion, telefono) VALUES
(1, 1, 'Av. Principal Central 100 - TechStore HQ', '999888777'),
(2, 2, 'Jr. Ayacucho 450, Huamanga', '987654321')
ON DUPLICATE KEY UPDATE direccion=VALUES(direccion);

-- Categorías
INSERT INTO categorias (id, nombre) VALUES
(1, 'Electronica'),
(2, 'Ropa'),
(3, 'Hogar'),
(4, 'Deportes'),
(5, 'Libros')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Productos con costo unitario para cálculo de margen de ganancia / reportes
INSERT INTO productos (id, nombre, descripcion, precio, costo_unitario, stock, imagen_url, categoria_id) VALUES
(1, 'Smartphone Galaxy S24 Ultra', 'Telefono inteligente de ultima generacion con IA integrada, pantalla AMOLED 6.8" y camara 200MP.', 3799.00, 2650.00, 25, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&h=600&fit=crop', 1),
(2, 'Laptop Pro 15 Gamer RTX 4060', 'Laptop profesional Intel Core i7 13va gen, 32GB RAM, 1TB SSD NVMe, pantalla 165Hz.', 4899.00, 3420.00, 15, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600&h=600&fit=crop', 1),
(3, 'Audifonos Bluetooth Sony ANC', 'Audifonos inalambricos con cancelacion activa de ruido y autonomia de 30 horas.', 649.00, 420.00, 40, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&h=600&fit=crop', 1),
(4, 'Camiseta Algodon Organico Tech', 'Camiseta 100% algodon organico suave, transpirable y costuras reforzadas.', 49.90, 22.00, 80, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&h=600&fit=crop', 2),
(5, 'Jeans Slim Fit Denim Premium', 'Jeans clasicos resistentes y comodos con acabado lavado moderno.', 129.00, 65.00, 50, 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&h=600&fit=crop', 2),
(6, 'Lampara LED Inteligente RGB', 'Lampara de escritorio regulable con control por app y carga inalambrica integrada.', 119.00, 58.00, 35, 'https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=600&h=600&fit=crop', 3),
(7, 'Zapatillas Running Pro Sport', 'Zapatillas ultraligeras con amortiguacion avanzada y suela antideslizante.', 289.00, 155.00, 30, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=600&fit=crop', 4),
(8, 'Libro: Clean Code en Español', 'Manual de desarrollo de software agil de Robert C. Martin.', 99.00, 45.00, 45, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600&h=600&fit=crop', 5)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Carritos iniciales
INSERT INTO carritos (id, usuario_id, fecha_creacion) VALUES
(1, 1, NOW()),
(2, 2, NOW())
ON DUPLICATE KEY UPDATE usuario_id=VALUES(usuario_id);

-- Venta de muestra inicial para poblar el Historial de Ventas y Balance
INSERT INTO pedidos (id, usuario_id, fecha_pedido, total, estado, direccion_envio, telefono_contacto, estado_pago, metodo_pago, referencia_pago, fecha_pago, fecha_entrega_confirmada) VALUES
(1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), 748.00, 'ENTREGADO', 'Jr. Ayacucho 450, Huamanga', '987654321', 'CONFIRMADO', 'YAPE', 'OP-9841284', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), 3799.00, 'PROCESANDO', 'Jr. Ayacucho 450, Huamanga', '987654321', 'CONFIRMADO', 'TRANSFERENCIA', 'BCP-TR-459201', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL)
ON DUPLICATE KEY UPDATE total=VALUES(total);

INSERT INTO detalles_pedido (id, pedido_id, producto_id, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 3, 1, 649.00, 649.00),
(2, 1, 8, 1, 99.00, 99.00),
(3, 2, 1, 1, 3799.00, 3799.00)
ON DUPLICATE KEY UPDATE subtotal=VALUES(subtotal);
