#!/bin/bash

cd `dirname $0`


WORKSPACE=`cd ../.. && pwd`

DB_PATH=$WORKSPACE/db
echo "DB path: $DB_PATH"

java -jar ./derbyrun.jar ij << EOF 
connect 'jdbc:derby:$DB_PATH';
select * from $1;
exit;
EOF
echo
