#!/bin/bash
if [[ $# -eq 2 ]]; then
	SERV=${1}
	PROFILE=${2}
	case $SERV in
		"emr")
			SERV="elasticmapreduce"
			;;
		"cw")
			SERV="cloudwatch"
			;;
		"rs")
			SERV="redshift"
			;;
		"ec")
			SERV="elasticache"
			;;
		"eb")
			SERV="elasticbeanstalk"
			;;
		"ddb")
			SERV="dynamodb"
			;;
		"ds")
			SERV="directoryservicev2"
			;;
		*)
			#SERV="console"
			;;
	esac
	open `java -jar target/allhailakas-1.0-SNAPSHOT-jar-with-dependencies.jar ${SERV} ${PROFILE}`
else
	PROFILE=${1}
	open `java -jar target/allhailakas-1.0-SNAPSHOT-jar-with-dependencies.jar console ${PROFILE}`
fi;