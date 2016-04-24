#!/bin/bash

cd `dirname $0`

CLASSPATH=../build/classes
for f in ../WebContent/WEB-INF/lib/*.jar; do 
	CLASSPATH=$CLASSPATH:$f
done

java -cp $CLASSPATH com.ibm.scas.analytics.utils.PromoCode $*
