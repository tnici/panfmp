#!/bin/sh
cd `dirname $0`
. ./config.sh.inc
. ./classpath.sh.inc
java ${PANFMP_TOOLS_JAVA_OPTIONS} \
	-Dlog4j.configuration="file:${PANFMP_TOOLS_LOG4J_CONFIG}" \
	de.pangaea.metadataportal.harvester.Checker \
	"${PANFMP_CONFIG}" "$@"