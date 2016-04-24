#!/bin/bash

cd `dirname $0`

url_base=https://10.108.242.194:9443/pcmwebapi-1.0/rest

username=cpe-prod
password=scas2analytics

while [[ $# -gt 1 ]]; do
	args=$args" "$1
	shift
done

curl -k --location-trusted -s -H 'Accept: application/json' -H 'Content-Type: application/json' -H "user: $username" -H "password: $password" $args $url_base/$1 | ./format-json.py
