UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/biginsights-plugin.jar' 
  WHERE id='biginsights';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bi-qse-plugin.jar' 
  WHERE id='bi-qse';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams-plugin.jar' 
  WHERE id='streams';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams-dev-plugin.jar' 
  WHERE id='streams-dev';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/cloudera-plugin.jar' 
  WHERE id='cloudera';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bluacc-plugin.jar' 
  WHERE id='bluacc';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/hortonworks-plugin.jar' 
  WHERE id='hortonworks';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/han-test-plugin.jar' 
  WHERE id='han-test';

UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/monitoring-plugin.jar' 
  WHERE id='monitoring';

------------------------

UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/biginsights'
  WHERE id='biginsights';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bi-qse'
  WHERE id='bi-qse';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams'
  WHERE id='streams';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/streams-dev'
  WHERE id='streams-dev';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/cloudera'
  WHERE id='cloudera';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/bluacc'
  WHERE id='bluacc';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/hortonworks'
  WHERE id='hortonworks';

UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/han-test'
  WHERE id='han-test';

UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/monitoring'
  WHERE id='monitoring';

  UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins-test/dummy'
  WHERE id='dummy';
