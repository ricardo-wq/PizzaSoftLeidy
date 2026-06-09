-- Crear y usar la base de datos
CREATE DATABASE IF NOT EXISTS `pizzeria` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `pizzeria`;

-- Tabla cliente
CREATE TABLE `cliente` (
  `idCliente` INT(11) NOT NULL AUTO_INCREMENT,
  `nombreCliente` VARCHAR(50),
  `telefono` VARCHAR(20),
  `direccion` VARCHAR(100),
  PRIMARY KEY (`idCliente`)
);

-- Tabla complementos
CREATE TABLE `complementos` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50),
  `precio_chica` DECIMAL(10,2),
  `precio_grande` DECIMAL(10,2),
  `precio_familiar` DECIMAL(10,2),
  PRIMARY KEY (`id`)
);

INSERT INTO `complementos` (`nombre`,`precio_chica`,`precio_grande`,`precio_familiar`) VALUES
  ('Refresco 1.5 L.',30.00,30.00,30.00),
  ('Orilla de Queso',35.00,40.00,50.00),
  ('Orden de Pan con Ajo',35.00,35.00,35.00),
  ('Ingrediente Extra',15.00,20.00,25.00);

-- Tabla pizzas
-- Tabla pizzas sin la columna ingredientesDisponibles
CREATE TABLE `pizzas` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50),
  `precio_chica` DECIMAL(10,2),
  `precio_grande` DECIMAL(10,2),
  `precio_familiar` DECIMAL(10,2),
  PRIMARY KEY (`id`)
);

INSERT INTO `pizzas` (`nombre`,`precio_chica`,`precio_grande`,`precio_familiar`) VALUES
  ('1 Ingrediente',70.00,115.00,140.00),
  ('2 Ingredientes',90.00,135.00,160.00),
  ('3 Ingredientes',110.00,150.00,190.00),
  ('Champinon',100.00,160.00,180.00),
  ('Hawaiana',100.00,160.00,180.00),
  ('Carnes Frias',130.00,195.00,235.00),
  ('Campechana',120.00,180.00,210.00),
  ('Pastor',120.00,200.00,270.00),
  ('Pastor Especial',180.00,295.00,350.00),
  ('Pizza Surtida',140.00,280.00,330.00),
  ('Carpizzio Especial',200.00,295.00,370.00),
  ('Jamon',70.00,115.00,140.00),
   ('Pepperoni',70.00,115.00,140.00),
    ('Salchicha',70.00,115.00,140.00),
     ('Salami',70.00,115.00,140.00);


-- Tabla usuarios
CREATE TABLE `usuarios` (
  `id_usuario` INT(11) NOT NULL AUTO_INCREMENT,
  `usuario` VARCHAR(50) NOT NULL,
  `contraseña` VARCHAR(64) NOT NULL,
  `nombres` VARCHAR(50),
  `apellidos` VARCHAR(50),
  PRIMARY KEY (`id_usuario`)
);

INSERT INTO usuarios (usuario, contraseña, nombres, apellidos)
VALUES ('admin', 'admin', 'admin', 'admin');


CREATE TABLE pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    cliente VARCHAR(100) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(20) NOT NULL
);

-- Tabla ventas
CREATE TABLE `ventas` (
  `id_venta` INT(11) NOT NULL AUTO_INCREMENT,
  `id_pedido` INT NOT NULL,
  `nombre_cliente` VARCHAR(50),
  `fecha_venta` DATE,
  `hora_pedido` TIME,
  `nombre_pizza` VARCHAR(50),
  `tamanio_pizza` VARCHAR(20),
  `ingredientes` VARCHAR(100),
  `precio_pizza` DECIMAL(10,2),
  `complementos` VARCHAR(100),
  `precio_complemento` DECIMAL(10,2),
  `ingredientesExtras` VARCHAR(100),
  `cantidad_pizzas` INT(11),
  `total_venta` DECIMAL(10,2),
  `estado` VARCHAR(20),
  `tiempo_entrega_minutos` INT(11),
  `hora_entrega` TIME,
  PRIMARY KEY (`id_venta`),
  FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
);

CREATE TABLE config (
    id INT(11) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id),
    ruc INT(11) NOT NULL, 
    nombre VARCHAR(100) NOT NULL, 
    telefono VARCHAR(10) NOT NULL, 
    direccion VARCHAR(100) NOT NULL,
    mensaje VARCHAR(300) NOT NULL
) ;
INSERT INTO config (ruc, nombre, telefono, direccion, mensaje)
VALUES (123456789, 'Pizzeria Carpizzio', '9811323039', 'Calle Honestidad, Colonia Miguel Hidalgo', 'Il Sapore di Casa');

