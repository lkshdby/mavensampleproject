--
-- Table structure for table `CONTROLTYPES`
--

DROP TABLE IF EXISTS `CONTROLTYPES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CONTROLTYPES` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CONTROLTYPES`
--

LOCK TABLES `CONTROLTYPES` WRITE;
/*!40000 ALTER TABLE `CONTROLTYPES` DISABLE KEYS */;
INSERT INTO `CONTROLTYPES` VALUES (1,'text');
INSERT INTO `CONTROLTYPES` VALUES (2,'textarea');
INSERT INTO `CONTROLTYPES` VALUES (3,'select');
INSERT INTO `CONTROLTYPES` VALUES (4,'checkbox');
INSERT INTO `CONTROLTYPES` VALUES (5,'radio');
INSERT INTO `CONTROLTYPES` VALUES (6,'spinner');
INSERT INTO `CONTROLTYPES` VALUES (7,'link');
/*!40000 ALTER TABLE `CONTROLTYPES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATACENTERFIELDSMAP`
--

DROP TABLE IF EXISTS `DATACENTERFIELDSMAP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATACENTERFIELDSMAP` (
  `dataCenterId` int(11) NOT NULL,
  `formFieldId` int(11) NOT NULL,
  PRIMARY KEY (`dataCenterId`,`formFieldId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATACENTERFIELDSMAP`
--

LOCK TABLES `DATACENTERFIELDSMAP` WRITE;
/*!40000 ALTER TABLE `DATACENTERFIELDSMAP` DISABLE KEYS */;
INSERT INTO `DATACENTERFIELDSMAP` VALUES (1,5);
INSERT INTO `DATACENTERFIELDSMAP` VALUES (1,6);
INSERT INTO `DATACENTERFIELDSMAP` VALUES (1,9);
INSERT INTO `DATACENTERFIELDSMAP` VALUES (1,10);
INSERT INTO `DATACENTERFIELDSMAP` VALUES (1,11);
INSERT INTO `DATACENTERFIELDSMAP` VALUES (1,12);
/*!40000 ALTER TABLE `DATACENTERFIELDSMAP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATACENTERS`
--

DROP TABLE IF EXISTS `DATACENTERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATACENTERS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATACENTERS`
--

LOCK TABLES `DATACENTERS` WRITE;
/*!40000 ALTER TABLE `DATACENTERS` DISABLE KEYS */;
INSERT INTO `DATACENTERS` VALUES (1,'From Softlayer Object');
/*!40000 ALTER TABLE `DATACENTERS` ENABLE KEYS */;
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
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,5,'1');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,6,'2');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,9,'3');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,10,'4');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,131,'6');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,132,'7');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,133,'8');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,134,'5');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,135,'9');
INSERT INTO `DATATRANSFERFIELDSMAP` VALUES (1,136,'10');
/*!40000 ALTER TABLE `DATATRANSFERFIELDSMAP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FORMFIELDS`
--

DROP TABLE IF EXISTS `FORMFIELDS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FORMFIELDS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stepId` int(11) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `label` varchar(45) DEFAULT NULL,
  `typeId` int(11) NOT NULL,
  `orderIndex` int(11) NOT NULL,
  `value` varchar(45) DEFAULT NULL,
  `attachedRESTEvent` varchar(45) DEFAULT NULL,
  `description` varchar(700) DEFAULT NULL,
  `defaultValue` varchar(45) DEFAULT NULL,
  `isMandetory` tinyint(4) DEFAULT '0',
  `isEnabled` tinyint(4) DEFAULT '1',
  `isOnDemand` tinyint(4) DEFAULT '0',
  `helpDescription` varchar(500) DEFAULT NULL,
  `isHelpEnabled` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=157 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FORMFIELDS`
--

LOCK TABLES `FORMFIELDS` WRITE;
/*!40000 ALTER TABLE `FORMFIELDS` DISABLE KEYS */;
INSERT INTO `FORMFIELDS` VALUES (1,1,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (2,1,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (4,3,'importData','Import Data to HDFS',3,0,'No','dataCenters','Select Datacenter','No',1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (5,3,'DATA_IN_USERNAME','Object Store Username',1,3,NULL,NULL,'Object Store Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (6,3,'DATA_IN_APIKEY','Object Store API Key',1,4,NULL,NULL,'Specify the API Key of the Object Store account',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (9,3,'DATA_IN_LOCATION','Data Center',3,5,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (10,3,'DATA_IN_CONTAINER','Object Store Container',3,6,'Select Data Container','getContainersInLocation','Enter HDFS Object Store Container',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (11,3,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (12,3,'DATA_IN_DESTINATION','HDFS Directory',1,8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory. \r\n\r\n Examples: \r\n / \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (13,4,'USE_SDFS','Is Secure Hadoop?',4,0,'Yes','formFields/4','Select Checkbox for Secure Hadoop','No',0,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (14,4,'SDFS_USERNAME','Swift Object Storage Username',1,1,NULL,NULL,'Enter Swift Object Storage Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (15,4,'SDFS_APIKEY','Swift Object Storage API Key',1,2,NULL,NULL,'Enter Swift Object Storage API Key',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (16,4,'SDFS_LOCATION','SDFS Data Center',3,3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (25,7,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (26,7,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (27,10,'importData','Import Data to HDFS',3,0,'No','dataCenters','Select Datacenter','No',1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (28,10,'DATA_IN_USERNAME','Object Store Username',1,3,NULL,NULL,'Object Store Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (29,10,'DATA_IN_APIKEY','Object Store API Key',1,4,NULL,NULL,'Specify the API Key of the Object Store account',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (32,10,'DATA_IN_LOCATION','Data Center Location',3,5,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (33,10,'DATA_IN_CONTAINER','Object Store Container',3,6,'Select Data Container','getContainersInLocation','Enter HDFS Object Store Container',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (34,10,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (35,10,'DATA_IN_DESTINATION','HDFS Directory',1,8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory. \r\n\r\n Examples: \r\n / \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (49,13,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (50,13,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (52,16,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (53,16,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (55,18,'importData','Import Data to HDFS',3,0,'No','dataCenters','Select Datacenter','No',1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (56,18,'DATA_IN_USERNAME','Object Store Username',1,3,NULL,NULL,'Object Store Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (57,18,'DATA_IN_APIKEY','Object Store API Key',1,4,NULL,NULL,'Specify the API Key of the Object Store account',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (58,18,'DATA_IN_LOCATION','Data Center',3,5,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (59,18,'DATA_IN_CONTAINER','Object Store Container',3,6,'Select Data Container','getContainersInLocation','Enter HDFS Object Store Container',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (60,18,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (61,18,'DATA_IN_DESTINATION','HDFS Directory',1,8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory. \r\n\r\n Examples: \r\n / \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (62,14,'edgeNodes','Edge Nodes',6,2,NULL,NULL,'This cluster allows you to have from 0 up to w edge nodes where w is the number of worker nodes for your cluster. Click on Info Icon to learn more about Edge Nodes.','Select',1,1,0,'An edge node is a worker node that is provisioned with a public interface so that the cluster has additional nodes (other than the master node) with public domain access. These nodes can be used to ingest data at scale or from different data sources.',1);
INSERT INTO `FORMFIELDS` VALUES (63,20,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (64,20,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (66,22,'importData','Import Data to HDFS',3,0,'No','dataCenters','Select Datacenter','No',1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (67,22,'DATA_IN_USERNAME','Object Store Username',1,3,NULL,NULL,'Object Store Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (68,22,'DATA_IN_APIKEY','Object Store API Key',1,4,NULL,NULL,'Specify the API Key of the Object Store account',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (69,22,'DATA_IN_LOCATION','Data Center',3,5,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (70,22,'DATA_IN_CONTAINER','Object Store Container',3,6,'Select Data Container','getContainersInLocation','Enter HDFS Object Store Container',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (71,22,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (72,22,'DATA_IN_DESTINATION','HDFS Directory',1,8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory. \r\n\r\n Examples: \r\n / \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (73,23,'USE_SDFS','Is Secure Hadoop?',4,0,'Yes','formFields/4','Select Checkbox for Secure Hadoop','No',0,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (74,23,'SDFS_USERNAME','Swift Object Storage Username',1,1,NULL,NULL,'Enter Swift Object Storage Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (75,23,'SDFS_APIKEY','Swift Object Storage API Key',1,2,NULL,NULL,'Enter Swift Object Storage API Key',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (76,23,'SDFS_LOCATION','SDFS Data Center',3,3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (78,2,'dataNode','Data Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1);
INSERT INTO `FORMFIELDS` VALUES (79,2,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (80,2,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (81,9,'dataNode','Data Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1);
INSERT INTO `FORMFIELDS` VALUES (82,9,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (83,9,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (84,14,'dataNode','Worker Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 worker nodes where n is the quota for your cluster. Click on Info Icon to learn more about Worker Nodes.','Select',1,1,0,'A worker node runs streams application jobs.',1);
INSERT INTO `FORMFIELDS` VALUES (85,14,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, Management API Service, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (86,14,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (87,17,'dataNode','Data Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1);
INSERT INTO `FORMFIELDS` VALUES (88,17,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (89,17,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (90,21,'dataNode','Data Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1);
INSERT INTO `FORMFIELDS` VALUES (91,21,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (92,21,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (115,30,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (116,30,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (117,31,'dataNode','Data Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1);
INSERT INTO `FORMFIELDS` VALUES (118,31,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (119,31,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (120,32,'importData','Import Data to HDFS',3,0,'No','dataCenters','Select Datacenter','No',1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (121,32,'DATA_IN_USERNAME','Object Store Username',1,3,NULL,NULL,'Object Store Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (122,32,'DATA_IN_APIKEY','Object Store API Key',1,4,NULL,NULL,'Specify the API Key of the Object Store account',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (123,32,'DATA_IN_LOCATION','Data Center',3,5,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (124,32,'DATA_IN_CONTAINER','Object Store Container',3,6,'Select Data Container','getContainersInLocation','Enter HDFS Object Store Container',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (125,32,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (126,32,'DATA_IN_DESTINATION','HDFS Directory',1,8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory. \r\n\r\n Examples: \r\n / \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (127,33,'USE_SDFS','Is Secure Hadoop?',4,0,'Yes','formFields/4','Select Checkbox for Secure Hadoop','No',0,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (128,33,'SDFS_USERNAME','Swift Object Storage Username',1,1,NULL,NULL,'Enter Swift Object Storage Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (129,33,'SDFS_APIKEY','Swift Object Storage API Key',1,2,NULL,NULL,'Enter Swift Object Storage API Key',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (130,33,'SDFS_LOCATION','SDFS Data Center',3,3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (131,0,'DATA_IN_DESTINATION','HDFS Directory',1,8,'',NULL,'Enter the target directory for your transfer.  You may enter a new directory or an existing one.  A new directory will be created with the name you enter if it doesn\'t already exist. \r\n\r\n Examples: \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (132,0,'transferDirection','Transfer Direction',5,1,'From Softlayer Object Storage',NULL,'Select Transfer Direction ','From Softlayer Object Storage',1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (133,0,'transferDirection','Transfer Direction',5,2,'To Softlayer Object Storage',NULL,'Select Transfer Direction ',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (134,0,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (135,-1,'DATA_IN_DESTINATION','HDFS Path',1,8,'',NULL,'Enter the source path for your transfer.  It is not recommended to specify a wildcard at the directory level if that directory contains any subdirectories.  Instead, specify the full path. \r\n\r\n # To transfer all objects from a directory, enter /directory/* \r\n # To transfer a specific object from a directory, enter /directory/objectname \r\n # To transfer all objects from a subdirectory, enter /directory/subdirectory/* \r\n # To transfer a specific object from a subdirectory, enter /directory/subdirectory/objectname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (136,-1,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the target path for your transfer. \r\n\r\n # To transfer directly to the container, enter / \r\n # To transfer to a new directory, enter /newDirectoryName',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (141,35,'clusterName','Cluster Name',1,0,NULL,'checkClusterName','Enter Cluster Name',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (142,35,'clusterDescription','Cluster Description',2,1,NULL,NULL,'Enter Cluster Description',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (143,36,'dataNode','Data Nodes',6,1,'','nodes','This cluster allows you to have from 1 up to n-1 data nodes where n is the quota for your cluster. Click on Info Icon to learn more about Data Nodes.','Select',1,1,0,'A data node runs compute services like Data Node, HBase Region Server, Task Tracker etc. Data nodes provide all of their local storage to HDFS.',1);
INSERT INTO `FORMFIELDS` VALUES (144,36,'masterNode','Master Node',1,0,'1',NULL,'This cluster requires 1 and only 1 master node. Click on Info Icon to learn more about Master Nodes.','1',1,1,0,'A master node runs management services like Admin Console, NameNode, Secondary NameNode, JobTracker, HCatalog, etc. If the cluster has / provides access to the Internet, the master node is provisioned with a public interface.',1);
INSERT INTO `FORMFIELDS` VALUES (145,36,'nodeConfiguration','Show / Hide node configuration',7,3,NULL,'nodeConfigurations','Show / Hide node configuration',NULL,1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (146,37,'importData','Import Data to HDFS',3,0,'No','dataCenters','Select Datacenter','No',1,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (147,37,'DATA_IN_USERNAME','Object Store Username',1,3,NULL,NULL,'Object Store Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (148,37,'DATA_IN_APIKEY','Object Store API Key',1,4,NULL,NULL,'Specify the API Key of the Object Store account',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (149,37,'DATA_IN_LOCATION','Data Center',3,5,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (150,37,'DATA_IN_CONTAINER','Object Store Container',3,6,'Select Data Container','getContainersInLocation','Enter HDFS Object Store Container',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (151,37,'DATA_IN_PATH','Object Store Path',1,7,NULL,NULL,'Enter the source path for your transfer. \r\n\r\n # To transfer all content from the container, enter * \r\n # To transfer a specific object from the container, enter objectname \r\n # To transfer all objects from a folder or sub-folder within the container, enter folder/* or folder/subfolder/*, etc. \r\n # To transfer a specific object from a folder or sub-folder, enter folder/objectname or folder/subfolder/objectname, etc.',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (152,37,'DATA_IN_DESTINATION','HDFS Directory',1,8,NULL,NULL,'Enter the target directory for your transfer.  A new directory will be created, and the transfer will go to this directory.  If you enter a slash (/) only, the transfer will go to the parent directory. \r\n\r\n Examples: \r\n / \r\n /directoryname \r\n /directoryname/subdirectoryname',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (153,38,'USE_SDFS','Is Secure Hadoop?',4,0,'Yes','formFields/4','Select Checkbox for Secure Hadoop','No',0,1,0,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (154,38,'SDFS_USERNAME','Swift Object Storage Username',1,1,NULL,NULL,'Enter Swift Object Storage Username',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (155,38,'SDFS_APIKEY','Swift Object Storage API Key',1,2,NULL,NULL,'Enter Swift Object Storage API Key',NULL,1,1,1,NULL,0);
INSERT INTO `FORMFIELDS` VALUES (156,38,'SDFS_LOCATION','SDFS Data Center',3,3,'Select Data Center','softLayerLocations',NULL,NULL,1,1,1,NULL,0);
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
  `pluginId` varchar(50) DEFAULT NULL,
  `nodeSize` varchar(45) DEFAULT NULL,
  `specification` varchar(250) DEFAULT NULL,
  `dataBandwidth` varchar(250) DEFAULT NULL,
  `usedFor` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `pluginId` (`pluginId`),
  CONSTRAINT `NODECONFIGURATIONS_ibfk_1` FOREIGN KEY (`pluginId`) REFERENCES `PLUGINS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NODECONFIGURATIONS`
--

LOCK TABLES `NODECONFIGURATIONS` WRITE;
/*!40000 ALTER TABLE `NODECONFIGURATIONS` DISABLE KEYS */;
INSERT INTO `NODECONFIGURATIONS` VALUES ('1','hortonworks','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('10','streams-dev','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('11','bi-qse','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('12','bi-qse','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('15','bi40-IOP','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('16','bi40-IOP','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('17','bi40-addons','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('18','bi40-addons','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('2','hortonworks','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('3','bi-30','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('4','bi-30','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('5','bi40','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('6','bi40','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('7','bi40','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
INSERT INTO `NODECONFIGURATIONS` VALUES ('8','bi40','Large','. 10 Physical Cores (4 vCPUs)~. 96GB RAM~. 10 X 3.5TB direct attached SATA disks~. Dedicated 10GBPs Ethernet','. 20TB per node over public interface~. Unlimited over private interface','External data ingest, data warehouse, advanced analytics');
INSERT INTO `NODECONFIGURATIONS` VALUES ('9','streams-dev','Small','. 2 Physical Cores (4 vCPUs)~. 19GB RAM~. 2 X 3.5TB direct attached SATA disks~. Shared 10GBPs Ethernet','. 5TB per node over public interface~. Unlimited over private interface','Data extraction, transformation, file processing, search');
/*!40000 ALTER TABLE `NODECONFIGURATIONS` ENABLE KEYS */;
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
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Amsterdam 1','https://ams01.objectstorage.softlayer.net/auth/v1.0/','https://ams01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Dallas 5','https://dal05.objectstorage.softlayer.net/auth/v1.0/','https://dal05.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Frankfurt 2','https://fra02.objectstorage.softlayer.net/auth/v1.0/','https://fra02.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Hong Kong 2','https://hkg02.objectstorage.softlayer.net/auth/v1.0/','https://hkg02.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('London 2','https://lon02.objectstorage.softlayer.net/auth/v1.0/','https://lon02.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Melbourne 1','https://mel01.objectstorage.softlayer.net/auth/v1.0/','https://mel01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Mexico 1','https://mex01.objectstorage.softlayer.net/auth/v1.0/','https://mex01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Milan 1','https://mil01.objectstorage.softlayer.net/auth/v1.0/','https://mil01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Montreal 1','https://mon01.objectstorage.softlayer.net/auth/v1.0/','https://mon01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Paris 1','https://par01.objectstorage.softlayer.net/auth/v1.0/','https://par01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('San Jose 1','https://sjc01.objectstorage.softlayer.net/auth/v1.0/','https://sjc01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Singapore 1','https://sng01.objectstorage.softlayer.net/auth/v1.0/','https://sng01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Sydney 1','https://syd01.objectstorage.softlayer.net/auth/v1.0/','https://syd01.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Tokyo 2','https://tok02.objectstorage.softlayer.net/auth/v1.0/','https://tok02.objectstorage.service.networklayer.com/auth/v1.0/');
INSERT INTO `SOFTLAYERLOCATIONS` VALUES ('Toronto 1','https://tor01.objectstorage.softlayer.net/auth/v1.0/','https://tor01.objectstorage.service.networklayer.com/auth/v1.0/');
/*!40000 ALTER TABLE `SOFTLAYERLOCATIONS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `STEPDETAILS`
--

DROP TABLE IF EXISTS `STEPDETAILS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `STEPDETAILS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stepNumber` int(11) DEFAULT NULL,
  `pluginId` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `description` varchar(250) DEFAULT NULL,
  `formTitle` varchar(45) NOT NULL,
  `formDescription` varchar(250) NOT NULL,
  `isEnabled` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `STEPDETAILS`
--

LOCK TABLES `STEPDETAILS` WRITE;
/*!40000 ALTER TABLE `STEPDETAILS` DISABLE KEYS */;
INSERT INTO `STEPDETAILS` VALUES (1,1,'bi-30','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (2,2,'bi-30','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1);
INSERT INTO `STEPDETAILS` VALUES (3,3,'bi-30','Step 3','Data Automation','Provide information about the Object Store','Please provide details of your data automation',1);
INSERT INTO `STEPDETAILS` VALUES (4,4,'bi-30','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',1);
INSERT INTO `STEPDETAILS` VALUES (5,5,'bi-30','Step 5','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
INSERT INTO `STEPDETAILS` VALUES (7,1,'hortonworks','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (9,3,'hortonworks','Step 3','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1);
INSERT INTO `STEPDETAILS` VALUES (10,4,'hortonworks','Step 4','Data Automation','Provide information about the Object Store','Please provide details of your data automation',1);
INSERT INTO `STEPDETAILS` VALUES (12,6,'hortonworks','Step 6','Review Details','Review Details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
INSERT INTO `STEPDETAILS` VALUES (13,1,'streams-dev','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (14,2,'streams-dev','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1);
INSERT INTO `STEPDETAILS` VALUES (15,3,'streams-dev','Step 3','Review Details','Review Details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
INSERT INTO `STEPDETAILS` VALUES (16,1,'bi-qse','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (17,2,'bi-qse','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*). ',1);
INSERT INTO `STEPDETAILS` VALUES (18,3,'bi-qse','Step 3','Data Automation','Provide information about the Object Store','Please provide details of your data automation',1);
INSERT INTO `STEPDETAILS` VALUES (19,4,'bi-qse','Step 4','Review Details','Review Details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
INSERT INTO `STEPDETAILS` VALUES (20,1,'bi40','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (21,2,'bi40','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (22,3,'bi40','Step 3','Data Automation','Provide information about the Object Store','Please provide details of your data automation',1);
INSERT INTO `STEPDETAILS` VALUES (23,4,'bi40','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0);
INSERT INTO `STEPDETAILS` VALUES (24,5,'bi40','Step 5','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
INSERT INTO `STEPDETAILS` VALUES (30,1,'bi40-IOP','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (31,2,'bi40-IOP','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (32,3,'bi40-IOP','Step 3','Data Automation','Provide information about the Object Store','Please provide details of your data automation',1);
INSERT INTO `STEPDETAILS` VALUES (33,4,'bi40-IOP','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',0);
INSERT INTO `STEPDETAILS` VALUES (34,5,'bi40-IOP','Step 5','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
INSERT INTO `STEPDETAILS` VALUES (35,1,'bi40-addons','Step 1','Cluster Description','Provide details about your cluster','Complete the fields below to describe your new {name} cluster. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (36,2,'bi40-addons','Step 2','Cluster Parameters','Provide details about the cluster size','Please provide details about your cluster size. Required fields are indicated with an asterisk (*).',1);
INSERT INTO `STEPDETAILS` VALUES (37,3,'bi40-addons','Step 3','Data Automation','Provide information about the object store','Please provide details of your data automation',1);
INSERT INTO `STEPDETAILS` VALUES (38,4,'bi40-addons','Step 4','Secure Hadoop','Secure Hadoop','Please provide details of your Secure Hadoop',1);
INSERT INTO `STEPDETAILS` VALUES (39,5,'bi40-addons','Step 5','Review Details','Review details','Please Review your details for {name} ~ Note: After the environment is created, click on the cluster name to view the password information.',1);
/*!40000 ALTER TABLE `STEPDETAILS` ENABLE KEYS */;
UNLOCK TABLES;

