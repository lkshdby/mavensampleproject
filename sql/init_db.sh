#!/bin/bash

cd `dirname $0`

WORKSPACE=`cd ../.. && pwd`

DB_PATH=$WORKSPACE/db
echo "DB path: $DB_PATH"

if [[ -d $DB_PATH ]]; then 
	rm -rf $DB_PATH
fi

java -jar ./derbyrun.jar ij << EOF 
connect 'jdbc:derby:$DB_PATH;create=true';
run 'create_db.sql';
run 'populate_fake_data.sql';
exit;
EOF

