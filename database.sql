-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: networkprogramming
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `jobs`
--

DROP TABLE IF EXISTS `jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jobs` (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `status` enum('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
  `total_images` int NOT NULL,
  `processed_images` int NOT NULL DEFAULT '0',
  `task_details` varchar(500) DEFAULT NULL,
  `submit_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `finish_time` timestamp NULL DEFAULT NULL,
  `result_zip_path` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`job_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `jobs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jobs`
--

LOCK TABLES `jobs` WRITE;
/*!40000 ALTER TABLE `jobs` DISABLE KEYS */;
INSERT INTO `jobs` VALUES (1,1,'COMPLETED',4,4,'','2025-11-23 08:56:22','2025-11-23 08:56:26','C:\\results\\job_1.zip'),(2,1,'COMPLETED',4,4,'watermark:true;','2025-11-23 08:56:36','2025-11-23 08:56:37','C:\\results\\job_2.zip'),(3,1,'COMPLETED',5,5,'watermark:true;','2025-11-23 16:38:10','2025-11-23 16:38:12','C:\\results\\job_3.zip'),(4,1,'COMPLETED',10,10,'resize:200;','2025-11-23 16:43:59','2025-11-23 16:44:01','C:\\results\\job_4.zip'),(5,1,'COMPLETED',10,10,'watermark:true;','2025-11-23 16:44:11','2025-11-23 16:44:13','C:\\results\\job_5.zip'),(6,1,'COMPLETED',5,5,'resize:200;','2025-11-23 16:50:46','2025-11-23 16:50:47','C:\\results\\job_6.zip'),(7,1,'COMPLETED',4,4,'resize:200;','2025-11-23 16:56:11','2025-11-23 16:56:12','C:\\results\\job_7.zip'),(8,1,'COMPLETED',25,25,'resize:210;','2025-11-23 16:59:36','2025-11-23 16:59:39','C:\\results\\job_8.zip'),(9,1,'COMPLETED',6,6,'watermark:true;logoPath:C:\\Users\\huynh\\git\\batch-image-processing\\target\\batch-image-processing-0.0.1-SNAPSHOT\\logos\\watermark (2).png;','2025-11-23 17:12:26','2025-11-23 17:12:27','C:\\results\\job_9.zip'),(10,1,'COMPLETED',4,4,'watermark:true;logoPath:C:\\Users\\huynh\\git\\batch-image-processing\\target\\batch-image-processing-0.0.1-SNAPSHOT\\logos\\watermark (2).png;','2025-11-23 17:18:41','2025-11-23 17:18:42','C:\\results\\job_10.zip'),(11,1,'COMPLETED',10,10,'watermark:true;logoPath:C:\\Users\\huynh\\git\\batch-image-processing\\target\\batch-image-processing-0.0.1-SNAPSHOT\\logos\\logo.png;','2025-11-23 17:23:51','2025-11-23 17:23:54','C:\\results\\job_11.zip'),(12,1,'COMPLETED',5,5,'resize:123;','2025-11-23 17:34:44','2025-11-23 17:34:45','C:\\results\\job_12.zip'),(13,1,'COMPLETED',8,8,'watermark:true;logoPath:C:\\Users\\huynh\\git\\batch-image-processing\\target\\batch-image-processing-0.0.1-SNAPSHOT\\logos\\logo.png;','2025-11-23 17:35:11','2025-11-23 17:35:12','C:\\results\\job_13.zip'),(14,1,'COMPLETED',10,10,'resize:500;watermark:true;logoPath:C:\\Users\\huynh\\git\\batch-image-processing\\target\\batch-image-processing-0.0.1-SNAPSHOT\\logos\\watermark (2).png;','2025-11-23 17:38:55','2025-11-23 17:38:57','C:\\results\\job_14.zip');
/*!40000 ALTER TABLE `jobs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Bonbone','8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92','huynhtiennhat0403@gmail.com','2025-11-10 08:27:40');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-24  1:12:03
