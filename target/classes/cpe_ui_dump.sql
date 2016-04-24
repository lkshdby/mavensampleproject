
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
-- Table structure for table `CONTROLTYPES`
--

DROP TABLE IF EXISTS `CONTROLTYPES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONTROLTYPES` (
  `id` varchar(36) NOT NULL DEFAULT '',
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CONTROLTYPES`
--

LOCK TABLES `CONTROLTYPES` WRITE;
/*!40000 ALTER TABLE `CONTROLTYPES` DISABLE KEYS */;
INSERT INTO `CONTROLTYPES` VALUES ('1','text'),
('2','textarea'),
('3','select'),
('4','checkbox'),
('5','radio'),
('6','spinner'),
('7','link'),
('8','ipaddress');
/*!40000 ALTER TABLE `CONTROLTYPES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATACENTERS`
--

DROP TABLE IF EXISTS `DATACENTERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATACENTERS` (
  `id` varchar(36) NOT NULL DEFAULT '',
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATACENTERS`
--

LOCK TABLES `DATACENTERS` WRITE;
/*!40000 ALTER TABLE `DATACENTERS` DISABLE KEYS */;
INSERT INTO `DATACENTERS` VALUES ('1','From Softlayer Object');
/*!40000 ALTER TABLE `DATACENTERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PLUGINS`
--

DROP TABLE IF EXISTS `PLUGINS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PLUGINS` (
  `id` varchar(50) NOT NULL DEFAULT '',
  `className` varchar(250) DEFAULT NULL,
  `source` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plugins`
--

LOCK TABLES `PLUGINS` WRITE;
/*!40000 ALTER TABLE `PLUGINS` DISABLE KEYS */;
INSERT INTO `PLUGINS` VALUES 
('bi40-IOP','com.ibm.icas.biginsights40.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/biginsights40-plugin.jar'),
('bi40-addons','com.ibm.icas.biginsights40.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/biginsights40-plugin.jar'),
('bi-30','com.ibm.icas.biginsights.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/biginsights-plugin.jar'),
('bi30-sdfs','com.ibm.icas.biginsights.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/biginsights-plugin.jar'),
('bi-qse','com.ibm.icas.biqse.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/bi-qse-plugin.jar'),
('hortonworks','com.ibm.icas.hortonworks.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/hortonworks-plugin.jar'),
('streams','com.ibm.icas.streams.dev.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/streams-dev-plugin.jar'),
('cloudera','com.ibm.icas.cloudera.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/cloudera-plugin.jar'),
('bi41-IOP','com.ibm.icas.biginsights40.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/biginsights40-plugin.jar'),
('bi41-addons','com.ibm.icas.biginsights40.ServiceProviderPlugin','file:///opt/platform/gui/3.0/tomcat/webapps/cpe-plugins/biginsights40-plugin.jar');
/*!40000 ALTER TABLE `PLUGINS` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `STEPDETAILS`
--

DROP TABLE IF EXISTS `STEPDETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `STEPDETAILS` (
  `id` varchar(36) NOT NULL DEFAULT '',
  `stepNumber` int(11) DEFAULT NULL,
  `pluginId` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `description` varchar(250) DEFAULT NULL,
  `formTitle` varchar(45) NOT NULL,
  `formDescription` varchar(250) NOT NULL,
  `isEnabled` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `STEPDETAILS`
--

LOCK TABLES `STEPDETAILS` WRITE;
/*!40000 ALTER TABLE `STEPDETAILS` DISABLE KEYS */;
INSERT INTO `STEPDETAILS` VALUES ('1',1,'bi-30','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('10',3,'hortonworks','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('12',5,'hortonworks','Step 5','Review Details','Review Details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('13',1,'streams','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('14',2,'streams','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1),
 ('15',3,'streams','Step 3','Review Details','Review Details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('2',2,'bi-30','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1),
 ('20',1,'bi30-sdfs','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('21',2,'bi30-sdfs','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1),
 ('22',3,'bi30-sdfs','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',0),
 ('23',4,'bi30-sdfs','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',1),
 ('24',6,'bi30-sdfs','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('25',4,'hortonworks','Step 4','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('26',5,'bi-30','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('27',5,'bi30-sdfs','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('3',3,'bi-30','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('30',0,'hortonworks','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('31',-1,'hortonworks','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('32',0,'bi-30','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('33',-1,'bi-30','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('34',0,'streams','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('35',-1,'streams','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('36',0,'bi30-sdfs','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('37',-1,'bi30-sdfs','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('60',0,'bi40-IOP','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('61',-1,'bi40-IOP','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('62',0,'bi40-addons','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('63',-1,'bi40-addons','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('64',0,'bi-qse','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('65',-1,'bi-qse','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('38',1,'bi40-IOP','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('39',2,'bi40-IOP','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1),
 ('4',4,'bi-30','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0),
 ('40',3,'bi40-IOP','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('41',4,'bi40-IOP','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0),
 ('42',6,'bi40-IOP','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('43',5,'bi40-IOP','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('45',1,'bi40-addons','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('46',2,'bi40-addons','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1),
 ('47',3,'bi40-addons','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('48',4,'bi40-addons','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0),
 ('49',6,'bi40-addons','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('5',6,'bi-30','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('51',1,'bi-qse','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('52',2,'bi-qse','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1),
 ('53',3,'bi-qse','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('54',4,'bi-qse','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0),
 ('55',5,'bi-qse','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('56',6,'bi-qse','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('57',5,'bi40-addons','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('7',1,'hortonworks','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('9',2,'hortonworks','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1),
 ('66',-1,'bi40-IOP','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('67',0,'bi40-IOP','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('68',1,'bi40-IOP','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('69',2,'bi40-IOP','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1),
 ('70',3,'bi40-IOP','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('71',4,'bi40-IOP','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0),
 ('72',5,'bi40-IOP','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('73',6,'bi40-IOP','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1),
 ('74',-1,'bi40-addons','Step -1','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('75',0,'bi40-addons','Step 0','Transfer Data','Transfer Data','dummy step to maintain transfer data steps',0),
 ('76',1,'bi40-addons','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1),
 ('77',2,'bi40-addons','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1),
 ('78',3,'bi40-addons','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1),
 ('79',4,'bi40-addons','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0),
 ('80',5,'bi40-addons','Step 5','Gateway Parameters','Dedicated VPN Gateway Parameters','Provide details about your dedicated VPN gateway. This step is optional. You may skip this step and enter the gateway parameters after the cluster is provisioned. Required fields are indicated with an asterisk (*).',0),
 ('81',6,'bi40-addons','Step 6','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
/*!40000 ALTER TABLE `STEPDETAILS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FORMFIELDS`
--

DROP TABLE IF EXISTS `FORMFIELDS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FORMFIELDS` (
  `id` varchar(36) NOT NULL DEFAULT '',
  `stepId` varchar(36) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `label` varchar(45) DEFAULT NULL,
  `typeId` varchar(36) NOT NULL,
  `orderIndex` int(11) NOT NULL,
  `value` varchar(45) DEFAULT NULL,
  `attachedRESTEvent` varchar(45) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `defaultValue` varchar(45) DEFAULT NULL,
  `isMandetory` tinyint(4) DEFAULT '0',
  `isEnabled` tinyint(4) DEFAULT '1',
  `isOnDemand` tinyint(4) DEFAULT '0',
  `helpDescription` varchar(500) DEFAULT NULL,
  `isHelpEnabled` tinyint(4) DEFAULT '0',
  `maximumValue` int(10) DEFAULT '0',
  `minimumValue` int(10) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FORMFIELDS`
--

LOCK TABLES `FORMFIELDS` WRITE;
/*!40000 ALTER TABLE `FORMFIELDS` DISABLE KEYS */;
INSERT INTO `FORMFIELDS` VALUES ('1','1','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('10','3','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('101','25','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('102','25','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('103','25','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('104','25','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('105','25','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('106','25','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('107','25','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('108','25','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('109','25','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('11','3','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('110','25','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('111','25','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('112','25','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('113','25','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('114','25','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('115','25','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('116','25','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('117','25','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('118','25','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/25','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('119','25','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('12','3','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('120','25','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('121','26','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('122','26','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('123','26','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('124','26','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('125','26','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('126','26','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('127','26','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('128','26','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('129','26','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('13','4','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/4','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('130','26','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('131','26','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('132','26','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('133','26','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('134','26','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('135','26','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('136','26','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('137','26','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('138','26','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/26','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('139','26','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('14','4','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('140','26','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('141','27','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('142','27','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('143','27','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('144','27','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('145','27','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('146','27','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('147','27','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('148','27','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('149','27','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('15','4','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('150','27','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('151','27','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('152','27','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('153','27','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('154','27','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('155','27','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('156','27','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('157','27','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('158','27','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/27','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('159','27','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('16','4','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('160','27','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('161','30','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('162','30','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('163','30','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('164','30','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('165','31','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('166','31','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('167','32','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('168','32','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('169','32','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('170','32','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('171','33','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('172','33','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('173','34','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('174','34','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('175','34','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('176','34','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('177','35','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('178','35','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('179','36','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('180','36','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('181','36','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('182','36','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('183','37','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('184','37','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('191','57','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('192','57','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('193','57','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('194','57','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('195','57','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('196','57','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('197','57','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('198','57','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('199','57','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('2','1','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('200','57','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('201','57','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('202','57','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('203','57','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('204','57','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('205','57','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('206','57','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('207','57','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('208','57','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/57','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('209','57','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('210','57','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('212','38','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('213','38','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('214','40','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('215','40','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('216','40','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('217','40','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('218','40','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('219','40','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('220','40','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('221','41','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/41','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('222','41','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('223','41','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('224','41','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('225','39','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('226','39','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('227','39','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('228','39','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('231','43','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('232','43','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('233','43','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('234','43','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('235','43','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('236','43','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('237','43','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('238','43','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('239','43','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('240','43','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('241','43','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('242','43','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('243','43','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('244','43','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('245','43','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('246','43','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('247','43','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('248','43','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/43','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('249','43','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('25','7','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('250','43','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('252','45','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('253','45','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('254','47','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('255','47','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('256','47','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('257','47','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('258','47','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('259','47','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('26','7','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('260','47','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('261','48','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/48','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('262','48','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('263','48','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('264','48','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('265','46','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('266','46','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('267','46','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('268','46','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('27','10','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('270','51','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('271','53','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('272','53','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('273','55','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('274','55','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('275','55','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('276','55','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('277','55','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('278','55','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('279','55','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('28','10','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('280','55','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('281','55','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('282','54','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/54','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('283','55','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('284','55','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('285','55','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('286','55','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('287','55','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('288','55','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('289','55','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('29','10','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('290','55','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('291','55','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/55','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('292','55','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('293','54','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('294','55','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('296','54','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('297','51','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('298','53','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('299','53','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('300','53','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('301','52','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('302','52','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('303','52','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('304','53','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('305','52','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('306','53','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('308','54','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('309','60','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('310','60','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('311','60','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('312','60','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('313','61','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('314','61','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('315','62','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('316','62','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('317','62','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('318','62','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('319','63','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('32','10','DATA_IN_LOCATION','Data Center Location','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('320','63','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('321','64','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('322','64','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('323','64','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('324','64','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('325','65','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('326','65','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('33','10','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('330','26','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('331','26','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('332','26','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('333','26','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('334','55','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('335','55','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('336','55','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('337','55','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('338','27','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('339','27','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('34','10','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('340','27','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('341','27','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('342','43','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('343','43','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('344','43','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('345','43','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('346','57','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('347','57','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('348','57','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('349','57','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('35','10','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('350','25','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('351','25','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('352','25','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('353','25','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('4','3','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('49','13','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('5','3','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('50','13','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('6','3','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('62','14','edgeNodes','Edge Nodes','6',2,NULL,NULL,'This cluster allows you to have from 0 up to w edge nodes where w is the number of worker nodes for your cluster. Click on Info Icon to learn more about Edge Nodes.','Select',1,1,0,'An edge node is a worker node that is provisioned with a public interface so that the cluster has additional nodes (other than the master node) with public domain access. These nodes can be used to ingest data at scale or from different data sources.',1,0,0),
('63','20','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('64','20','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('66','22','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('67','22','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('68','22','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('69','22','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('70','22','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('71','22','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('72','22','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('73','23','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/23','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('74','23','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('75','23','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('76','23','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('78','2','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('79','2','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('80','2','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('81','9','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('82','9','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('83','9','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('84','14','dataNode','Worker Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 worker nodes where n is the quota for your cluster. Click on Info Icon to learn more about Worker Nodes.','Select',1,1,0,'A worker node runs streams application jobs.',1,0,0),
('85','14','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, Management API Service, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('86','14','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('9','3','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('90','21','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('91','21','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('92','21','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('93','9','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('94','2','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('95','14','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('96','21','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('354','66','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('355','66','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('356','67','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('357','67','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('358','67','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('359','67','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('360','68','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('361','68','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('362','69','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('363','69','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('364','69','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('365','69','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('366','70','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('367','70','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('368','70','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('369','70','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('370','70','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('371','70','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('372','70','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('373','71','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/41','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('374','71','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('375','71','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('376','71','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('377','72','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('378','72','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('379','72','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('380','72','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('381','72','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('382','72','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('383','72','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('384','72','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('385','72','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('386','72','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('387','72','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('388','72','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('389','72','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('390','72','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('391','72','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('392','72','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('393','72','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('394','72','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/43','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('395','72','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('396','72','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('397','72','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('398','72','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('399','72','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('400','72','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0),
('401','74','DATA_IN_DESTINATION','HDFS Path','1',8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n1) To transfer all objects from a directory, enter /directory/* \r\n2) To transfer a specific object from a directory, enter /directory/objectname \r\n3) To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n4) To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0,0,0),
('402','74','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n1) To transfer directly to the container, enter / \r\n2) To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0,0,0),
('403','75','DATA_IN_DESTINATION','HDFS Directory','1',8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it does not already exist. \r\n\r\nExamples: \r\n/directoryname \r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('404','75','transferDirection','Transfer Direction','5',1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0,0,0),
('405','75','transferDirection','Transfer Direction','5',2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0,0,0),
('406','75','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('407','76','clusterName','Cluster Name','1',0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0,64,0),
('408','76','clusterDescription','Cluster Description','2',1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0,0,0),
('409','77','dataNode','Data Nodes','6',1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1,0,0),
('410','77','masterNode','Master Node','1',0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1,0,0),
('411','77','nodeConfiguration','Show / Hide node configuration','7',3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0,0,0),
('412','77','cpeLocationName','Cluster Location','3',4,NULL,'cpeLocations','Select Cluster Location',NULL,1,1,0,NULL,0,0,0),
('413','78','importData','Import Data to HDFS','3',0,'No','dataCenters','Select Import Data to HDFS from \r\n1)No\r\n2)From Softlayer Object','No',1,1,0,NULL,0,0,0),
('414','78','DATA_IN_USERNAME','Object Store Username','1',3,NULL,NULL,'Enter Object Store Username',NULL,1,1,1,NULL,0,0,0),
('415','78','DATA_IN_APIKEY','Object Store API Key','1',4,NULL,NULL,'Specify the API Key of the object store account',NULL,1,1,1,NULL,0,0,0),
('416','78','DATA_IN_LOCATION','Data Center','3',5,'Select Data Center','softLayerLocations','Select Data Center Location',NULL,1,1,1,NULL,0,0,0),
('417','78','DATA_IN_CONTAINER','Object Store Container','3',6,'Select Data Container','getContainersInLocation','Select HDFS Object Store Container',NULL,1,1,1,NULL,0,0,0),
('418','78','DATA_IN_PATH','Object Store Path','1',7,NULL,NULL,'Enter the source path for your transfer. \r\n1) To transfer all content from the container, enter * \r\n2) To transfer a specific object from the container, enter objectname \r\n3) To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n4) To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0,0,0),
('419','78','DATA_IN_DESTINATION','HDFS Directory','1',8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory \r\n\r\nExamples:\r\n/\r\n/directoryname\r\n/directoryname/subdirectoryname',NULL,1,1,1,NULL,0,0,0),
('420','79','USE_SDFS','Use Secure Hadoop with this cluster?','4',0,'Yes','formFields/48','Specify here whether you want to use Secure Hadoop Data Encryption with this cluster.','Yes',0,1,0,NULL,0,0,0),
('421','79','SDFS_USERNAME','Object Storage Username','1',1,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('422','79','SDFS_APIKEY','Object Storage API Key','1',2,NULL,NULL,'To use Secure Hadoop Data Encryption with your Hadoop cluster, please provide the credentials for your Object Storage account.',NULL,1,1,1,NULL,0,0,0),
('423','79','SDFS_LOCATION','Object Storage Datacenter Location','3',3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0,0,0),
('424','80','VPN_IP_ADDRESS','Public IP address of your on-premise gateway','8',0,NULL,NULL,'Enter VPN Gateway IP Address',NULL,1,1,1,NULL,0,0,0),
('425','80','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',1,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('426','80','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',3,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('427','80','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',4,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('428','80','IKE_HASH_ALG','Data Integrity Hash Algorithm','5',5,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('429','80','IKE_ENCRYPTION_ALG','Key-Exchange Encryption Algorithm','5',2,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('430','80','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',6,'2 (recommended)',NULL,'2','2 (recommended)',1,1,1,NULL,0,0,0),
('431','80','IKE_DH_GROUP','Diffe-Hellman Group for IKE SA','5',7,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('432','80','AUTHENTICATION_MODE','Authentication Method','3',8,NULL,'authenticationMethods',NULL,NULL,1,1,1,NULL,0,0,0),
('433','80','IKE_KEY_LIFETIME','Lifetime of IKE SA','6',9,NULL,NULL,NULL,'3600',1,1,1,NULL,0,99999,30),
('434','80','CUST_SUBNETS','Customer Subnets','7',22,NULL,'subnets','Enter Subnet',NULL,1,1,1,NULL,0,0,0),
('435','80','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',10,'AES-256 (recommended)',NULL,'AES-256','AES-256 (recommended)',1,1,1,NULL,0,0,0),
('436','80','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',11,'AES-128',NULL,'AES-128',NULL,1,1,1,NULL,0,0,0),
('437','80','ESP_ENCRYPTION_ALG','Encryption Algorithm for ESP','5',12,'3DES',NULL,'3DES',NULL,1,1,1,NULL,0,0,0),
('438','80','ESP_HASH_ALG','Hash Algorithm for ESP','5',13,'SHA-1 (recommended)',NULL,'SHA-1','SHA-1 (recommended)',1,1,1,NULL,0,0,0),
('439','80','ESP_HASH_ALG','Hash Algorithm for ESP','5',14,'MD5',NULL,'MD5',NULL,1,1,1,NULL,0,0,0),
('440','80','ESP_KEY_LIFETIME','Key Lifetime for ESP (in seconds)','6',19,NULL,NULL,'Enter Key Lifetime for ESP','3600',1,1,1,NULL,0,99999,30),
('441','80','dedicatedVPN','Specify gateway parameters for this cluster?','4',0,'Yes','formFields/57','Select if dedicated VPN required',NULL,0,1,0,NULL,0,0,0),
('442','80','PRE_SHARED_SECRET','Pre-Shared Secret','1',21,NULL,NULL,'Enter Pre-Shared Secret',NULL,1,1,1,NULL,0,0,0),
('443','80','PRE_SHARED_SECRET_CHK','Generate Pre-Shared Secret','4',20,'Yes',NULL,'Select checkbox to generate Pre-Shared Secret Key otherwise enter existing Pre-Shared Secret Key in below box',NULL,0,1,1,NULL,0,0,0),
('444','80','ESP_PFS','ESP Perfect Forward Secrecy','5',15,'Enabled',NULL,'Enabled',NULL,1,1,1,NULL,0,0,0),
('445','80','ESP_PFS','ESP Perfect Forward Secrecy','5',16,'2',NULL,'2',NULL,1,1,1,NULL,0,0,0),
('446','80','ESP_PFS','ESP Perfect Forward Secrecy','5',17,'5',NULL,'5',NULL,1,1,1,NULL,0,0,0),
('447','80','ESP_PFS','ESP Perfect Forward Secrecy','5',18,'Disabled',NULL,'Disabled','Disabled',1,1,1,NULL,0,0,0);
/*!40000 ALTER TABLE `FORMFIELDS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NODECONFIGURATIONS`
--

DROP TABLE IF EXISTS `NODECONFIGURATIONS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `NODECONFIGURATIONS` (
  `id` varchar(36) NOT NULL,
  `pluginId` varchar(45) DEFAULT NULL,
  `nodeSize` varchar(45) DEFAULT NULL,
  `specification` varchar(250) DEFAULT NULL,
  `dataBandwidth` varchar(250) DEFAULT NULL,
  `usedFor` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `pluginId` (`pluginId`),
  CONSTRAINT `NODECONFIGURATIONS_ibfk_1` FOREIGN KEY (`pluginId`) REFERENCES `plugins` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NODECONFIGURATIONS`
--

LOCK TABLES `NODECONFIGURATIONS` WRITE;
/*!40000 ALTER TABLE `NODECONFIGURATIONS` DISABLE KEYS */;
INSERT INTO `NODECONFIGURATIONS` VALUES ('1','hortonworks','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('8','streams','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('9','bi40-IOP','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('10','bi40-addons','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('11','bi40-addons','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('12','bi40-IOP','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('13','bi-qse','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('14','bi-qse','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('2','hortonworks','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('3','bi-30','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('4','bi-30','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('5','bi30-sdfs','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('6','bi30-sdfs','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('7','streams','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('15','bi41-IOP','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('16','bi41-addons','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search'),
('17','bi41-addons','Large','. 10 Physical Cores (20 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics'),
('18','bi41-IOP','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
/*!40000 ALTER TABLE `NODECONFIGURATIONS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATACENTERFIELDSMAP`
--

DROP TABLE IF EXISTS `DATACENTERFIELDSMAP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATACENTERFIELDSMAP` (
  `dataCenterId` varchar(36) NOT NULL DEFAULT '',
  `formFieldId` int(11) NOT NULL,
  PRIMARY KEY (`dataCenterId`,`formFieldId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATACENTERFIELDSMAP`
--

LOCK TABLES `DATACENTERFIELDSMAP` WRITE;
/*!40000 ALTER TABLE `DATACENTERFIELDSMAP` DISABLE KEYS */;
INSERT INTO `DATACENTERFIELDSMAP` VALUES ('1',5),
('1',6),
('1',9),
('1',10),
('1',11),
('1',12);
/*!40000 ALTER TABLE `DATACENTERFIELDSMAP` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `DATATRANSFERFIELDSMAP`
--

DROP TABLE IF EXISTS `DATATRANSFERFIELDSMAP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATATRANSFERFIELDSMAP` (
  `dataCenterId` int(11) NOT NULL,
  `formFieldId` int(11) NOT NULL,
  `id` varchar(36) NOT NULL,
  PRIMARY KEY (`dataCenterId`,`formFieldId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATATRANSFERFIELDSMAP`
--

LOCK TABLES `DATATRANSFERFIELDSMAP` WRITE;
/*!40000 ALTER TABLE `DATATRANSFERFIELDSMAP` DISABLE KEYS */;
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,5,'1'),
(1,6,'2'),
(1,9,'3'),
(1,10,'4'),
(1,164,'5'),
(1,161,'6'),
(1,162,'7'),
(1,163,'8'),
(1,165,'9'),
(1,166,'10');
/*!40000 ALTER TABLE `DATATRANSFERFIELDSMAP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SOFTLAYERLOCATIONS`
--

DROP TABLE IF EXISTS `SOFTLAYERLOCATIONS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SOFTLAYERLOCATIONS` (
  `name` varchar(50) NOT NULL,
  `publicUrl` varchar(200) DEFAULT NULL,
  `privateUrl` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SOFTLAYERLOCATIONS`
--

LOCK TABLES `SOFTLAYERLOCATIONS` WRITE;
/*!40000 ALTER TABLE `SOFTLAYERLOCATIONS` DISABLE KEYS */;
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Amsterdam 1','https://ams01.objectstorage.softlayer.net/auth/v1.0/','https://ams01.objectstorage.service.networklayer.com/auth/v1.0/'),('Dallas 5','https://dal05.objectstorage.softlayer.net/auth/v1.0/','https://dal05.objectstorage.service.networklayer.com/auth/v1.0/'),('Frankfurt 2','https://fra02.objectstorage.softlayer.net/auth/v1.0/','https://fra02.objectstorage.service.networklayer.com/auth/v1.0/'),('Hong Kong 2','https://hkg02.objectstorage.softlayer.net/auth/v1.0/','https://hkg02.objectstorage.service.networklayer.com/auth/v1.0/'),('London 2','https://lon02.objectstorage.softlayer.net/auth/v1.0/','https://lon02.objectstorage.service.networklayer.com/auth/v1.0/'),('Melbourne 1','https://mel01.objectstorage.softlayer.net/auth/v1.0/','https://mel01.objectstorage.service.networklayer.com/auth/v1.0/'),('Mexico 1','https://mex01.objectstorage.softlayer.net/auth/v1.0/','https://mex01.objectstorage.service.networklayer.com/auth/v1.0/'),('Milan 1','https://mil01.objectstorage.softlayer.net/auth/v1.0/','https://mil01.objectstorage.service.networklayer.com/auth/v1.0/'),('Montreal 1','https://mon01.objectstorage.softlayer.net/auth/v1.0/','https://mon01.objectstorage.service.networklayer.com/auth/v1.0/'),('Paris 1','https://par01.objectstorage.softlayer.net/auth/v1.0/','https://par01.objectstorage.service.networklayer.com/auth/v1.0/'),('San Jose 1','https://sjc01.objectstorage.softlayer.net/auth/v1.0/','https://sjc01.objectstorage.service.networklayer.com/auth/v1.0/'),('Singapore 1','https://sng01.objectstorage.softlayer.net/auth/v1.0/','https://sng01.objectstorage.service.networklayer.com/auth/v1.0/'),('Sydney 1','https://syd01.objectstorage.softlayer.net/auth/v1.0/','https://syd01.objectstorage.service.networklayer.com/auth/v1.0/'),('Tokyo 2','https://tok02.objectstorage.softlayer.net/auth/v1.0/','https://tok02.objectstorage.service.networklayer.com/auth/v1.0/'),('Toronto 1','https://tor01.objectstorage.softlayer.net/auth/v1.0/','https://tor01.objectstorage.service.networklayer.com/auth/v1.0/');
/*!40000 ALTER TABLE `SOFTLAYERLOCATIONS` ENABLE KEYS */;
UNLOCK TABLES;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

