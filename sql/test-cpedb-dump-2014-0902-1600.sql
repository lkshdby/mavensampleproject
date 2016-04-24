-- MySQL dump 10.13  Distrib 5.5.38, for osx10.9 (i386)
--
-- Host: localhost    Database: dc329ab06987540658c6fb7fe06d39438
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
) ENGINE=InnoDB AUTO_INCREMENT=1269 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ACCOUNTS`
--

LOCK TABLES `ACCOUNTS` WRITE;
/*!40000 ALTER TABLE `ACCOUNTS` DISABLE KEYS */;
INSERT INTO `ACCOUNTS` VALUES (1112,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',NULL,0),(1113,'streams','https://test.marketplace-test.ibmcloud.com','IBM',NULL,0),(1133,'streams','https://test.marketplace-test.ibmcloud.com','IBM',NULL,0),(1145,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1181,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',1431215940000,0),(1182,'streams','https://test.marketplace-test.ibmcloud.com','IBM',1431302340000,0),(1184,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',1431734340000,0),(1191,'cloudera','https://test.marketplace-test.ibmcloud.com','IBM',1404172740000,0),(1192,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1193,'streams','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1197,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',1402185540000,0),(1198,'streams','https://test.marketplace-test.ibmcloud.com','IBM',1402185540000,0),(1202,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',1404604740000,0),(1208,'bluacc-dev','https://ibmbluemix.appdirect.com','ACME11',0,0),(1214,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1218,'bi-qse','https://marketplace.ibmcloud.com','IBM',0,0),(1219,'streams-dev','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1223,'streams','https://test.marketplace-test.ibmcloud.com','IBM',1412121540000,0),(1224,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1225,'hortonworks','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1230,'bi-qse','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1231,'biginsights','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1232,'bluacc','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1233,'bluacc-dev','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1234,'cloudera','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1235,'han-test','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1236,'hortonworks','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1237,'streams','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1238,'streams-dev','http://analytics.icas.ibm.com','_SYSTEM_',0,0),(1244,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,1),(1245,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,1),(1258,'han-test','https://test.marketplace-test.ibmcloud.com','IBM',0,4),(1260,'streams','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1266,'biginsights','https://test.marketplace-test.ibmcloud.com','IBM',0,0),(1267,'streams','https://test.marketplace-test.ibmcloud.com','IBM',1412121540000,0),(1268,'monitoring','https://test.marketplace-test.ibmcloud.com','IBM',0,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLUSTERS`
--

LOCK TABLES `CLUSTERS` WRITE;
/*!40000 ALTER TABLE `CLUSTERS` DISABLE KEYS */;
INSERT INTO `CLUSTERS` VALUES (10,10173,'DemoBackupBICluster','please','2c9e8790-481d01d6-0148-36c93089-0c8a',5,1,1409667455064,0),(11,10156,'DemoStreamsCluster','please','2c9e8790-481d01d6-0148-36ca34f1-0d5e',2,1,1409667517326,0);
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
INSERT INTO `CONTENTMAP` VALUES ('bi-qse','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bi-qse'),('biginsights','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/biginsights'),('bluacc','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bluacc'),('cloudera','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/cloudera'),('han-test','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/han-test'),('hortonworks','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/hortonworks'),('monitoring','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/monitoring'),('streams','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams'),('streams-dev','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams-dev');
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
INSERT INTO `OFFERINGS` VALUES ('bi-qse','biginsights-qse-1341','5zvvYUwKvDqd2EJp+dmz/77pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere BigInsights QSE','bi-qse','bi-qse','dev3-cpe',1),('biginsights','ibm-infosphere-biginsights-3','tBb2/gJVXwRiEuUcIPgOUb7pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere BigInsights','biginsights','biginsights','n35-cpe',1),('bluacc','ibm-blu-acceleration-for-cloud-7','WLgcMAANMGF2qNN1fqj3Jr7pQGBUWA6T7CwihKzYZpA=','IBM BLU Acceleration for Cloud','bluacc','bluacc','dev2-cpe',1),('bluacc-dev','ibm-blu-acceleration-for-cloud-7560','yqRBDBPixp9umw8RpcJlAr7pQGBUWA6T7CwihKzYZpA=','IBM BLU Acceleration for Cloud','bluacc','bluacc','dev2-cpe',1),('cloudera','cloudera-28','xzWL2JpbQInoRXnzpYyNdL7pQGBUWA6T7CwihKzYZpA=','High Performance Analytics Clusters with Cloudera Express','cloudera','cloudera','n35-cpe',1),('han-test','han-test-product-264','XfDJnw1rTdLVypVhMi/z0r7pQGBUWA6T7CwihKzYZpA=','Han Test Product','han-test','han-test','n35-cpe',1),('hortonworks','hortonworks-150','wJA9Gd5iR4c1x/duQJ3GnL7pQGBUWA6T7CwihKzYZpA=','Hortonworks','hortonworks','hortonworks','dev3-cpe',1),('monitoring','monitoring-for-analytics-1072','wC00An61laP+iRajB1L7f77pQGBUWA6T7CwihKzYZpA=','Monitoring for Analytics','monitoring','monitoring','n35-cpe',1),('streams','ibm-infosphere-streams-4','9msL6dNEWEaf715sfCxfIb7pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere Streams','streams','streams','n35-cpe',1),('streams-dev','ibm-infosphere-streams-dev-336','J6HkSgBKKGWt/opHx0fjN77pQGBUWA6T7CwihKzYZpA=','IBM InfoSphere Streams (dev)','streams-dev','streams-dev','dev2-cpe',1);
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
INSERT INTO `PCMAEBACKENDS` VALUES ('dev2-cpe','https://75.126.27.219:8443/pcmwebapi-1.0/rest','2c9e984a-444b68b1-0144-4fe09b3d-0523','cluster-provisioning-engine','2x1ARSJQ9WbQyGtZEOKqVw=='),('dev3-cpe','https://75.126.27.220:8443/pcmwebapi-1.0/rest','2c9e984b-4690508a-0146-90b1b4ed-0005','cluster-provisioning-engine','2x1ARSJQ9WbQyGtZEOKqVw=='),('n35-cpe','https://mapreduce.beta.manage.softlayer.com:8443/pcmwebapi-1.0/rest','2c9e8790-424e0880-0142-d853be03-3fff','cpe-test','2x1ARSJQ9WbQyGtZEOKqVw==');
/*!40000 ALTER TABLE `PCMAEBACKENDS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PLUGINPARAMS`
--

DROP TABLE IF EXISTS `PLUGINPARAMS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLUGINPARAMS` (
  `plugin` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  `value` varchar(250) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `plugin` (`plugin`),
  CONSTRAINT `PLUGINPARAMS_ibfk_1` FOREIGN KEY (`plugin`) REFERENCES `PLUGINS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PLUGINPARAMS`
--

LOCK TABLES `PLUGINPARAMS` WRITE;
/*!40000 ALTER TABLE `PLUGINPARAMS` DISABLE KEYS */;
INSERT INTO `PLUGINPARAMS` VALUES ('streams','kvm.cluster.def','2c9e8790-4615aaf6-0146-164a0fef-09e4'),('streams','edge.tier.name','EdgeNodes'),('streams','cluster.quota','2'),('han-test','kvm.cluster.def','2c9e8790-4615aaf6-0146-de2f782d-6cec'),('han-test','compute.tier.name','ComputeNodes'),('han-test','ha.name.node.price','100'),('han-test','data.masking.price','10'),('biginsights','kvm.cluster.def','2c9e8790-4615aaf6-0146-de2f782d-6cec'),('biginsights','compute.tier.name','ComputeNodes'),('streams-dev','kvm.cluster.def','2c9e984a-464ee803-0146-4ef74912-0116'),('streams-dev','edge.tier.name','EdgeNodes'),('streams-dev','cluster.quota','2'),('bi-qse','kvm.cluster.def','2c9e984b-47003539-0147-12ce6dce-0d75'),('bi-qse','compute.tier.name','ComputeNodes'),('hortonworks','kvm.cluster.def','2c9e984b-47267745-0147-26cb3f73-0782'),('monitoring','kvm.cluster.def','2c9e8790-4615aaf6-0146-de2f782d-6cec');
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
INSERT INTO `PLUGINS` VALUES ('bi-qse','com.ibm.icas.biqse.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bi-qse-plugin.jar'),('biginsights','com.ibm.icas.biginsights.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/biginsights-plugin.jar'),('bluacc','com.ibm.icas.bluacc.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bluacc-plugin.jar'),('cloudera','com.ibm.icas.cloudera.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/cloudera-plugin.jar'),('han-test','han.test.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/han-test-plugin.jar'),('hortonworks','com.ibm.icas.hortonworks.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/hortonworks-plugin.jar'),('monitoring','com.ibm.icas.monitoring.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/monitoring-plugin.jar'),('streams','com.ibm.icas.streams.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams-plugin.jar'),('streams-dev','com.ibm.icas.streams.dev.ServiceProviderPlugin','https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams-dev-plugin.jar');
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
) ENGINE=InnoDB AUTO_INCREMENT=10316 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SUBSCRIBERS`
--

LOCK TABLES `SUBSCRIBERS` WRITE;
/*!40000 ALTER TABLE `SUBSCRIBERS` DISABLE KEYS */;
INSERT INTO `SUBSCRIBERS` VALUES (10132,'p0jlUvk5RB66XOOr09QBpQm7LU5HUEecxEOFYQhMFcu+6UBgVFgOk+wsIoSs2GaQ',1,1112,'https://test.marketplace-test.ibmcloud.com/openid/id/016ce855-21d6-4ddb-a929-3944d6873de6','Kailash Marthi'),(10133,'l7P81moiA9fkNiBzuc8D9jYFLzwrvABmUqO33nPV7tG+6UBgVFgOk+wsIoSs2GaQ',1,1113,'https://test.marketplace-test.ibmcloud.com/openid/id/016ce855-21d6-4ddb-a929-3944d6873de6','Kailash Marthi'),(10156,'vP/CjcdRNYCwl71DdE08HlACnDwg56g1Zjl1ooZRo6i+6UBgVFgOk+wsIoSs2GaQ',1,1133,'https://test.marketplace-test.ibmcloud.com/openid/id/1737acc9-aa21-4d3e-b9f3-a8fef4b07449','Hidayatullah Shaikh'),(10157,'Bq8K21JukAdmYNpKlxdSrLRORFlfp0Ha40noHtOvCma+6UBgVFgOk+wsIoSs2GaQ',1,1133,'https://test.marketplace-test.ibmcloud.com/openid/id/ac8be038-758b-4957-831f-c5a7760bc465','Margot Casey'),(10158,'1leySYM5Pa/VtqrNzsO5ymP0bh6v2xZqSHFWS63ujg++6UBgVFgOk+wsIoSs2GaQ',1,1133,'https://test.marketplace-test.ibmcloud.com/openid/id/016ce855-21d6-4ddb-a929-3944d6873de6','Kailash Marthi'),(10173,'/V1JocpJWiXs8CgAUvEho8SKNlZd7ri+1QT8XFvxroa+6UBgVFgOk+wsIoSs2GaQ',1,1145,'https://test.marketplace-test.ibmcloud.com/openid/id/1737acc9-aa21-4d3e-b9f3-a8fef4b07449','Hidayatullah Shaikh'),(10198,'ud0ZmScBiYIsMujM2+jxkeU3vyVDX5/SFaYBFUkrYEm+6UBgVFgOk+wsIoSs2GaQ',1,1133,'https://test.marketplace-test.ibmcloud.com/openid/id/c6df70b8-6093-403c-a0d6-b9718441024c','Jonathan Henthorn'),(10199,'yqCpHoxZR/tjmxAxoL6O2/mpmc9u1TNcq36faftS1Zi+6UBgVFgOk+wsIoSs2GaQ',1,1145,'https://test.marketplace-test.ibmcloud.com/openid/id/08700876-2a66-4b79-80cd-5b6f38d84ebd','Vijay Bommireddipalli'),(10200,'GlqFgHslZ1bpaO6r25TBgow06nEeO+leEpaTV1f2a+G+6UBgVFgOk+wsIoSs2GaQ',1,1133,'https://test.marketplace-test.ibmcloud.com/openid/id/08700876-2a66-4b79-80cd-5b6f38d84ebd','Vijay Bommireddipalli'),(10212,'BTozNkEz+ZkgHRvwazlIaP+Wq6qJsx0TPHld01RkN2m+6UBgVFgOk+wsIoSs2GaQ',1,1181,'https://test.marketplace-test.ibmcloud.com/openid/id/562826ac-bbee-4393-ae84-62193a95c59a','James VanOosten'),(10213,'TfvTiHj8Pe5Iyi1IkRirQdEQEzt97fwRzhSHfMGTMpK+6UBgVFgOk+wsIoSs2GaQ',1,1182,'https://test.marketplace-test.ibmcloud.com/openid/id/562826ac-bbee-4393-ae84-62193a95c59a','James VanOosten'),(10217,'+Z1lgcA35CwlQIqeCDbEniDqZYbnaUdm8X7CcVn8br6+6UBgVFgOk+wsIoSs2GaQ',1,1184,'https://test.marketplace-test.ibmcloud.com/openid/id/0d99004e-727d-4cc5-a1fa-aedc2bf620b4','Tomas Znamenacek'),(10225,'d/7V5mTJ5C+IsAFcf70OOqoMDl/cSVT6VsLi9G59X9C+6UBgVFgOk+wsIoSs2GaQ',1,1145,'https://test.marketplace-test.ibmcloud.com/openid/id/016ce855-21d6-4ddb-a929-3944d6873de6','Kailash Marthi'),(10229,'vW3sMh/kCIPJR6DZT0GmUMqHrOnCrIcdnSUa2Nr71E6+6UBgVFgOk+wsIoSs2GaQ',1,1191,'https://test.marketplace-test.ibmcloud.com/openid/id/5790982e-7bd8-4e3c-a0c1-e8d84a7e12ed','Radhika Agrawal'),(10230,'Hh2pVtj2obpj1wCVUalgx7Oqe2mmcT1tINCFO837FcK+6UBgVFgOk+wsIoSs2GaQ',1,1192,'https://test.marketplace-test.ibmcloud.com/openid/id/2396c47f-9561-4999-8eee-cb1392f6db37','John Mullaly'),(10231,'XLVqnYdtOsUzA4wOTe27pww9pNSS57mhd+moZ+IirI6+6UBgVFgOk+wsIoSs2GaQ',1,1193,'https://test.marketplace-test.ibmcloud.com/openid/id/2396c47f-9561-4999-8eee-cb1392f6db37','John Mullaly'),(10235,'JJ2gjyStrQ0iRekk0KoCwk2bIbhjww5z/bUtSN8HJam+6UBgVFgOk+wsIoSs2GaQ',1,1197,'https://test.marketplace-test.ibmcloud.com/openid/id/341be531-420e-467a-b4ad-ae11d4c5e900','Han Chen'),(10236,'Qcu3I0pNatvIRwUsV7jfOZjAIB503OK9nHvq4i/rIMO+6UBgVFgOk+wsIoSs2GaQ',1,1198,'https://test.marketplace-test.ibmcloud.com/openid/id/341be531-420e-467a-b4ad-ae11d4c5e900','Han Chen'),(10240,'EzL2D3gXC9gnvQnrGEpG1MZoMT914hxhKPkvvZku9PW+6UBgVFgOk+wsIoSs2GaQ',1,1202,'https://test.marketplace-test.ibmcloud.com/openid/id/e6631d74-964a-4a34-9b38-1e30e461d0b6','deepali bhende'),(10248,'aiD2xLR8jPm9asSVXU+0u/wQNJRV4pujF1AFWxfmEj++6UBgVFgOk+wsIoSs2GaQ',1,1208,'https://ibmbluemix.appdirect.com/openid/id/62636711-02df-47ef-8f35-da2113607e8f','Hidayatullah Shaikh'),(10255,'lR7MUtwUu51/eSBUUvxBoTQStb9By0CsFf3QHLgN2m6+6UBgVFgOk+wsIoSs2GaQ',1,1214,'https://test.marketplace-test.ibmcloud.com/openid/id/c6df70b8-6093-403c-a0d6-b9718441024c','Jonathan Henthorn'),(10259,'bB2QjpexWArHBfn2mnfvUIYM5KyPskQGwsqZqVl1DYe+6UBgVFgOk+wsIoSs2GaQ',1,1218,'https://marketplace.ibmcloud.com/openid/id/88a29453-cbc1-475d-8cee-b6b972a65b10','Han Chen'),(10260,'3YVRPjDRhfp4Uzx8FfAA0Bj8fkraztuGqB3cbaUs++2+6UBgVFgOk+wsIoSs2GaQ',1,1219,'https://test.marketplace-test.ibmcloud.com/openid/id/c3cbc5ae-8877-4fd1-8948-d4e53bbefa4a','Han Chen'),(10267,'i2+eBNfrL7Uh2Vkbyomp62Klni3fRRnlFKO+jr75OMe+6UBgVFgOk+wsIoSs2GaQ',1,1225,'https://test.marketplace-test.ibmcloud.com/openid/id/c8f076c4-4e13-44f0-8588-b6869a49135f','MINKYONG KIM'),(10272,'g4e/z/6SjdSXAO5KiMVCBmDq8qDlQ6HTnS2ukw9Y4/e+6UBgVFgOk+wsIoSs2GaQ',-1,1230,'','Garbage Collector'),(10273,'5twUdprgJkx9U/IaTQVPrQ1STIC+DgCfhxEehpZqajS+6UBgVFgOk+wsIoSs2GaQ',-1,1231,'','Garbage Collector'),(10274,'WlA8f3Z/csMU018JG/QbfNy+FfOnrXftxfDX3JNNQj6+6UBgVFgOk+wsIoSs2GaQ',-1,1232,'','Garbage Collector'),(10275,'kGSYo/Yzj6ds0sS64gF8k70IqOFZLjyZ3AAPgCXP6DS+6UBgVFgOk+wsIoSs2GaQ',-1,1233,'','Garbage Collector'),(10276,'VerqjtCuSt3VfqK863zpbWG22RO9FQ7VXHkbu3jaWDy+6UBgVFgOk+wsIoSs2GaQ',-1,1234,'','Garbage Collector'),(10277,'aFLeErEzor8+jXo6DaQyjFWHQfVDLtWmSOT7gdahbHy+6UBgVFgOk+wsIoSs2GaQ',-1,1235,'','Garbage Collector'),(10278,'zya1Qz2kDQSkWPFx93Di9E20xrlFdJokIrSxk45iGaq+6UBgVFgOk+wsIoSs2GaQ',-1,1236,'','Garbage Collector'),(10279,'PZh3sYo27eWjM6ydRcZMtIoRAL788lt0sy/bw75lqvS+6UBgVFgOk+wsIoSs2GaQ',-1,1237,'','Garbage Collector'),(10280,'xeldCFM3yXaTzlGNWpRZv5/OjYVEsVIUqkY2Dtkemba+6UBgVFgOk+wsIoSs2GaQ',-1,1238,'','Garbage Collector'),(10282,'9f/5Gfz7QsGyV5X63G6N8Ert9540OYDIJz0f/zHHlMa+6UBgVFgOk+wsIoSs2GaQ',1,1224,'https://test.marketplace-test.ibmcloud.com/openid/id/5790982e-7bd8-4e3c-a0c1-e8d84a7e12ed','Radhika Agrawal'),(10288,'VlG1zO++K7JBwDH3s2VTZfIPhs+Qii6H+irmsMmw29G+6UBgVFgOk+wsIoSs2GaQ',1,1244,'https://test.marketplace-test.ibmcloud.com/openid/id/40ef8b38-8f8c-4b1e-b4bc-af4813a64dee','Robert Koch'),(10289,'tZhA42KMOF/ESzP0IFVsiLEalxfI2MFcQAvxchhv/RK+6UBgVFgOk+wsIoSs2GaQ',1,1245,'https://test.marketplace-test.ibmcloud.com/openid/id/ac8be038-758b-4957-831f-c5a7760bc465','Margot Casey'),(10290,'JoX6UsPk43c61n5DKfcoZoK8VMaIoj5I4mMKeUNwQIa+6UBgVFgOk+wsIoSs2GaQ',1,1223,'https://test.marketplace-test.ibmcloud.com/openid/id/1477d112-8c32-4703-ae79-ed13565ce30d','Amar P'),(10298,'HHuSKd36Cu4f9l03ek+M0+fSGVgbs7NTK/KlFuZoc06+6UBgVFgOk+wsIoSs2GaQ',1,1145,'https://test.marketplace-test.ibmcloud.com/openid/id/30805bfb-e860-42bd-976b-664a62fb8692','Samuel Leslie'),(10304,'0BbizoOTCetaqwkbPOhAVmLOBz88w5iut1eVOzUcJj6+6UBgVFgOk+wsIoSs2GaQ',1,1258,'https://test.marketplace-test.ibmcloud.com/openid/id/40ef8b38-8f8c-4b1e-b4bc-af4813a64dee','Robert Koch'),(10306,'G6oX77Y/CKsN8pfBCMk4qiwfvyOYWVUrviMvaijBKBq+6UBgVFgOk+wsIoSs2GaQ',1,1260,'https://test.marketplace-test.ibmcloud.com/openid/id/c6df70b8-6093-403c-a0d6-b9718441024c','Jonathan Henthorn'),(10307,'BydQi0MBSmS0x/e0zlPKNfN9C3guAC5tYlWN3n4vIVS+6UBgVFgOk+wsIoSs2GaQ',1,1145,'https://test.marketplace-test.ibmcloud.com/openid/id/9b373122-382f-417b-a330-69f662ef2084','James Williams'),(10313,'Xd9rvnwsZr7AbkPqZFtXywa2fvhnAAC0VaW/Wr0e4da+6UBgVFgOk+wsIoSs2GaQ',1,1266,'https://test.marketplace-test.ibmcloud.com/openid/id/c4b39dcb-e909-45dd-ac3f-3c1a6fc45c46','Meghana Nikam'),(10314,'BK757yYcXN8UJygS0HvHVGB5aWOpjdW1moK198gwBgW+6UBgVFgOk+wsIoSs2GaQ',1,1267,'https://test.marketplace-test.ibmcloud.com/openid/id/c4b39dcb-e909-45dd-ac3f-3c1a6fc45c46','Meghana Nikam'),(10315,'y9AhVpYrSuZbS+bmFYRbDouxCt/GAwyothgNRpPZvLa+6UBgVFgOk+wsIoSs2GaQ',1,1268,'https://test.marketplace-test.ibmcloud.com/openid/id/c3cbc5ae-8877-4fd1-8948-d4e53bbefa4a','Han Chen');
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

-- Dump completed on 2014-09-02 16:00:22
