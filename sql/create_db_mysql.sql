CREATE TABLE PCMAEBACKENDS (
  id VARCHAR(50),
  url VARCHAR(250),
  account VARCHAR(50),
  username VARCHAR(50),
  password VARCHAR(250),
  PRIMARY KEY(id)
) ENGINE=INNODB;

CREATE TABLE PLUGINS (
  id VARCHAR(50),
  className VARCHAR(250),
  source VARCHAR(250),
  PRIMARY KEY (id)
) ENGINE=INNODB;


CREATE TABLE PLUGINPARAMS (
  plugin VARCHAR(50),
  name VARCHAR(50),
  value VARCHAR(250),
  PRIMARY KEY (plugin, name),
  FOREIGN KEY (plugin) REFERENCES PLUGINS(id) ON DELETE RESTRICT
) ENGINE=INNODB;


CREATE TABLE CONTENTMAP (
  id VARCHAR(50),
  url VARCHAR(500),
  PRIMARY KEY (id)
) ENGINE=INNODB;


CREATE TABLE OFFERINGS (
  id VARCHAR(50) NOT NULL,
  oauthKey VARCHAR(250),
  oauthSecret VARCHAR(250),
  name VARCHAR(250),
  plugin VARCHAR(50),
  urlPath VARCHAR(50),
  multiuser BOOLEAN,
  PRIMARY KEY (id),
  FOREIGN KEY (plugin) REFERENCES PLUGINS(id) ON DELETE RESTRICT,
  FOREIGN KEY (urlPath) REFERENCES CONTENTMAP(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE ACCOUNTS (
  id VARCHAR(36) NOT NULL,
  offeringId VARCHAR(50),
  marketUrl VARCHAR(500),
  partner VARCHAR(500),
  expiration BIGINT,
  quantity INT,
  edition VARCHAR(100),
  state INT,
  PRIMARY KEY (id),
  FOREIGN KEY (offeringId) REFERENCES OFFERINGS(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE ACCOUNT_PARAMS (
  accountId VARCHAR(36) NOT NULL,
  name VARCHAR(250),
  value VARCHAR(4096),
  FOREIGN KEY (accountId) REFERENCES ACCOUNTS(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE SUBSCRIBERS (
  id VARCHAR(36) NOT NULL,
  apiKey VARCHAR(250),
  type INT,
  accountId VARCHAR(36),
  externalId VARCHAR(500),
  name VARCHAR(500),
  PRIMARY KEY (id),
  FOREIGN KEY (accountId) REFERENCES ACCOUNTS(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE CLUSTERS (
  id VARCHAR(36) NOT NULL,
  owner VARCHAR(36),
  name VARCHAR(250),
  description VARCHAR(250),
  clusterId VARCHAR(250),
  size INT,
  state INT,
  launchTime BIGINT,
  terminateTime BIGINT,
  currentStep VARCHAR(30),
  PRIMARY KEY (id),
  FOREIGN KEY (owner) REFERENCES SUBSCRIBERS(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE CLUSTER_PARAMS (
  clusterId VARCHAR(36) NOT NULL,
  name VARCHAR(250),
  value VARCHAR(4096),
  FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE CPELOCATIONS (
  name VARCHAR(100) NOT NULL,
  url VARCHAR(200) NOT NULL,
  pcmaeBackendId VARCHAR(50) NOT NULL,
  PRIMARY KEY (name),
  FOREIGN KEY (pcmaeBackendId) REFERENCES PCMAEBACKENDS(id) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE OFFERINGLOCATIONS (
  id VARCHAR(36) NOT NULL ,
  offeringId VARCHAR(50) NOT NULL ,
  cpeLocationName VARCHAR(100) NOT NULL,
  PRIMARY KEY (id) ,
  FOREIGN KEY (offeringId ) REFERENCES OFFERINGS (id ) ON DELETE RESTRICT,
  FOREIGN KEY (cpeLocationName ) REFERENCES CPELOCATIONS (name ) ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE STEPDETAILS (
  id VARCHAR(36) NOT NULL,
  stepNumber int(11) DEFAULT NULL,
  pluginId varchar(50) DEFAULT NULL,
  name varchar(50) DEFAULT NULL,
  description varchar(250) DEFAULT NULL,
  formTitle varchar(45) NOT NULL,
  formDescription varchar(250) NOT NULL,
  isEnabled tinyint(4) DEFAULT '1',
  PRIMARY KEY (id),
  KEY FK_PLUGIN_ID (pluginId),
  FOREIGN KEY (pluginId) REFERENCES PLUGINS(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE CONTROLTYPES (
  id VARCHAR(36) NOT NULL,
  name varchar(45) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE FORMFIELDS (
  id VARCHAR(36) NOT NULL,
  stepId VARCHAR(36) DEFAULT NULL,
  name varchar(45) DEFAULT NULL,
  label varchar(45) DEFAULT NULL,
  typeId VARCHAR(36) NOT NULL,
  orderIndex int(11) NOT NULL,
  value varchar(45) DEFAULT NULL,
  attachedRESTEvent varchar(45) DEFAULT NULL,
  description varchar(1000) DEFAULT NULL,
  defaultValue varchar(45) DEFAULT NULL,
  isMandetory tinyint(4) DEFAULT '0',
  isEnabled tinyint(4) DEFAULT '1',
  isOnDemand tinyint(4) DEFAULT '0',
  helpDescription varchar(500) DEFAULT NULL,
  isHelpEnabled tinyint(4) DEFAULT '0',
  maximumValue int(10) DEFAULT 0,
  minimumValue int(10) DEFAULT 0,
  PRIMARY KEY (id),
  FOREIGN KEY (stepId) REFERENCES STEPDETAILS(id) ON DELETE CASCADE,
  FOREIGN KEY (typeId) REFERENCES CONTROLTYPES(id) ON DELETE RESTRICT
) ENGINE=InnoDB;


CREATE TABLE DATACENTERS (
  id VARCHAR(36) NOT NULL,
  name varchar(100) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE TABLE SOFTLAYERLOCATIONS (
  name varchar(50) NOT NULL,
  publicUrl varchar(200) DEFAULT NULL,
  privateUrl varchar(200) DEFAULT NULL,
  PRIMARY KEY (name)
) ENGINE=InnoDB;


CREATE TABLE DATACENTERFIELDSMAP (
  id VARCHAR(36) NOT NULL,
  dataCenterId VARCHAR(36) NOT NULL,
  formFieldId VARCHAR(36) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (dataCenterId) REFERENCES DATACENTERS(id) ON DELETE CASCADE,
  FOREIGN KEY (formFieldId) REFERENCES FORMFIELDS(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE NODECONFIGURATIONS (
  id varchar(36) NOT NULL,
  pluginId varchar(45) DEFAULT NULL,
  nodeSize varchar(45) DEFAULT NULL,
  specification varchar(250) DEFAULT NULL,
  dataBandwidth varchar(250) DEFAULT NULL,
  usedFor varchar(250) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY pluginId (pluginId),
  FOREIGN KEY (pluginId) REFERENCES plugins (id)
) ENGINE=InnoDB;

CREATE TABLE DATATRANSFERFIELDSMAP (
  dataCenterId int(11) NOT NULL,
  formFieldId int(11) NOT NULL,
  id varchar(36) NOT NULL,
  PRIMARY KEY (dataCenterId,formFieldId)
) ENGINE=InnoDB;

CREATE TABLE SOFTLAYERACCOUNTS (
  id 			VARCHAR(36) NOT NULL,
  url			VARCHAR(50),
  username		VARCHAR(64),
  apiKey 		VARCHAR(128),
  PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE GATEWAYS (
  id VARCHAR(36) NOT NULL,
  softLayerId	INT,
  type			VARCHAR(10) NOT NULL,
  softLayerAccountId  VARCHAR(36) NOT NULL,
  accountId		VARCHAR(36), 	
  cpeLocationName VARCHAR(100) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (accountId) REFERENCES ACCOUNTS(id) ON DELETE CASCADE,
  FOREIGN KEY (softLayerAccountId) REFERENCES SOFTLAYERACCOUNTS(id) ON DELETE RESTRICT,
  FOREIGN KEY (cpeLocationName ) REFERENCES CPELOCATIONS (name ) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE GATEWAY_SSL_CERTS (
  gatewayId    VARCHAR(36) NOT NULL,
  memberIp     VARCHAR(36) NOT NULL,
  username		VARCHAR(25),
  password		VARCHAR(50), 
  sslCert      VARCHAR(1024) NOT NULL,
  FOREIGN KEY (gatewayId) REFERENCES GATEWAYS(id) ON DELETE CASCADE
) ENGINE=INNODB;


CREATE TABLE VLANS (
  id VARCHAR(36) NOT NULL,
  softLayerId	INT,
  clusterId	VARCHAR(36),
  gatewayId		VARCHAR(36),
  softLayerAccountId  VARCHAR(36),
  PRIMARY KEY (id),
  cpeLocationName VARCHAR(100) NOT NULL,
  FOREIGN KEY (gatewayId) REFERENCES GATEWAYS(id),
  FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id),
  FOREIGN KEY (softLayerAccountId) REFERENCES SOFTLAYERACCOUNTS(id) ON DELETE RESTRICT,
  FOREIGN KEY (cpeLocationName ) REFERENCES CPELOCATIONS (name ) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE SUBNETS (
  id VARCHAR(36) NOT NULL,
  softLayerId	INT,
  vlanId		VARCHAR(36) NOT NULL,
  networkAddr	VARCHAR(15),
  gatewayAddr	VARCHAR(15),
  broadcastAddr	VARCHAR(15),
  cidr			INT,
  softLayerAccountId  VARCHAR(36),
  PRIMARY KEY (id),
  FOREIGN KEY (vlanId) REFERENCES VLANS(id) ON DELETE CASCADE,
  FOREIGN KEY (softLayerAccountId) REFERENCES SOFTLAYERACCOUNTS(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE IPADDRESS (
  id VARCHAR(36) NOT NULL,
  ipAddress 	BIGINT NOT NULL,
  subnetId		VARCHAR(36) NOT NULL,
  hostname		VARCHAR(64),
  clusterId		VARCHAR(36),
  tierName		VARCHAR(50),
  PRIMARY KEY (id),
  FOREIGN KEY (subnetId) REFERENCES SUBNETS(id) ON DELETE CASCADE,
  FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE VPNTUNNELS (
	id     		VARCHAR(36) NOT NULL,
	custIpAddr	VARCHAR(36) NOT NULL,
	gatewayId	VARCHAR(36),
	PRIMARY KEY (id),
	FOREIGN KEY (gatewayId) REFERENCES GATEWAYS(id) ON DELETE RESTRICT
) ENGINE=INNODB;

CREATE TABLE VPNTUNNEL_PARAMS (
	vpnTunnelId	VARCHAR(36) NOT NULL,
	name		VARCHAR(50) NOT NULL,
	value		VARCHAR(255),
	FOREIGN KEY (vpnTunnelId) REFERENCES VPNTUNNELS(id) on DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE SOFTLAYERORDERS (
  id VARCHAR(50),
  softLayerId INT,
  clusterId VARCHAR(36),
  tierName VARCHAR(100),
  softLayerAccountId VARCHAR(36),
  PRIMARY KEY (id),
  FOREIGN KEY (clusterId) REFERENCES CLUSTERS(id) ON DELETE CASCADE,
  FOREIGN KEY (softLayerAccountId) REFERENCES SOFTLAYERACCOUNTS(id) ON DELETE CASCADE
) ENGINE=INNODB;