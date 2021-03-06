@echo off

REM INFO: All paths are relative to the scripts/ directory

REM config file to use
SET PANFMP_CONFIG="../conf/config.xml"

REM java options for harvesting and management tools
SET PANFMP_TOOLS_JAVA_OPTIONS=-XX:+UseG1GC -Xms64M -Xmx512M

REM log4j configuration file for harvesting and management tools
SET PANFMP_TOOLS_LOG4J_CONFIG="./console.log.properties"
