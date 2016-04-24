-- MySQL dump 10.13  Distrib 5.5.38, for osx10.9 (i386)
--
-- Host: localhost    Database: da654526e9d0944f797bf2c3ca654170b
-- ------------------------------------------------------
-- Server version	5.5.29-rel29.4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ACCOUNTS`
--

DROP TABLE IF EXISTS `ACCOUNTS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ACCOUNTS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `offeringId` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `marketUrl` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `partner` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `expiration` bigint(20) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `offeringId` (`offeringId`),
  CONSTRAINT `ACCOUNTS_ibfk_1` FOREIGN KEY (`offeringId`) REFERENCES `OFFERINGS` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1172 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ACCOUNTS`
--

LOCK TABLES `ACCOUNTS` WRITE;
/*!40000 ALTER TABLE `ACCOUNTS` DISABLE KEYS */;
INSERT INTO `ACCOUNTS` VALUES (1113,'biginsights','https://marketplace.ibmcloud.com','IBM',NULL,0),(1118,'biginsights','https://marketplace.ibmcloud.com','IBM',NULL,0),(1143,'streams','https://marketplace.ibmcloud.com','IBM',1403913540000,0),(1150,'streams','https://marketplace.ibmcloud.com','IBM',1401494340000,0),(1151,'streams','https://marketplace.ibmcloud.com','IBM',1435708740000,0),(1155,'streams','https://marketplace.ibmcloud.com','IBM',1404518340000,0),(1165,'biginsights','http://analytics.icas.ibm.com','_SYSTEM_',-1,-1),(1166,'streams','http://analytics.icas.ibm.com','_SYSTEM_',-1,-1),(1167,'streams-deprecated','http://analytics.icas.ibm.com','_SYSTEM_',-1,-1),(1168,'streams','https://marketplace.ibmcloud.com','IBM',0,0),(1171,'streams','https://marketplace.ibmcloud.com','IBM',0,0);
/*!40000 ALTER TABLE `ACCOUNTS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLUSTERS`
--

DROP TABLE IF EXISTS `CLUSTERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLUSTERS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) DEFAULT NULL,
  `name` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `description` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `clusterId` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `launchTime` bigint(20) DEFAULT NULL,
  `terminateTime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `owner` (`owner`),
  CONSTRAINT `CLUSTERS_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `SUBSCRIBERS` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLUSTERS`
--

LOCK TABLES `CLUSTERS` WRITE;
/*!40000 ALTER TABLE `CLUSTERS` DISABLE KEYS */;
INSERT INTO `CLUSTERS` VALUES (78,10175,'Test Oct 25','Test Oct 25',NULL,2,1,1414259730524,0);
/*!40000 ALTER TABLE `CLUSTERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CONTENTMAP`
--

DROP TABLE IF EXISTS `CONTENTMAP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONTENTMAP` (
  `id` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `url` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CONTENTMAP`
--

LOCK TABLES `CONTENTMAP` WRITE;
/*!40000 ALTER TABLE `CONTENTMAP` DISABLE KEYS */;
INSERT INTO `CONTENTMAP` VALUES ('biginsights','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/biginsights'),('streams','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/streams');
/*!40000 ALTER TABLE `CONTENTMAP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OFFERINGS`
--

DROP TABLE IF EXISTS `OFFERINGS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OFFERINGS` (
  `id` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `oauthKey` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `oauthSecret` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `plugin` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `urlPath` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `backendId` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `multiuser` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `backendId` (`backendId`),
  KEY `plugin` (`plugin`),
  KEY `urlPath` (`urlPath`),
  CONSTRAINT `OFFERINGS_ibfk_1` FOREIGN KEY (`backendId`) REFERENCES `PCMAEBACKENDS` (`id`),
  CONSTRAINT `OFFERINGS_ibfk_2` FOREIGN KEY (`plugin`) REFERENCES `PLUGINS` (`id`),
  CONSTRAINT `OFFERINGS_ibfk_3` FOREIGN KEY (`urlPath`) REFERENCES `CONTENTMAP` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `OFFERINGS`
--

LOCK TABLES `OFFERINGS` WRITE;
/*!40000 ALTER TABLE `OFFERINGS` DISABLE KEYS */;
INSERT INTO `OFFERINGS` VALUES ('biginsights','test-3','nsLtYt3WWvfsI/2Nrt6cZ77pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere BigInsights','biginsights','biginsights','n35-cpe',1),('streams','ibm-infosphere-streams-973','RFegK4CL7wFp/0ZAaioTfb7pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere Streams','streams','streams','poc-cpe-prod',1),('streams-deprecated','ibm-infosphere-streams-186','YTLhCsm2Tm57WCr+pYZ4OL7pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere Streams (Deprecated)','streams','streams','n35-cpe',1);
/*!40000 ALTER TABLE `OFFERINGS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PCMAEBACKENDS`
--

DROP TABLE IF EXISTS `PCMAEBACKENDS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PCMAEBACKENDS` (
  `id` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `url` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `account` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `username` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `password` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PCMAEBACKENDS`
--

LOCK TABLES `PCMAEBACKENDS` WRITE;
/*!40000 ALTER TABLE `PCMAEBACKENDS` DISABLE KEYS */;
INSERT INTO `PCMAEBACKENDS` VALUES ('n35-cpe','https://mapreduce.beta.manage.softlayer.com:8443/pcmwebapi-1.0/rest','2c9e8790-424e0880-0142-d853be03-3fff','cpe-prod','2x1ARSJQ9WbQyGtZEOKqVw=='),('poc-cpe-prod','https://usdal05pcm00014ianra.dev.analytics.ibmcloud.com:8443/pcmwebapi-1.0/rest','2c989851-4818e752-0148-1d271fbf-0f63','cpe-prod','2x1ARSJQ9WbQyGtZEOKqVw==');
/*!40000 ALTER TABLE `PCMAEBACKENDS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PLUGINPARAMS`
--

DROP TABLE IF EXISTS `PLUGINPARAMS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLUGINPARAMS` (
  `plugin` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `value` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`plugin`,`name`),
  CONSTRAINT `PLUGINPARAMS_ibfk_1` FOREIGN KEY (`plugin`) REFERENCES `PLUGINS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PLUGINPARAMS`
--

LOCK TABLES `PLUGINPARAMS` WRITE;
/*!40000 ALTER TABLE `PLUGINPARAMS` DISABLE KEYS */;
INSERT INTO `PLUGINPARAMS` VALUES ('streams','cluster.quota','1'),('streams','edge.tier.name','EdgeNodes'),('streams','kvm.cluster.def','2c989851-48bca9bf-0148-c836346d-3720');
/*!40000 ALTER TABLE `PLUGINPARAMS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PLUGINS`
--

DROP TABLE IF EXISTS `PLUGINS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLUGINS` (
  `id` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `className` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `source` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PLUGINS`
--

LOCK TABLES `PLUGINS` WRITE;
/*!40000 ALTER TABLE `PLUGINS` DISABLE KEYS */;
INSERT INTO `PLUGINS` VALUES ('biginsights','com.ibm.icas.biginsights.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/biginsights-plugin.jar'),('streams','com.ibm.icas.streams.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/streams-plugin.jar');
/*!40000 ALTER TABLE `PLUGINS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SUBSCRIBERS`
--

DROP TABLE IF EXISTS `SUBSCRIBERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SUBSCRIBERS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `apiKey` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `accountId` int(11) DEFAULT NULL,
  `externalId` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `accountId` (`accountId`),
  CONSTRAINT `SUBSCRIBERS_ibfk_1` FOREIGN KEY (`accountId`) REFERENCES `ACCOUNTS` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10176 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SUBSCRIBERS`
--

LOCK TABLES `SUBSCRIBERS` WRITE;
/*!40000 ALTER TABLE `SUBSCRIBERS` DISABLE KEYS */;
INSERT INTO `SUBSCRIBERS` VALUES (10100,'hNyTB2uTENPzjvBNlk02dMPc658P5meL2lM43S67S2e+6UBgVFgOk+wsIoSs2GaQ',1,1113,'https://marketplace.ibmcloud.com/openid/id/4117e65f-959d-45a8-bd2a-93e2a7d6ad3e','Amit Kumar'),(10105,'CT5exF094JmogEbvp8JZxiIMru1ThtKo6Cv76T5eqr2+6UBgVFgOk+wsIoSs2GaQ',1,1118,'https://marketplace.ibmcloud.com/openid/id/fdea56bd-6f6b-47f1-b44a-ac4b421ece72','Amit Kumar'),(10144,'rnbj1uPlDwH8qo+BzKhddT4/UBmGSp+aGNHbWhtjP5++6UBgVFgOk+wsIoSs2GaQ',1,1143,'https://marketplace.ibmcloud.com/openid/id/32982ff8-b5c5-4749-9754-ff990cd110b2','Michael Branson'),(10151,'NtJeC92zu/iSCcWkee9dvO2y/0L5f6xcnaQczKrvBde+6UBgVFgOk+wsIoSs2GaQ',1,1150,'https://marketplace.ibmcloud.com/openid/id/88a29453-cbc1-475d-8cee-b6b972a65b10','Han Chen'),(10152,'M5ZhpfP4qme+6Dolv7KtDxde3M753EqNTxqhPQZ1GbK+6UBgVFgOk+wsIoSs2GaQ',1,1151,'https://marketplace.ibmcloud.com/openid/id/97d4d385-e767-4795-a613-2875da4832d5','Kailash Marthi'),(10156,'2sB4ZDo+tjGtcNOPSAyo1DYC2+H0K01ols3cEghOcpS+6UBgVFgOk+wsIoSs2GaQ',1,1155,'https://marketplace.ibmcloud.com/openid/id/12710109-2e37-48e4-8fcd-658ffc9af146','Andrew Streit'),(10169,'eC6Hffb7M2Gz7mjR0WV1Ar3VfBGR4E2cKNnSkpayAEO+6UBgVFgOk+wsIoSs2GaQ',-1,1165,'','Garbage Collector'),(10170,'jyps0/WRZ5JgjM0BrYYMXLK5McISykiKM2OJkOveOby+6UBgVFgOk+wsIoSs2GaQ',-1,1166,'','Garbage Collector'),(10171,'3t/1+YEnNIqWdq6fPp4XV86CU9ZQV/wdddkHwJLurXq+6UBgVFgOk+wsIoSs2GaQ',-1,1167,'','Garbage Collector'),(10172,'IfHyWmX9uUyoqmyU5E2k6lVTJU2eUP/yeiV9bFAtlOq+6UBgVFgOk+wsIoSs2GaQ',1,1168,'https://marketplace.ibmcloud.com/openid/id/3c73bacd-31f9-4bd8-976e-512aa0d64aa3','Jonathan Henthorn'),(10175,'lv/LK4pedEt7QqmL+EhXiaXXaHhf1W+miPkJqogyD5S+6UBgVFgOk+wsIoSs2GaQ',1,1171,'https://marketplace.ibmcloud.com/openid/id/6bdd014c-d465-47a7-964e-6a46be700da6','Kimberly Madia');
/*!40000 ALTER TABLE `SUBSCRIBERS` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-11-25 16:08:46
