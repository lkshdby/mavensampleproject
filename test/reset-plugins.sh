#!/bin/bash

cd `dirname $0`
curl -s -H "api-key:$ADMIN_KEY" -X DELETE $REST_URL/plugins | ./format-json.py
 