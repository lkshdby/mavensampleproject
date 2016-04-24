UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/biginsights-plugin.jar' 
  WHERE id='biginsights';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/streams-plugin.jar' 
  WHERE id='streams';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/cloudera-plugin.jar' 
  WHERE id='cloudera';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/bluacc-plugin.jar' 
  WHERE id='bluacc';
  
UPDATE PLUGINS 
  set source='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/hortonworks-plugin.jar' 
  WHERE id='hortonworks';
  
------------------------

UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/biginsights'
  WHERE id='biginsights';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/streams'
  WHERE id='streams';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/cloudera'
  WHERE id='cloudera';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/bluacc'
  WHERE id='bluacc';
  
UPDATE CONTENTMAP 
  set url='https://dal05.objectstorage.softlayer.net/v1/AUTH_a13d5a48-a82e-4aa2-a2ee-46c938586366/cpe-plugins/hortonworks'
  WHERE id='hortonworks';

