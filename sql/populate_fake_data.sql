-----------------------------------
-- PCM-AE backends
-----------------------------------

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('n35-cpe', 'https://mapreduce.beta.manage.softlayer.com:8443/pcmwebapi-1.0/rest', '2c9e8790-424e0880-0142-d853be03-3fff', 'cluster-provisioning-engine', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('poc-cpe-sandbox', 'https://usdal05pcm00014ianra.dev.analytics.ibmcloud.com:8443/pcmwebapi-1.0/rest', '2c989851-4818e752-0148-1d271fbf-0f63', 'cpe-sandbox', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('poc-cpe-test', 'https://usdal05pcm00014ianra.dev.analytics.ibmcloud.com:8443/pcmwebapi-1.0/rest', '2c989851-4818e752-0148-1d271fbf-0f63', 'cpe-test', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('poc-cpe-test1', 'https://usdal05pcm00014ianra.dev.analytics.ibmcloud.com:8443/pcmwebapi-1.0/rest', '2c989851-4818e752-0148-1d271fbf-0f63', 'cpe-test1', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('poc-cpe-prod', 'https://usdal05pcm00014ianra.dev.analytics.ibmcloud.com:8443/pcmwebapi-1.0/rest', '2c989851-4818e752-0148-1d271fbf-0f63', 'cpe-prod', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('poc-sf', 'https://usdal05pcm00014ianra.dev.analytics.ibmcloud.com:8443/pcmwebapi-1.0/rest', '2c989851-4856692e-0148-56d84e05-0b94', 'solution-foundry', 'oNbjtah/68dyP44NTrbX6w==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('dev2-cpe', 'https://75.126.27.219:8443/pcmwebapi-1.0/rest', '2c9e984a-444b68b1-0144-4fe09b3d-0523', 'cluster-provisioning-engine', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('dev3-cpe', 'https://75.126.27.220:8443/pcmwebapi-1.0/rest', '2c9e984b-4690508a-0146-90b1b4ed-0005', 'cluster-provisioning-engine', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('hpc11-cpe-prod', 'https://uswdc01hpc00011ianra.analytics.ibmcloud.com:9443/pcmwebapi-1.0/rest', '8aec7242-491ee447-0149-1f5aeeba-00fd', 'cpe-prod', '2x1ARSJQ9WbQyGtZEOKqVw==');

INSERT INTO PCMAEBACKENDS (id, url, account, username, password)
VALUES ('hpc24-cpe-test', 'https://10.124.83.213:9443/pcmwebapi-1.0/rest', '8afcd355-4b849fcf-014b-bed9cdee-061e', 'cpe-test', '2x1ARSJQ9WbQyGtZEOKqVw==');

-----------------------------------
-- Plugins
-----------------------------------

INSERT INTO PLUGINS (id, className, source) 
VALUES ('biginsights', 'com.ibm.icas.biginsights.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/biginsights-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('bi-qse', 'com.ibm.icas.biqse.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/bi-qse-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('streams', 'com.ibm.icas.streams.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/streams-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('streams-dev', 'com.ibm.icas.streams.dev.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/streams-dev-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('cloudera', 'com.ibm.icas.cloudera.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/cloudera-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('bluacc', 'com.ibm.icas.bluacc.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/bluacc-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('hortonworks', 'com.ibm.icas.hortonworks.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/hortonworks-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('han-test', 'han.test.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/han-test-plugin/target/classes/');

INSERT INTO PLUGINS (id, className, source) 
VALUES ('monitoring', 'com.ibm.icas.monitoring.ServiceProviderPlugin', 'file:///Users/chenhan/work/analytics/monitoring-plugin/target/classes/');


UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/bi-qse-plugin/target/classes/' WHERE id='bi-qse';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/biginsights-plugin/target/classes/' WHERE id='biginsights';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/streams-plugin/target/classes/' WHERE id='streams';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/streams-dev-plugin/target/classes/' WHERE id='streams-dev';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/cloudera-plugin/target/classes/' WHERE id='cloudera';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/bluacc-plugin/target/classes/' WHERE id='bluacc';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/hortonworks-plugin/target/classes/' WHERE id='hortonworks';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/han-test-plugin/target/classes/' WHERE id='han-test';
UPDATE PLUGINS SET source='file:///Users/chenhan/work/analytics/monitoring-plugin/target/classes/' WHERE id='monitoring';

-----------------------------------
-- Plugins parameters
-----------------------------------

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams', 'kvm.cluster.def', '2c9e8790-4615aaf6-0146-164a0fef-09e4');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams', 'compute.tier.name', 'ComputeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams', 'edge.tier.name', 'EdgeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams', 'cluster.quota', '2');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams-dev', 'kvm.cluster.def.small', '2c9e8790-4615aaf6-0146-164a0fef-09e4');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams-dev', 'kvm.cluster.def.large', '2c9e8790-4615aaf6-0146-164a0fef-09e4');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams-dev', 'compute.tier.name', 'ComputeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams-dev', 'edge.tier.name', 'EdgeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('streams-dev', 'cluster.quota', '2');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('biginsights', 'kvm.cluster.def', '2c9e8790-4615aaf6-0146-de2f782d-6cec');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('biginsights', 'compute.tier.name', 'ComputeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('bi-qse', 'kvm.cluster.def.small', '2c9e8790-4615aaf6-0146-de2f782d-6cec');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('bi-qse', 'kvm.cluster.def.large', '2c9e8790-4615aaf6-0146-de2f782d-6cec');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('bi-qse', 'compute.tier.name', 'ComputeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('han-test', 'kvm.cluster.def', '2c9e8790-4615aaf6-0146-de2f782d-6cec');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('han-test', 'compute.tier.name', 'ComputeNodes');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('han-test', 'ha.name.node.price', '100');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('han-test', 'data.masking.price', '10');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('hortonworks', 'kvm.cluster.def', '2c989851-486a58d3-0148-7a943e39-0f6c');

INSERT INTO PLUGINPARAMS (plugin, name, value) 
VALUES ('monitoring', 'kvm.cluster.def', '8aec7242-49337986-0149-3d4f5a57-12af');

-----------------------------------
-- Plugin web content 
-----------------------------------

INSERT INTO CONTENTMAP (id, url) VALUES ('biginsights', 'http://localhost:8081/biginsights');
INSERT INTO CONTENTMAP (id, url) VALUES ('bi-qse', 'http://localhost:8081/bi-qse');
INSERT INTO CONTENTMAP (id, url) VALUES ('streams', 'http://localhost:8081/streams');
INSERT INTO CONTENTMAP (id, url) VALUES ('streams-dev', 'http://localhost:8081/streams-dev');
INSERT INTO CONTENTMAP (id, url) VALUES ('cloudera', 'http://localhost:8081/cloudera');
INSERT INTO CONTENTMAP (id, url) VALUES ('bluacc', 'http://localhost:8081/bluacc');
INSERT INTO CONTENTMAP (id, url) VALUES ('hortonworks', 'http://localhost:8081/hortonworks');
INSERT INTO CONTENTMAP (id, url) VALUES ('han-test', 'http://localhost:8081/han-test');
INSERT INTO CONTENTMAP (id, url) VALUES ('monitoring', 'http://localhost:8081/monitoring');
INSERT INTO CONTENTMAP (id, url) VALUES ('dummy', 'http://localhost:8081/dummy');

----------------------------
-- offerings in the sandbox
----------------------------

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, backendId, multiuser) 
VALUES ('biginsights-sandbox', 'mapreduce-as-a-service-7103', '4LgE/Rp2Jilkftrw4PK/GL7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere BigInsights', 'biginsights', 'biginsights', TRUE);
-- oauthSecret: 16URRxGp8lVOUT6k

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, backendId, multiuser) 
VALUES ('streams-sandbox', 'ibm-infosphere-streams-7172', 'c0ANM2RS7qsFMUz9gdtWtr7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere Streams', 'streams', 'streams', TRUE);
-- oauthSecret: llwJXFjIntF9x8it

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, backendId, multiuser) 
VALUES ('han-test-sandbox', 'hans-test-7851', 'LX15Wgy0R8ke9v4clmc/Er7pQGBUWA6T7CwihKzYZpA=', 'Han Test Product', 'han-test', 'han-test', TRUE);
-- oauthSecret: 8ap8yvwfNijVfR35

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, backendId, multiuser)  
VALUES ('bluacc-dev', 'ibm-blu-acceleration-for-cloud-7560', 'yqRBDBPixp9umw8RpcJlAr7pQGBUWA6T7CwihKzYZpA=', 'IBM BLU Acceleration for Cloud', 'bluacc', 'bluacc', TRUE);
-- oauthSecret: 5zRhV0A9rWeTCjpB

----------------------------
-- offerings in STAGE MP
----------------------------

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('biginsights-sandbox', 'mapreduce-as-a-service-7103', '4LgE/Rp2Jilkftrw4PK/GL7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere BigInsights', 'biginsights', 'biginsights', TRUE);
-- oauthSecret: 16URRxGp8lVOUT6k

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams-sandbox', 'ibm-infosphere-streams-7172', 'c0ANM2RS7qsFMUz9gdtWtr7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere Streams', 'streams', 'streams', TRUE);
-- oauthSecret: llwJXFjIntF9x8it

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('han-test-sandbox', 'hans-test-7851', 'LX15Wgy0R8ke9v4clmc/Er7pQGBUWA6T7CwihKzYZpA=', 'Han Test Product', 'han-test', 'han-test', TRUE);
-- oauthSecret: 8ap8yvwfNijVfR35

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser)  
VALUES ('bluacc-dev', 'ibm-blu-acceleration-for-cloud-7560', 'yqRBDBPixp9umw8RpcJlAr7pQGBUWA6T7CwihKzYZpA=', 'IBM BLU Acceleration for Cloud', 'bluacc', 'bluacc', TRUE);
-- oauthSecret: 5zRhV0A9rWeTCjpB

----------------------------
-- offerings in STAGE MP
----------------------------

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('biginsights', 'ibm-infosphere-biginsights-3', 'tBb2/gJVXwRiEuUcIPgOUb7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere BigInsights', 'biginsights', 'biginsights', TRUE);
-- oauthSecret: cM7FMf8eHM2ATWft

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams', 'ibm-infosphere-streams-4', '9msL6dNEWEaf715sfCxfIb7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere Streams', 'streams', 'streams', TRUE);
-- oauthSecret: U0i9xmDcwuWhT5zt

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('cloudera', 'cloudera-28', 'xzWL2JpbQInoRXnzpYyNdL7pQGBUWA6T7CwihKzYZpA=', 'Cloudera', 'cloudera', 'cloudera', TRUE);
-- oauthSecret: DfBqSJn6UFgXzvzY

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('han-test', 'han-test-product-264', 'XfDJnw1rTdLVypVhMi/z0r7pQGBUWA6T7CwihKzYZpA=', 'Han Test Product', 'han-test', 'han-test', TRUE);
-- oauthSecret: NfaQPw26CAHqAi6D

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams-dev', 'ibm-infosphere-streams-dev-336', 'J6HkSgBKKGWt/opHx0fjN77pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere Streams (dev)', 'streams-dev', 'streams-dev', TRUE);
-- oauthSecret: GBAmxLvG6ouRwsoe

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('hortonworks', 'hortonworks-150', 'wJA9Gd5iR4c1x/duQJ3GnL7pQGBUWA6T7CwihKzYZpA=', 'Hortonworks', 'hortonworks', 'hortonworks', TRUE);
-- oauthSecret: 5PzbkL8EHoBufw94

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser)  
VALUES ('bluacc', 'ibm-blu-acceleration-for-cloud-7', 'WLgcMAANMGF2qNN1fqj3Jr7pQGBUWA6T7CwihKzYZpA=', 'IBM BLU Acceleration for Cloud', 'bluacc', 'bluacc', TRUE);
-- oauthSecret: 9dmPiMdglAnWCjIP

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser)  
VALUES ('monitoring', 'monitoring-for-analytics-1072', 'wC00An61laP+iRajB1L7f77pQGBUWA6T7CwihKzYZpA=', 'Monitoring for Analytics', 'monitoring', 'monitoring', TRUE);
-- oauthSecret: xc5liaGRq6AOVGpb

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('bi-qse-stage', 'ibm-infosphere-biginsights-qse-1492', 'EROHeDFK+WraLm9upCr2ur7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere BigInsights QSE (Stage)', 'bi-qse', 'bi-qse', TRUE);
-- oauthSecret: WkbyPcaRlJAy3UXy

----------------------------
-- offerings in TEST1 MP
----------------------------
INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('han-test1', 'han-test-811', 'NauNs0ywO4Jd4L8NbggI6b7pQGBUWA6T7CwihKzYZpA=', 'Han Test', 'han-test', 'han-test', TRUE);
-- oauthSecret: tVvnU2PNzCBbZcOt

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('bi-30', 'test-icas-bi-30-2699', 'LcYFNS3Sj3dweQnoW0QiXr7pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere BigInsights 3.0', 'bi-30', 'bi-30', TRUE);
-- 12EXVfVLBwIo4biG

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('bi-qse', 'test-icas-bi-qse-2696', 'Tfm1gdBVOwjKP2dSRjo+9r7pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere BigInsights QuickStart Edition', 'bi-qse', 'bi-qse', TRUE);
-- S5NyUWasv5N73Uzn 

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams', 'test-icas-streams-2693', 'ledl8dxRJuxauxsFIi03Q77pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere Streams 3.2.1', 'streams', 'streams', TRUE);
-- Xfpi3QuZwi9VZBpC

----------------------------
-- offerings in PROD MP
----------------------------

-- IBM Cloud Analytics Application Services (John's company)
----------------------------
INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('bi-qse-trial', 'bi-free-edition-2266', 'zs8ZXNS5cBt3PMctHwFbC77pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere BigInsights QuickStart Edition', 'bi-qse', 'bi-qse', TRUE);
-- oauthSecret: fWjO6d6qaZPtGFKT

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams-trial', 'streams-free-edition-2269', '7TL/HLcfG1oJZGZ0E4zBML7pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere Streams 3.2.1', 'streams-dev', 'streams-dev', TRUE);
-- oauthSecret: csHFhEnmj0OVmCkx

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('bi-qse', 'ibm-cloud-analytics-services--for-use-with-biginsights-quick-start-edition-2028', 'ADjqnGDpsJLjSQSRzKOa277pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere BigInsights QuickStart Edition', 'bi-qse', 'bi-qse', TRUE);
-- oauthSecret: p7cxj0ncsxY5QXg8

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams', 'ibm-cloud-analytics-services-for-infosphere-streams-1566', 'AavkDWec4hnvhBbRMin9gr7pQGBUWA6T7CwihKzYZpA=', 'IBM Cloud Analytics Application Services|for InfoSphere Streams 3.2.1', 'streams-dev', 'streams-dev', TRUE);
-- oauthSecret: 9GbUhB5viOeCh5qz

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser)  
VALUES ('monitoring', 'monitoring-for-analytics-1765', 'g8/CTyS13mOK19yzflLZab7pQGBUWA6T7CwihKzYZpA=', 'Monitoring for Analytics', 'monitoring', 'monitoring', TRUE);
-- oauthSecret: A4psVOouKzM2DrW0

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser)  
VALUES ('ibm10cent', 'ibm10centtest-1800', 'XFGne0VfExBfhv+hiSRqUb7pQGBUWA6T7CwihKzYZpA=', 'IBM Internal Test', NULL, 'dummy', TRUE);
-- oauthSecret: unMIPDzIrUmyVceO

-- IBM Corporation (Andy's company)
----------------------------
INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams', 'ibm-infosphere-streams-973', 'RFegK4CL7wFp/0ZAaioTfb7pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere Streams', 'streams', 'streams', TRUE);
-- oauthSecret: kJcbShkQ1C1S8d9r

-- IBM Development (Shaikh's company)
----------------------------

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('biginsights', 'test-3', 'nsLtYt3WWvfsI/2Nrt6cZ77pQGBUWA6T7CwihKzYZpA=', 'IBM InfoSphere BigInsights', 'biginsights', 'biginsights', TRUE);
-- oauthSecret: aXNZZ8kR2K05g262

INSERT INTO OFFERINGS (id, oauthKey, oauthSecret, name, plugin, urlPath, multiuser) 
VALUES ('streams-deprecated', 'ibm-infosphere-streams-186', '', 'IBM InfoSphere Streams', 'streams', 'streams', TRUE);
-- oauthSecret: 00AWDz2pIBUQCwXs

-----------------------------------
-- Dummy data for local test
-----------------------------------

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1000, 'biginsights', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1001, 'streams', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1002, 'cloudera', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1003, 'bluacc', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1004, 'hortonworks', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1005, 'han-test', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1006, 'bi-qse', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1007, 'streams-dev', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1008, 'monitoring', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (1009, 'ibm10cent', 'http://analytics.icas.ibm.com', '_SYSTEM_', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2000, 'biginsights', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2001, 'streams', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2002, 'cloudera', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2003, 'bluacc', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2004, 'hortonworks', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2005, 'han-test', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2006, 'bi-qse', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2007, 'streams-dev', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2008, 'monitoring', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

INSERT INTO ACCOUNTS (id, offeringId, marketUrl, partner, quantity)
VALUES (2009, 'ibm10cent', 'https://ibmbluemix.appdirect.com', 'ACME', 0);

------------------

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10000, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1000, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10001, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1001, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10002, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1002, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10003, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1003, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10004, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1004, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10005, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1005, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10006, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1006, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10007, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1007, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10008, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1008, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (10009, 'Nip3cuwDVP+4GbTcELMJWxdNXS1Siw+HQjE4heEmbZmVAQtDTbS1BCwTxCpd3RjK', -1, 1009, '', 'Garbage Collector');
-- apikey: f3b3d6298e536dc42bea97c50196758bf7b4ca3c

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20000, 'f/oxssjrc/+cuv4iPSGozQ==', 1, 2000, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 1234567890

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20001, 'ABlJ1VXUaImNkcD3Tgzdpw==', 1, 2001, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 2345678901

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20002, 'KXmfs40gbcI8lm63UvCZOg==', 1, 2002, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 3456789012

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20003, '4LkxrAokKZW0PLTIa4EO1g==', 1, 2003, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 4567890123

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20004, 'wRUsJd2XwNLh2qU2bICKQQ==', 1, 2004, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 5678901234

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20005, '2jma+8D3XCAAhXS+naXbDg==', 1, 2005, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 6789012345

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20006, 'hzFPfR/mZukiOn3cMeFGKg==', 1, 2006, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 7890123456

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20007, 'KRHp8+qJOGN3w2h4opUdLA==', 1, 2007, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 9012345678

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20008, 'tsTijfza+acif7TacIwnsw==', 1, 2008, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 0123456789

INSERT INTO SUBSCRIBERS (id, apiKey, type, accountId, externalId, name) 
VALUES (20009, 'f/oxssjrc/+cuv4iPSGozQ==', 1, 2009, 'https://ibmbluemix.appdirect.com/openid/id/1913c554-e326-44f9-943b-316de17dc4b3', 'Han Chen');
-- apikey: 1234567890

------------------

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20000, 'Small BI cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20000, 'Medium BI cluster', 'A 15-node cluster', null, 15, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20001, 'Small Streams cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20001, 'Large Streams cluster', 'A 50-node cluster', null, 50, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20002, 'Small Cloudera cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20002, 'Large Cloudera cluster', 'A 50-node cluster', null, 50, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20003, 'Small Bluacc cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20003, 'Large Bluacc cluster', 'A 50-node cluster', null, 50, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20004, 'Small Hortonworks cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20004, 'Large Hortonworks cluster', 'A 50-node cluster', null, 50, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20005, 'Small Han Test cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20005, 'Large Han Test cluster', 'A 50-node cluster', null, 50, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20006, 'Small BI QSE cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20006, 'Large BI QSE cluster', 'A 50-node cluster', null, 50, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20007, 'Small Streams dev cluster', 'A 5-node cluster', null, 5, 1);

INSERT INTO CLUSTERS (owner, name, description, clusterId, size, state) 
VALUES (20007, 'Large Streams dev cluster', 'A 50-node cluster', null, 50, 1);
