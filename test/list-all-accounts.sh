#!/bin/bash

cd `dirname $0`
curl -s -H "api-key:$ADMIN_KEY" $REST_URL/accounts | ./format-json.py
 