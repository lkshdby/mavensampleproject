UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/biginsights-plugin/bin/' 
  WHERE id='biginsights';
  
UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/bi-qse-plugin/bin/' 
  WHERE id='bi-qse';

UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/streams-plugin/bin/' 
  WHERE id='streams';
  
UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/streams-dev-plugin/bin/' 
  WHERE id='streams-dev';
  
UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/cloudera-plugin/bin/' 
  WHERE id='cloudera';
  
UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/bluacc-plugin/bin/' 
  WHERE id='bluacc';
  
UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/hortonworks-plugin/bin/' 
  WHERE id='hortonworks';
  
UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/han-test-plugin/bin/' 
  WHERE id='han-test';

UPDATE PLUGINS 
  set source='file:///Users/chenhan/work/analytics/monitoring-plugin/bin/' 
  WHERE id='monitoring';

  ------------------------

UPDATE CONTENTMAP 
  set url='http://localhost:8081/biginsights'
  WHERE id='biginsights';
  
UPDATE CONTENTMAP 
  set url='http://localhost:8081/bi-qse'
  WHERE id='bi-qse';
  
UPDATE CONTENTMAP 
  set url='http://localhost:8081/streams'
  WHERE id='streams';
  
UPDATE CONTENTMAP 
  set url='http://localhost:8081/streams-dev'
  WHERE id='streams-dev';
  
UPDATE CONTENTMAP 
  set url='http://localhost:8081/cloudera'
  WHERE id='cloudera';
  
UPDATE CONTENTMAP 
  set url='http://localhost:8081/bluacc'
  WHERE id='bluacc';
  
UPDATE CONTENTMAP 
  set url='http://localhost:8081/hortonworks'
  WHERE id='hortonworks';

UPDATE CONTENTMAP 
  set url='http://localhost:8081/han-test'
  WHERE id='han-test';

UPDATE CONTENTMAP 
  set url='http://localhost:8081/monitoring'
  WHERE id='monitoring';
