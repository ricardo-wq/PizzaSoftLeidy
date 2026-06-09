-- MySQL dump 10.13  Distrib 8.4.5, for Win64 (x86_64)
--
-- Host: localhost    Database: pizzeria
-- ------------------------------------------------------
-- Server version	8.4.5

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `idCliente` int NOT NULL AUTO_INCREMENT,
  `nombreCliente` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `telefono` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `direccion` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`idCliente`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'Angel Francisco Ascencio','9811045376','Calle Milagro entre San Antonio y Puente'),(2,'Naty lopez','9813707994','tienda de don tino'),(3,'angeles','9811103812','calle jabin entre almendra y mensura (cuatrimoto)'),(4,'Angelica Uribe','9811061613','calle sacrificio esquina con honestidad casa 2 (naranja)'),(5,'Mamá de emma','9811924038','Mamá de emma'),(6,'mercedes lopez','9813495860','leovigildo gomez calle jerico entre sinai y ninive(casa azul marino)'),(7,'Imelda Contreras','9818296942','siglo xx1'),(8,'Isabella Montejo','9811173963','col miguel hidalgo calle vicente guerrero( a la vuelta de mili)'),(9,'Aron Vazquez','9811707941','calle miolagros col mikguel hidalgo');
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `complementos`
--

DROP TABLE IF EXISTS `complementos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `complementos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `precio_chica` decimal(10,2) DEFAULT NULL,
  `precio_grande` decimal(10,2) DEFAULT NULL,
  `precio_familiar` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `complementos`
--

LOCK TABLES `complementos` WRITE;
/*!40000 ALTER TABLE `complementos` DISABLE KEYS */;
INSERT INTO `complementos` VALUES (1,'Refresco 1.5 L.',35.00,35.00,35.00),(2,'Orilla de Queso',35.00,40.00,50.00),(3,'Orden de Pan con Ajo',35.00,35.00,35.00),(4,'Ingrediente Extra',15.00,20.00,25.00);
/*!40000 ALTER TABLE `complementos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config` (
  `id` int NOT NULL,
  `ruc` int NOT NULL,
  `nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `telefono` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `direccion` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `mensaje` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config`
--

LOCK TABLES `config` WRITE;
/*!40000 ALTER TABLE `config` DISABLE KEYS */;
INSERT INTO `config` VALUES (1,123456789,'Pizzeria Carpizzio','9811323039','Calle Honestidad, Colonia Miguel Hidalgo','Il Sapore di Casa');
/*!40000 ALTER TABLE `config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id_pedido` int NOT NULL AUTO_INCREMENT,
  `cliente` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total` decimal(10,2) NOT NULL DEFAULT '0.00',
  `estado` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id_pedido`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido`
--

LOCK TABLES `pedido` WRITE;
/*!40000 ALTER TABLE `pedido` DISABLE KEYS */;
INSERT INTO `pedido` VALUES (4,'Naty lopez','2025-05-16 19:06:45',160.00,'Entregado'),(5,'angeles','2025-05-16 19:36:27',120.00,'Entregado'),(6,'Angelica Uribe','2025-05-16 20:18:33',150.00,'Entregado'),(7,'Angelica Uribe','2025-05-16 20:19:43',200.00,'Entregado'),(8,'Mamá de emma','2025-05-17 19:52:25',190.00,'Entregado'),(9,'mercedes lopez','2025-05-17 20:22:12',200.00,'Entregado'),(11,'Isabella Montejo','2025-05-17 21:05:37',200.00,'Entregado'),(12,'Aron Vazquez','2025-05-17 22:26:02',310.00,'Entregado'),(13,'Aron Vazquez','2025-05-17 22:28:18',155.00,'Entregado');
/*!40000 ALTER TABLE `pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pizzas`
--

DROP TABLE IF EXISTS `pizzas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pizzas` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `precio_chica` decimal(10,2) DEFAULT NULL,
  `precio_grande` decimal(10,2) DEFAULT NULL,
  `precio_familiar` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pizzas`
--

LOCK TABLES `pizzas` WRITE;
/*!40000 ALTER TABLE `pizzas` DISABLE KEYS */;
INSERT INTO `pizzas` VALUES (1,'1 Ingrediente',70.00,115.00,140.00),(2,'2 Ingredientes',90.00,135.00,160.00),(3,'3 Ingredientes',110.00,150.00,190.00),(4,'Champinon',100.00,160.00,180.00),(5,'Hawaiana',100.00,160.00,180.00),(6,'Carnes Frias',130.00,195.00,235.00),(7,'Campechana',120.00,180.00,210.00),(8,'Pastor',120.00,200.00,270.00),(9,'Pastor Especial',180.00,295.00,350.00),(10,'Pizza Surtida',140.00,280.00,330.00),(11,'Carpizzio Especial',200.00,295.00,370.00),(13,'Jamon',70.00,115.00,140.00),(14,'Pepperoni',70.00,115.00,140.00),(15,'Salchicha',70.00,115.00,140.00),(16,'Salami',70.00,115.00,140.00);
/*!40000 ALTER TABLE `pizzas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `usuario` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `contraseña` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `rol` enum('Administrador','Asistente') CHARACTER SET utf8mb3 COLLATE utf8mb3_spanish_ci NOT NULL DEFAULT 'Asistente',
  `nombres` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `apellidos` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (5,'admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918','Administrador',NULL,NULL),(6,'juan','1234','Asistente','juanito','perez'),(7,'s','s','Asistente','s','s'),(8,'as','f4bf9f7fcbedaba0392f108c59d8f4a38b3838efb64877380171b54475c2ade8','Asistente','as','as');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ventas`
--

DROP TABLE IF EXISTS `ventas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ventas` (
  `id_venta` int NOT NULL AUTO_INCREMENT,
  `id_pedido` int NOT NULL,
  `nombre_cliente` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `fecha_venta` date DEFAULT NULL,
  `hora_pedido` time DEFAULT NULL,
  `nombre_pizza` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tamanio_pizza` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ingredientes` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `precio_pizza` decimal(10,2) DEFAULT NULL,
  `complementos` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `precio_complemento` decimal(10,2) DEFAULT NULL,
  `ingredientesExtras` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cantidad_pizzas` int DEFAULT NULL,
  `total_venta` decimal(10,2) DEFAULT NULL,
  `estado` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tiempo_entrega_minutos` int DEFAULT NULL,
  `hora_entrega` time DEFAULT NULL,
  PRIMARY KEY (`id_venta`),
  KEY `id_pedido` (`id_pedido`),
  CONSTRAINT `ventas_ibfk_1` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ventas`
--

LOCK TABLES `ventas` WRITE;
/*!40000 ALTER TABLE `ventas` DISABLE KEYS */;
INSERT INTO `ventas` VALUES (4,4,'Naty lopez','2025-05-16','19:06:41','2 Ingredientes','Familiar','Jamon, Pepperoni',160.00,'n/a',0.00,'n/a',1,160.00,'Entregado',40,'19:47:19'),(5,5,'angeles','2025-05-16','19:36:15','Pastor','Chica','Pastor',120.00,'n/a',0.00,'n/a',1,120.00,'Entregado',82,'20:58:40'),(6,6,'Angelica Uribe','2025-05-16','20:18:28','3 Ingredientes','Grande','Jamon, Champinon, Pina',150.00,'n/a',0.00,'n/a',1,150.00,'Entregado',39,'20:58:24'),(7,7,'Angelica Uribe','2025-05-16','20:19:36','Pastor','Grande','Pastor',200.00,'n/a',0.00,'n/a',1,200.00,'Entregado',38,'20:58:12'),(8,8,'Mamá de emma','2025-05-17','19:52:20','3 Ingredientes','Familiar','Champinon, Pina, Chorizo',190.00,'n/a',0.00,'n/a',1,190.00,'Entregado',42,'20:34:31'),(9,9,'mercedes lopez','2025-05-17','20:21:54','Pastor','Grande','Pastor',200.00,'n/a',0.00,'n/a',1,200.00,'Entregado',28,'20:50:48'),(12,11,'Isabella Montejo','2025-05-17','21:05:33','Pastor','Grande','Pastor',200.00,'n/a',0.00,'n/a',1,200.00,'Entregado',81,'22:27:29'),(13,12,'Aron Vazquez','2025-05-17','22:22:50','1 Ingrediente','Grande','Pepperoni',115.00,'Orilla de Queso, ',40.00,'n/a',1,155.00,'Entregado',4,'22:27:21'),(14,12,'Aron Vazquez','2025-05-17','22:25:57','1 Ingrediente','Grande','Pepperoni',115.00,'Orilla de Queso, ',40.00,'n/a',1,155.00,'Entregado',4,'22:27:21'),(15,13,'Aron Vazquez','2025-05-17','22:28:14','1 Ingrediente','Grande','Pepperoni',115.00,'Orilla de Queso, ',40.00,'n/a',1,155.00,'Entregado',-151,'19:56:47');
/*!40000 ALTER TABLE `ventas` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-25 19:40:40
