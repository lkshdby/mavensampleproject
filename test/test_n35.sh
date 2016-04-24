#!/bin/bash

cd `dirname $0`

#url_base=http://9.41.160.20:9081/pcm_ae/rest
#url_base=http://9.41.160.42:9081/pcm_ae/rest
#url_base=https://75.126.112.82:8443/cluster-api/rest
url_base=https://75.126.112.82:8443/pcmwebapi-1.0/rest

#username=Admin
#password=Admin
username=cluster-provisioning-engine
password=scas2analytics

while [[ $# -gt 1 ]]; do
	args=$args" "$1
	shift
done

curl -k --location-trusted -s -H 'Accept: application/json' -H 'Content-Type: application/json' -H "user: $username" -H "password: $password" $args $url_base/$1 | ./format-json.py
