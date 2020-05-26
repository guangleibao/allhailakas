#!/bin/bash
if [[ $# -eq 1 ]]; then
	PROFILE=${1}
	java -cp target/allhailakas-1.0-SNAPSHOT-jar-with-dependencies.jar bglutil.ahaa.RotateAk ${PROFILE}
fi;