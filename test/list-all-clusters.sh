#!/bin/bash

cd `dirname $0`
curl -s -H "api-key:$ADMIN_KEY" $REST_URL/allclusters | ./format-json.py
 