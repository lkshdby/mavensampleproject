DELETE FROM CLUSTER_DEFINITIONS;
DELETE FROM ACCOUNTS;

-- mock 5 node
INSERT INTO CLUSTER_DEFINITIONS (frameworkId, id, masterTier) 
VALUES ('hadoop', '00000000-00000000-0000-00000000-0001', 'Master');

-- mock 10 node
INSERT INTO CLUSTER_DEFINITIONS (frameworkId, id, masterTier) 
VALUES ('hadoop', '00000000-00000000-0000-00000000-0002', 'Master');

-- mock 15 node
INSERT INTO CLUSTER_DEFINITIONS (frameworkId, id, masterTier) 
VALUES ('hadoop', '00000000-00000000-0000-00000000-0003', 'Master');

-- mock 20 node
INSERT INTO CLUSTER_DEFINITIONS (frameworkId, id, masterTier) 
VALUES ('hadoop', '00000000-00000000-0000-00000000-0004', 'Master');

INSERT INTO ACCOUNTS (id, pcmUser, pcmPassword, pcmAccount)
VALUES ('sacs-demo', 'sacs-demo', 'changeme', '2c9e8381-3ef1a483-013e-fbda5481-2f81');

